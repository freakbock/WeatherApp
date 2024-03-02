package com.example.weatherapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class WeatherDataSource {
    private SQLiteDatabase mDatabase;
    private WeatherDPHelper dbHelper;

    public WeatherDataSource(Context context){
        dbHelper = new WeatherDPHelper(context);
        mDatabase = dbHelper.getWritableDatabase();
    }

    public void CloseConnection(){
        if(dbHelper!= null){
            mDatabase.close();
            dbHelper.close();
        }
    }

    public void insertWeatherDayData(String date, int temp_avg, String condition){
        new InsertWeatherDayDataTask().execute(date, temp_avg, condition);
    }

    public void insertWeatherHourData(String date, int hour, int temp, String icon){
        new InsertWeatherHourDataTask().execute(date, hour, temp, icon);
    }

    public void getAllWeatherDayData(DatabaseCallback callback){
        new GetAllWeatherDayDataTask(callback).execute();
    }

    public void getAllWeatherHourData(DatabaseCallback callback){
        new GetAllWeatherHourDataTask(callback).execute();
    }

    public void clearWeatherDayData(){
        new ClearWeatherDayDataTask().execute();
    }

    public void clearWeatherHourData(){
        new ClearWeatherHourDataTask().execute();
    }

    private class InsertWeatherDayDataTask extends AsyncTask<Object, Void, Long> {
        @Override
        protected Long doInBackground(Object... params) {
            String date = (String) params[0];
            int temp_avg = (int) params[1];
            String condition = (String) params[2];

            ContentValues values = new ContentValues();
            values.put("date", date);
            values.put("temp_avg", temp_avg);
            values.put("condition", condition);

            return mDatabase.insert("WeatherDay", null, values);
        }
    }

    private class InsertWeatherHourDataTask extends AsyncTask<Object, Void, Long> {
        @Override
        protected Long doInBackground(Object... params) {
            String date = (String) params[0];
            int hour = (int) params[1];
            int temp = (int) params[2];
            String icon = (String) params[3];

            ContentValues values = new ContentValues();
            values.put("date", date);
            values.put("hour", hour);
            values.put("temp_avg", temp);
            values.put("icon", icon);

            return mDatabase.insert("WeatherHour", null, values);
        }
    }

    private class GetAllWeatherDayDataTask extends AsyncTask<Void, Void, Cursor> {
        private DatabaseCallback callback;

        public GetAllWeatherDayDataTask(DatabaseCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            return mDatabase.query("WeatherDay", null, null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            callback.onCursorLoaded(cursor);
        }
    }

    private class GetAllWeatherHourDataTask extends AsyncTask<Void, Void, Cursor> {
        private DatabaseCallback callback;

        public GetAllWeatherHourDataTask(DatabaseCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            return mDatabase.query("WeatherHour", null, null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            callback.onCursorLoaded(cursor);
        }
    }

    private class ClearWeatherDayDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            mDatabase.execSQL("DELETE FROM WeatherDay");
            return null;
        }
    }

    private class ClearWeatherHourDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            mDatabase.execSQL("DELETE FROM WeatherHour");
            return null;
        }
    }

    public interface DatabaseCallback {
        void onTaskComplete(Long result);
        void onCursorLoaded(Cursor cursor);
    }
}
