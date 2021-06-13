package local.hal.an91.android.todo;

import android.widget.AdapterView;

import java.util.List;
import java.util.Map;

public class Task {
    private long _id;
    private String _name;
    private String _deadline;
    private int _done;
    private String _note;
    private List<Map<String, String>> _listData;

    public long getId(){
        return _id;
    }
    public void setId(long id){
        _id = id;
    }
    public String getName(){
        return _name;
    }
    public void setName(String name){
        _name = name;
    }
    public String getDeadline(){
        return _deadline;
    }
    public void setDeadline(String deadline){
        _deadline = deadline;
    }
    public int getDone(){return _done;}
    public void setDone(int done){ _done = done;}
    public String getNote(){return _note;}
    public void setNote(String note){
        _note = note;
    }
}
