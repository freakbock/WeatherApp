package com.example.weatherapp;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherAPI {
    private static String API = "a0869952-692a-4377-bd7c-cb4e2614f69c";

    public static void getWeatherData(String apIUrl, final WeatherCallback callback){
        new AsyncTask<Void, Void, String>(){

            @Override
            protected String doInBackground(Void... voids) {

                try
                {
                    URL url = new URL(apIUrl);
                    HttpURLConnection urlConnection =(HttpURLConnection) url.openConnection();
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line).append("\n");
                    }
                    return stringBuilder.toString();

                }
                catch (IOException e){
                    Log.e("WeatherAPI", "Error retrieving weather data: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String response){
                if(response!=null){
                    try{
                        JSONObject jsonObject = new JSONObject(response);

                        String temperature = jsonObject.getJSONObject("fact").getString("temp");
                        //get data
                    }
                    catch (JSONException e){
                        Log.e("WeatherAPI", "Error parsing weather data: " + e.getMessage());
                        callback.onFailure("Error parsin weather data");
                    }
                }
                else{
                    callback.onFailure("Error retreiving weather data");
                }
            }
        }.execute();
    }
    public interface WeatherCallback{
        void onSuccess(String temperature, String condition);
        void onFailure(String errorMessage);
    }
}
