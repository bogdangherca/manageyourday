package com.example.mycalendaractivity;

import android.content.Context;

public class DatabaseCreator 
{
    private static DatabaseManager instance;

    public static synchronized DatabaseManager getHelper(Context context)
    {
        if (instance == null)
            instance = new DatabaseManager(context);

        return instance;
    }
}