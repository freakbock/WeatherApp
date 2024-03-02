package com.example.weatherapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WeatherDataSource {
    private SQLiteDatabase mDatabase;

    public WeatherDataSource(Context context){
        WeatherDPHelper dbHelper = new WeatherDPHelper(context);
        mDatabase = dbHelper.getWritableDatabase();
    }

    public long insertWeatherDayData(String date, int temp_avg, String condition){
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("temp_avg", temp_avg);
        values.put("condition", condition);

        return mDatabase.insert("WeatherDay", null, values);
    }


    public long insertWeatherHourData(String date,int hour, int temp, String icon){
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("hour", temp);
        values.put("temp_avg", temp);
        values.put("icon", icon);

        return mDatabase.insert("WeatherHour", null, values);
    }

    public Cursor getAllWeatherDayData(){
        return mDatabase.query("WeatherDay",null, null, null, null, null, null);
    }
    public Cursor getAllWeatherHourData(){
        return mDatabase.query("WeatherHour",null, null, null, null, null, null);
    }

    public void clearWeatherDayData(){
        mDatabase.execSQL("DELETE FROM WeatherDay");
    }

    public void clearWeatherHourData(){
        mDatabase.execSQL("DELETE FROM WeatherHour");
    }
}
