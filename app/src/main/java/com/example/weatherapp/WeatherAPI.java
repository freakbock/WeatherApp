package com.example.weatherapp;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
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
                    urlConnection.setRequestProperty("X-Yandex-API-Key", API);
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

                        String daytime = jsonObject.getJSONObject("fact").getString("daytime");
                        int temp = jsonObject.getJSONObject("fact").getInt("temp");
                        String condition = jsonObject.getJSONObject("fact").getString("condition");


                        JSONArray forecasts = jsonObject.getJSONArray("forecasts");
                        MainActivity.weatherDays.clear();
                        MainActivity.weatherHours.clear();
                        for(int i = 0; i < forecasts.length(); i++){

                            JSONObject dataForDay = forecasts.getJSONObject(i);
                            String date = dataForDay.getString("date");
                            String dayCondition = dataForDay.getJSONObject("parts").getJSONObject("day").getString("condition");
                            String icon = dataForDay.getJSONObject("parts").getJSONObject("day").getString("icon");

                            JSONObject parts = dataForDay.getJSONObject("parts");
                            JSONObject night = parts.getJSONObject("night");
                            JSONObject morning = parts.getJSONObject("night");
                            JSONObject day = parts.getJSONObject("night");
                            JSONObject evening = parts.getJSONObject("night");
                            int temp_avg = (night.getInt("temp_avg")
                                    + morning.getInt("temp_avg") +
                                    day.getInt("temp_avg") +
                                    evening.getInt("temp_avg")) /4;

                            JSONArray Hours = dataForDay.getJSONArray("hours");
                            for(int j = 0; j < Hours.length(); j++){

                                JSONObject dataForHour = Hours.getJSONObject(j);

                                int hour = dataForHour.getInt("hour");
                                int hourTemp = dataForHour.getInt("temp");
                                String hourIcon = dataForHour.getString("icon");

                                WeatherOfHour weatherOfHour = new WeatherOfHour(date, hour, hourTemp, hourIcon);
                                MainActivity.weatherHours.add(weatherOfHour);

                            }


                            WeatherOfDay weatherOfDay = new WeatherOfDay(date, temp_avg, dayCondition, icon);
                            MainActivity.weatherDays.add(weatherOfDay);

                            callback.onSuccess(temp,condition, daytime);
                        }
                    }
                    catch (JSONException e){
                        Log.e("WeatherAPI", "Error parsing weather data: " + e.getMessage());
                        callback.onFailure("Error parsing weather data");
                    }
                }
                else{
                    callback.onFailure("Error retreiving weather data");
                }
            }
        }.execute();
    }
    public interface WeatherCallback{
        void onSuccess(int temp, String condition, String daytime);
        void onFailure(String errorMessage);
    }
}
