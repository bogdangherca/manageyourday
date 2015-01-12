package com.example.mycalendaractivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "ManagerDay2.db";
    private static final String SQL_CREATE_ENTRIES =
    	    "CREATE TABLE " + "Tasks" + " (" +
    	    "checkTask" + " int," +
    	    "subject" + " varchar(255)," +
    	    "description" + " varchar(255)," +
    	    "date" + " varchar(255)," +
    	    "hour" + " varchar(255)," +
    	    "importance" + " varchar(255)" + " )";
	
	
	public DatabaseManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
