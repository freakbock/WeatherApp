package com.example.weatherapp;

public class WeatherOfDay {
    public int weather_id;
    public String date;
    public int temp_avg;
    public String condition;
    public String icon;

    public WeatherOfDay(String date, int temp_avg, String condition, String icon){
        this.date = date;
        this.temp_avg = temp_avg;
        this.condition = condition;
        this.icon = icon;
    }
}
