package com.example.weatherapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImageTask {

    public static void DownloadImageFromUrl(String url, InputStreamCallback callback){
        new AsyncTask<String, Void, InputStream>(){

            @Override
            protected InputStream doInBackground(String... urls) {
                String imageUrl = url;
                Bitmap bitmap = null;

                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    return connection.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(InputStream result){
                if(result!=null){
                    callback.onBitmapLoaded(result);
                }
            }
        }.execute();
    }

    public interface InputStreamCallback{
        void onBitmapLoaded(InputStream bitmap);
    }
}
