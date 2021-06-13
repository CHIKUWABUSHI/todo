package local.hal.an91.android.todo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import static java.sql.Date.valueOf;

public class TaskEditActivity extends AppCompatActivity {

    private int _mode = Consts.MODE_INSERT;
    private long _idNo = 0;
    private DatabaseHelper _helper;
    private String date = "1998-10-21";

    Calendar cal = Calendar.getInstance();
    int nowYear = cal.get(Calendar.YEAR);
    int nowMonth = cal.get(Calendar.MONTH);
    int nowDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

    Date taskDate = new Date();
    SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);

        _helper = new DatabaseHelper(getApplicationContext());

        Intent intent = getIntent();
        _mode = intent.getIntExtra("mode", Consts.MODE_INSERT);

        if(_mode == Consts.MODE_INSERT){
            TextView tvTitleEdit = findViewById(R.id.tvTitleEdit);
            tvTitleEdit.setText(R.string.tv_title_insert);

            TextView tvInputDeadline = findViewById(R.id.tvInputDeadline);

            String today =nowYear + "-" + (nowMonth + 1) + "-" + nowDayOfMonth;
            try {
                taskDate = sdFormat.parse(today);
                date = sdFormat.format(taskDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tvInputDeadline.setText(date);
        }
        else{
            _idNo = intent.getLongExtra("idNo", 0);
            SQLiteDatabase db = _helper.getWritableDatabase();
            Task taskData = DataAccess.findByPK(db, _idNo);

            EditText etInputName = findViewById(R.id.etInputName);
            etInputName.setText(taskData.getName());

            TextView tvInputDeadline = findViewById(R.id.tvInputDeadline);
            try {
                taskDate = sdFormat.parse(taskData.getDeadline());
                date = sdFormat.format(taskDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //締め切り期限
            String[] strDeadline = date.split("-", 0);
            int intYear = Integer.parseInt(strDeadline[0]);
            int intMonth = Integer.parseInt(strDeadline[1]);
            int intDay = Integer.parseInt(strDeadline[2]);

            Calendar calDeadline = Calendar.getInstance();
            calDeadline.set(intYear, intMonth, intDay);
            Calendar diffCal = Calendar.getInstance();
            diffCal.set(nowYear, nowMonth+1, nowDayOfMonth);

            //残り日数
            long remainingDay = (calDeadline.getTimeInMillis() - diffCal.getTimeInMillis())/ (1000*24*60*60);

            //期限判定
            if(remainingDay < 0){
                Toast.makeText(TaskEditActivity.this, "期限切れです", Toast.LENGTH_SHORT).show();
                tvInputDeadline.setTextColor(Color.RED);
            }else if (remainingDay == 0){
                Toast.makeText(TaskEditActivity.this, "今日が期限です", Toast.LENGTH_SHORT).show();
                tvInputDeadline.setTextColor(Color.GREEN);
            }else {
                Toast.makeText(TaskEditActivity.this, "残り" + remainingDay + "日です", Toast.LENGTH_SHORT).show();
            }

            tvInputDeadline.setText(date);

            Switch swTaskStatus =findViewById(R.id.swStatus);
            //無理
            //TextView taskStatus = findViewById(R.id.tvTaskStatus);
            int dbDone = taskData.getDone();
            if(dbDone == 1){
                swTaskStatus.setChecked(true);
                //taskStatus.setText("完了:");
            }else{
                swTaskStatus.setChecked(false);
                //taskStatus.setText("未完了:");
            }

            EditText etInputNote = findViewById(R.id.etInputNote);
            etInputNote.setText(taskData.getNote());
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
    @Override
    protected void onDestroy(){
        _helper.close();
        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        if (_mode == Consts.MODE_INSERT){
            inflater.inflate(R.menu.menu_options_add, menu);
        }
        else{
            inflater.inflate(R.menu.menu_options_edit, menu);
        }
        return true;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id) {
            case R.id.btnSave:
                EditText etInputName = findViewById(R.id.etInputName);
                String inputName = etInputName.getText().toString();
                if (inputName.equals("")) {
                    Toast.makeText(local.hal.an91.android.todo.TaskEditActivity.this, R.string.msg_input_title, Toast.LENGTH_SHORT).show();
                } else {
                    TextView etInputDeadline = findViewById(R.id.tvInputDeadline);
                    String inputDeadline = etInputDeadline.getText().toString();
                    try {
                        taskDate = sdFormat.parse(inputDeadline);
                        date = sdFormat.format(taskDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Switch inputTaskStatus =findViewById(R.id.swStatus);
//                    boolean blTaskStatus = inputTaskStatus.isChecked();
//                    int inputDone = Boolean.hashCode(blTaskStatus);
                    int inputDone = 0;
                    if(inputTaskStatus.isChecked()) {
                        inputDone = 1;
                    }

                    EditText etInputNote = findViewById(R.id.etInputNote);
                    String inputNote = etInputNote.getText().toString();
                    SQLiteDatabase db = _helper.getWritableDatabase();
                    if (_mode == Consts.MODE_INSERT) {
                        DataAccess.insert(db, inputName, date, inputDone, inputNote);
                    } else {
                        DataAccess.update(db, _idNo, inputName, date, inputDone, inputNote);
                    }
                    finish();
                }
                return true;
            case android.R.id.home:
                finish();
                return true;
            case R.id.btnDelete:
                deleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void deleteDialog(){
        Bundle extras = new Bundle();
        extras.putLong("id", _idNo);
        extras.putBoolean("checkActivity", false);
        DeleteDialogFragment dialog = new DeleteDialogFragment(_helper);
        dialog.setArguments(extras);
        FragmentManager manager = getSupportFragmentManager();
        dialog.show(manager, "deleteDialogFragment");
    }

    public void deadlineDialog(View view) throws ParseException {
        TextView tvInputDeadline = findViewById(R.id.tvInputDeadline);
        String strDeadline = (String) tvInputDeadline.getText();
        String[] strArrayDeadline = strDeadline.split("-", 0);
        int intYear = Integer.parseInt(strArrayDeadline[0]);
        int intMonth = Integer.parseInt(strArrayDeadline[1]);
        int intDay = Integer.parseInt(strArrayDeadline[2]);

        DatePickerDialog dialog;
        if(intYear == nowYear && intMonth == nowMonth && intDay == nowDayOfMonth){
            dialog = new DatePickerDialog(TaskEditActivity.this, new DatePickerDialogDateSetListener(), nowYear, nowMonth, nowDayOfMonth);
        }else {
            dialog = new DatePickerDialog(TaskEditActivity.this, new DatePickerDialogDateSetListener(), intYear, intMonth -1, intDay);
        }
        dialog.show();
    }

    private class DatePickerDialogDateSetListener implements DatePickerDialog.OnDateSetListener{
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth){
            TextView tvInputDeadline = findViewById(R.id.tvInputDeadline);
            date = year + "-" + (month + 1) + "-" + dayOfMonth;
            try {
                taskDate = sdFormat.parse(date);
                date = sdFormat.format(taskDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tvInputDeadline.setText(date);
        }
    }
}