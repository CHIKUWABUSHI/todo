package local.hal.an91.android.todo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private RecyclerView _lvTaskList;
    SQLiteDatabase db;
    private DatabaseHelper _helper;
    private int btnFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _lvTaskList = findViewById(R.id.lvTaskList);
        LinearLayoutManager layout = new LinearLayoutManager(MainActivity.this);
        _lvTaskList.setLayoutManager(layout);
        RecycleListAdapter adapter = new RecycleListAdapter(null);
        _lvTaskList.setAdapter(adapter);
        _helper = new DatabaseHelper(getApplicationContext());

        DividerItemDecoration decorator = new DividerItemDecoration(MainActivity.this, layout.getOrientation());
        _lvTaskList.addItemDecoration(decorator);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadList();
    }

    @Override
    protected void onDestroy() {
        _helper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.btnNew:
                Intent intent = new Intent(MainActivity.this, TaskEditActivity.class);
                intent.putExtra("mode", Consts.MODE_INSERT);
                startActivity(intent);
                return true;

            case R.id.btnSearch:
                TextView _tvSearch = findViewById(R.id.btnSearch);
                String strSearch = _tvSearch.getText().toString();
                db = _helper.getWritableDatabase();
                Cursor cursor = DataAccess.search(db, strSearch);
                RecycleListAdapter adapter = new RecycleListAdapter(cursor);
                _lvTaskList.setAdapter(adapter);
                btnFlag = 3;
                return true;

            case R.id.btnSortOkTask:
                db = _helper.getWritableDatabase();
                cursor = DataAccess.sortASC(db, "0");
                adapter = new RecycleListAdapter(cursor);
                _lvTaskList.setAdapter(adapter);
                btnFlag = 2;
                return true;

            case R.id.btnSortNgTask:
                db = _helper.getWritableDatabase();
                cursor = DataAccess.sortDESC(db, "1");
                adapter = new RecycleListAdapter(cursor);
                _lvTaskList.setAdapter(adapter);
                btnFlag = 1;
                return true;

            case R.id.btnSortAll:
                btnFlag = 0;
                reloadList();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void reloadList() {
        switch (btnFlag) {
            case 0:
                SQLiteDatabase db = _helper.getWritableDatabase();
                Cursor cursor = DataAccess.findAll(db);
                RecycleListAdapter adapter = new RecycleListAdapter(cursor);
                _lvTaskList.setAdapter(adapter);
                break;

            case 1:
                db = _helper.getWritableDatabase();
                cursor = DataAccess.sortDESC(db, "1");
                adapter = new RecycleListAdapter(cursor);
                _lvTaskList.setAdapter(adapter);
                break;

            case 2:
                db = _helper.getWritableDatabase();
                cursor = DataAccess.sortASC(db, "0");
                adapter = new RecycleListAdapter(cursor);
                _lvTaskList.setAdapter(adapter);
                break;
        }

    }

    private class RecycleListViewHolder extends RecyclerView.ViewHolder {

        public TextView _tvTaskName;
        public TextView _tvTaskDeadline;
        public CheckBox _cbDone;
        public TextView _tvRemainingDay;

        public RecycleListViewHolder(View itemView) {
            super(itemView);
            _tvTaskName = itemView.findViewById(R.id.tvTaskName);
            _tvTaskDeadline = itemView.findViewById(R.id.tvTaskDeadline);
            _cbDone = itemView.findViewById(R.id.cbDone);
            _tvRemainingDay = itemView.findViewById(R.id.tvRemainingDay);
        }
    }

    private class RecycleListAdapter extends RecyclerView.Adapter<RecycleListViewHolder> {

        private Cursor _cursor;
        Calendar cal = Calendar.getInstance();
        int nowYear = cal.get(Calendar.YEAR);
        int nowMonth = cal.get(Calendar.MONTH);
        int nowDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        public RecycleListAdapter(Cursor cursor) {
            _cursor = cursor;
        }

        @Override
        public RecycleListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View view = inflater.inflate(R.layout.row, parent, false);
            view.findViewById(R.id.cbDone).setOnClickListener(new ItemClickListener());
            view.findViewById(R.id.llList).setOnClickListener(new ItemClickListener());
            view.setOnLongClickListener(new ItemLongClickListener());
            RecycleListViewHolder holder = new RecycleListViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecycleListViewHolder holder, int position) {
            String strName = "";
            String strDeadline = "";
            long id = 0;
            int intDone = 0;
            if (_cursor.moveToPosition(position)) {
                int idxId = _cursor.getColumnIndex("_id");
                int idxName = _cursor.getColumnIndex("name");
                int idxDeadline = _cursor.getColumnIndex("deadline");
                int idxDone = _cursor.getColumnIndex("done");
                id = _cursor.getLong(idxId);
                strName = _cursor.getString(idxName);
                strDeadline = _cursor.getString(idxDeadline);
                intDone = _cursor.getInt(idxDone);
            }

            String[] indexDeadline = strDeadline.split("-", 0);
            int intYear = Integer.parseInt(indexDeadline[0]);
            int intMonth = Integer.parseInt(indexDeadline[1]);
            int intDay = Integer.parseInt(indexDeadline[2]);

            Calendar calDeadline = Calendar.getInstance();
            calDeadline.set(intYear, intMonth, intDay);
            Calendar diffCal = Calendar.getInstance();
            diffCal.set(nowYear, nowMonth + 1, nowDayOfMonth);

            //残り日数
            long remainingDay = (calDeadline.getTimeInMillis() - diffCal.getTimeInMillis()) / (1000 * 24 * 60 * 60);

            //期限判定
            String deadlineMsg = "";

            holder._tvTaskDeadline.setText(strDeadline);

            if (remainingDay < 0) {
                deadlineMsg = "日前";
                remainingDay = Math.abs(remainingDay);
                holder._tvRemainingDay.setText(remainingDay + deadlineMsg);
                if (intDone == 0) {
                    holder._tvTaskDeadline.setTextColor(Color.RED);
                }
            } else if (remainingDay == 0) {
                deadlineMsg = "今日";
                holder._tvRemainingDay.setText(deadlineMsg);
            } else {
                holder._tvRemainingDay.setText(remainingDay + "日");
            }

            if (intDone == 1) {
                holder._cbDone.setChecked(true);
            }

            holder._tvTaskName.setText(strName);
            holder._tvTaskName.setTag(id);
            holder._cbDone.setTag(id);

        }

        @Override
        public int getItemCount() {
            return _cursor.getCount();
        }

        private class ItemClickListener implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                TextView _tvTaskName = view.findViewById(R.id.tvTaskName);
                CheckBox _cbDone = view.findViewById(R.id.cbDone);

                switch (view.getId()) {
                    case R.id.llList:
                        Long idNo = (Long) _tvTaskName.getTag();
                        Intent intent = new Intent(getApplicationContext(), TaskEditActivity.class);
                        intent.putExtra("mode", Consts.MODE_EDIT);
                        intent.putExtra("idNo", idNo);
                        startActivity(intent);
                        break;

                    case R.id.cbDone:
                        idNo = (Long) _cbDone.getTag();
                        int done = 0;
                        if (_cbDone.isChecked()) {
                            done = 1;
                        }
                        SQLiteDatabase db = _helper.getWritableDatabase();
                        DataAccess.updateDone(db, idNo, done);
                        LinearLayout _llList = findViewById(R.id.llList);
                        TranslateAnimation ta = new TranslateAnimation(0, -_llList.getWidth()*2, 0, 0);
                        ta.setDuration(1);
                        ta.setFillAfter(false);
                        _llList.startAnimation(ta);
                        reloadList();
                        break;
                }
            }
        }

        private class ItemLongClickListener implements View.OnLongClickListener {
            @Override
            public boolean onLongClick(View view) {
                TextView _tvTaskName = view.findViewById(R.id.tvTaskName);
                Long idNo = (Long) _tvTaskName.getTag();
                Bundle extras = new Bundle();
                extras.putLong("id", idNo);
                extras.putBoolean("checkActivity", true);
                DeleteDialogFragment dialog = new DeleteDialogFragment(_helper);
                dialog.setArguments(extras);
                FragmentManager manager = getSupportFragmentManager();
                dialog.show(manager, "deleteDialogFragment");
                return true;
                }
            }
        }
}