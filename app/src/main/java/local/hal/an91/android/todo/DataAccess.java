package local.hal.an91.android.todo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataAccess {
    public static Cursor findAll(SQLiteDatabase db){
        String sql = "SELECT * FROM (SELECT * FROM tasks WHERE done = 0 ORDER BY deadline ASC)" +
                     "UNION ALL " +
                     "SELECT * FROM (SELECT * FROM tasks WHERE done = 1 ORDER BY deadline DESC)";
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }
    public static Task findByPK(SQLiteDatabase db, long id){
        String sql = "SELECT * FROM tasks WHERE _id = " + id;
        Cursor cursor = db.rawQuery(sql, null);
        Task result = null;
        if(cursor.moveToFirst()){
            int idxName = cursor.getColumnIndex("name");
            int idxDeadline = cursor.getColumnIndex("deadline");
            int idxDone = cursor.getColumnIndex("done");
            int idxNote = cursor.getColumnIndex("note");

            String name = cursor.getString(idxName);
            String deadline = cursor.getString(idxDeadline);
            String done = cursor.getString(idxDone);
            int intDone = Integer.parseInt(done);
            String note = cursor.getString(idxNote);

            result = new Task();
            result.setId(id);
            result.setName(name);
            result.setDeadline(deadline);
            result.setDone(intDone);
            result.setNote(note);
        }
        return result;
    }

    public static Cursor search(SQLiteDatabase db, String searchWord){
        String sql = "SELECT * FROM tasks WHERE name LIKE ?";
        String[] selectionArgs = {searchWord};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        return cursor;
    }
    public static int update(SQLiteDatabase db, long id, String name, String deadline, int done, String note){
        String sql = "UPDATE tasks SET name = ?, deadline = ?, done = ?, note = ? WHERE _id = ?";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1, name);
        stmt.bindString(2, deadline);
        stmt.bindLong(3, done);
        stmt.bindString(4, note);
        stmt.bindLong(5, id);
        int result = stmt.executeUpdateDelete();
        return result;
    }
    public static int updateDone(SQLiteDatabase db, long _id, int done){
        String sql = "UPDATE tasks SET done = ? WHERE _id = ?";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1, done);
        stmt.bindLong(2, _id);
        int result = stmt.executeUpdateDelete();
        return result;
    }

    public static long insert(SQLiteDatabase db, String name, String deadline, int done, String note){
        String sql = "INSERT INTO tasks (name, deadline, done, note) VALUES (?,?,?,?)";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1, name);
        stmt.bindString(2, deadline);
        stmt.bindLong(3, done);
        stmt.bindString(4, note);
        long id = stmt.executeInsert();
        return id;
    }
    public static int delete(SQLiteDatabase db, long id){
        String sql = "DELETE FROM tasks WHERE _id = ?";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1, id);
        int result = stmt.executeUpdateDelete();
        return result;
    }

    public static Cursor sortASC(SQLiteDatabase db, String done){
        String sql = "SELECT * FROM tasks WHERE done = ? ORDER BY deadline ASC";
        String[] selectionArgs = {done};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        return cursor;
    }
    public static Cursor sortDESC(SQLiteDatabase db, String done){
        String sql = "SELECT * FROM tasks WHERE done = ? ORDER BY deadline DESC";
        String[] selectionArgs = {done};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        return cursor;
    }
}
