package com.example.countdownextra;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public class DatabaseAdapter {
    private static final String DATABASE_NAME = "database.db";
    private static final String ok="OK";
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_CREATE = "create table TIMES( ID integer primary key autoincrement, date TEXT, HOURS INTEGER, MINUTES  INTEGER,SECONDS  INTEGER,MILLISECONDS INTEGER); ";
    public static SQLiteDatabase db;
    private final Context context;
    private static DataBaseHelper dbHelper;
    public  DatabaseAdapter(Context _context)
    {
        context = _context;
        dbHelper = new DataBaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public  DatabaseAdapter open() throws SQLException
    {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        db.close();
    }
    // method to insert a record in Table
    public String insertEntry(String date, Integer hour, Integer minute, Integer second, Integer millisecond)
    {
        try {
            ContentValues newValues = new ContentValues();
            newValues.put("date", date);
            newValues.put("HOURS", hour);
            newValues.put("FIRSTNAME", minute);
            newValues.put("LASTNAME", second);
            newValues.put("USERNAME", millisecond);
            db = dbHelper.getWritableDatabase();
            long result=db.insert("TIMES", null, newValues);
            System.out.print(result);
            Toast.makeText(context, "Time Info Saved", Toast.LENGTH_LONG).show();
        }catch(Exception ex) {
            System.out.println("Exceptions " +ex);
            Log.e("Note", "One row entered");
        }
        return ok;
    }
    // method to delete a Record with less minutes tham specified
    public int deleteEntry(Integer minute)
    {
        String where="MINUTES < ?";
        int numberOFEntriesDeleted = db.delete("TIMES", where, new String[]{minute.toString()}) ;
        Toast.makeText(context, "Number fo Entry Deleted Successfully : "+numberOFEntriesDeleted, Toast.LENGTH_LONG).show();
        return numberOFEntriesDeleted;
    }
    // method to delete a Record with less minutes tham specified
    public int deleteEntry(String date)
    {
        String where="date < ?";
        int numberOFEntriesDeleted = db.delete("TIMES", where, new String[]{"DateTime(" + date + ")"}) ;
        Toast.makeText(context, "Number fo Entry Deleted Successfully : "+numberOFEntriesDeleted, Toast.LENGTH_LONG).show();
        return numberOFEntriesDeleted;
    }

}