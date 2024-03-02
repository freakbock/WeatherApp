package com.example.weatherapp;

public class WeatherOfDay {
    public String date;
    public int temp_avg;
    public String condition;

    public WeatherOfDay(String date, int temp_avg, String condition){
        this.date = date;
        this.temp_avg = temp_avg;
        this.condition = condition;
    }
}
