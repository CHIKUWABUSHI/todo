package local.hal.an91.android.todo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class DeleteDialogFragment extends DialogFragment{
    private DatabaseHelper _helper;

    public DeleteDialogFragment(DatabaseHelper helper){
        _helper = helper;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Activity parent = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle(R.string.dlg_delete_title);
        builder.setMessage(R.string.dlg_delete_msg);
        builder.setPositiveButton(R.string.dlg_delete_delete, new deleteDialogButtonClickListener());
        builder.setNegativeButton(R.string.dlg_delete_cancel, new deleteDialogButtonClickListener());
        AlertDialog dialog = builder.create();
        return dialog;
    }

    private class deleteDialogButtonClickListener implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which){
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    Activity parent = getActivity();
                    Bundle extras = getArguments();
                    long id = extras.getLong("id");
                    boolean checkActivity = extras.getBoolean("checkActivity");
                    SQLiteDatabase db = _helper.getWritableDatabase();
                    DataAccess.delete(db, id);
                    if(checkActivity) {
                        ((MainActivity)parent).reloadList();
                    }else{
                        parent.finish();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }
}
