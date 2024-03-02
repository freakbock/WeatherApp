package com.example.weatherapp;

public class Weather {
    public int weather_id;
    public String date;
    public int hour;
    public String week;
    public int temp;
    public String condition;
    public String icon;
    public String daytime;

    public Weather(String date,int hour, String week, int temp, String condition, String icon, String daytime){
        this.date = date;
        this.hour = hour;
        this.week = week;
        this.temp = temp;
        this.condition = condition;
        this.icon = icon;
        this.daytime = daytime;
    }
}
