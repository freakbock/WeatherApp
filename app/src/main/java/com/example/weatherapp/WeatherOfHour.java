package com.example.weatherapp;

public class WeatherOfHour {
    public int weather_id;
    public String date;
    public int hour;
    public int temp;
    public String icon;

    public WeatherOfHour(String date,int hour, int temp, String icon){
        this.date = date;
        this.hour = hour;
        this.temp = temp;
        this.icon = icon;
    }
}
