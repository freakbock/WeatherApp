package com.example.weatherapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDPHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 1;

    public WeatherDPHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_WEATHER_DAY_TABLE = "CREATE TABLE " +
                "WeatherDay (" +
                    "weather_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT NOT NULL, " +
                    "temp_avg INTEGER, " +
                    "condition TEXT" +
                    ");";

        final String SQL_CREATE_WEATHER_HOUR_TABLE = "CREATE TABLE " +
                "WeatherHour (" +
                "weather_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT NOT NULL, " +
                "hour INTEGER, " +
                "temp_avg INTEGER, " +
                "icon TEXT);";

        db.execSQL(SQL_CREATE_WEATHER_DAY_TABLE);
        db.execSQL(SQL_CREATE_WEATHER_HOUR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS WeatherDay");
        db.execSQL("DROP TABLE IF EXISTS WeatherHour");
        onCreate(db);
    }
}
