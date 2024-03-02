package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static List<WeatherOfDay> weatherDays = new ArrayList<>();
    public static List<WeatherOfHour> weatherHours = new ArrayList<>();

    TextView tempTV;
    TextView conditionTV;
    ConstraintLayout parent;
    LinearLayout parent_days;
    LinearLayout parent_hours;

    LinearLayout menu_hours;
    LinearLayout menu_days;

    boolean isHour = false;

    private NetworkChangeReceiver networkChangeReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parent = findViewById(R.id.parent);
        parent_days = findViewById(R.id.parent_days);
        parent_hours = findViewById(R.id.parent_hours);
        tempTV = findViewById(R.id.temp);
        conditionTV =findViewById(R.id.overcast);

        menu_days = findViewById(R.id.menu_days);
        menu_hours = findViewById(R.id.menu_hours);

        SharedPreferences prefs = getSharedPreferences("myprefs", MODE_PRIVATE);
        LoadBackgroundData(
                prefs.getInt("temp", 0),
                prefs.getString("condition", ""),
                prefs.getString("daytime", ""));

        networkChangeReceiver = new NetworkChangeReceiver();
        LoadData();
    }

    @Override
    protected void onResume(){
        super.onResume();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    protected  void onPause(){
        super.onPause();

        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onBackPressed() {
        if(isHour){
            LoadDays();
            isHour = false;
        }
        else{
            super.onBackPressed();
        }
    }

    public void LoadData(){
        WeatherAPI.getWeatherData("https://api.weather.yandex.ru/v2/forecast?limit=7&lang=ru_RU&hours=true&lat=58.0105&lon=56.2502&extra=true",
                new WeatherAPI.WeatherCallback() {
                    @Override
                    public void onSuccess(int temp, String condition, String daytime) {

                        WeatherDataSource wds = new WeatherDataSource(MainActivity.this);
                        wds.clearWeatherHourData();
                        wds.clearWeatherDayData();
                        for(WeatherOfDay data : weatherDays){
                            wds.insertWeatherDayData(data.date, data.temp_avg, data.condition);
                        }
                        for(WeatherOfHour data : weatherHours){
                            wds.insertWeatherHourData(data.date, data.hour, data.temp, data.icon);
                        }

                        SharedPreferences pref = getSharedPreferences("myprefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("condition", condition);
                        editor.putString("daytime", daytime);
                        editor.putInt("temp", temp);
                        editor.commit();

                        LoadBackgroundData(temp, condition, daytime);
                        LoadDays();
                    }

                    private boolean isWeatherDayDataLoaded = false;
                    private boolean isWeatherHourDataLoaded = false;
                    @Override
                    public void onFailure(String errorMessage) {

                        if(getSharedPreferences("myprefs", MODE_PRIVATE).contains("temp")){

                            WeatherDataSource wds = new WeatherDataSource(MainActivity.this);
                            wds.getAllWeatherDayData(new WeatherDataSource.DatabaseCallback() {
                                @Override
                                public void onTaskComplete(Long result) {

                                }

                                @Override
                                public void onCursorLoaded(Cursor cursor) {
                                    Cursor cursorDay = cursor;

                                    if(cursorDay.moveToFirst())
                                    {
                                        do{
                                            WeatherOfDay item = new WeatherOfDay(
                                                    cursorDay.getString(1),
                                                    cursorDay.getInt(2),
                                                    cursorDay.getString(3)
                                            );

                                            weatherDays.add(item);
                                        }
                                        while(cursorDay.moveToNext());
                                    }
                                    isWeatherDayDataLoaded = true;
                                    if(isWeatherHourDataLoaded){
                                        LoadDays();

                                        isWeatherDayDataLoaded = false;
                                        isWeatherHourDataLoaded = false;
                                    }
                                }
                            });
                            wds.getAllWeatherHourData(new WeatherDataSource.DatabaseCallback() {
                                @Override
                                public void onTaskComplete(Long result) {

                                }

                                @Override
                                public void onCursorLoaded(Cursor cursor) {
                                    Cursor cursorHour = cursor;
                                    if(cursorHour.moveToFirst())
                                    {
                                        do{
                                            WeatherOfHour item = new WeatherOfHour(
                                                    cursorHour.getString(1),
                                                    cursorHour.getInt(2),
                                                    cursorHour.getInt(3),
                                                    cursorHour.getString(4)
                                            );

                                            weatherHours.add(item);
                                        }
                                        while(cursorHour.moveToNext());
                                    }

                                    isWeatherHourDataLoaded = true;
                                    if(isWeatherDayDataLoaded){
                                        LoadDays();

                                        isWeatherDayDataLoaded = false;
                                        isWeatherHourDataLoaded = false;
                                    }
                                }
                            });

                            Toast.makeText(MainActivity.this, "Не удалось подключиться. Отображены старые данные", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Не удалось выполнить запрос", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }

    public void LoadBackgroundData(int temp, String condition, String daytime){

        tempTV.setText(String.valueOf(temp)+"°");
        conditionTV.setText(condition.substring(0,1).toUpperCase() + condition.substring(1));


        if(daytime.equals("d")){
            parent.setBackgroundResource(R.drawable.frame_2);
        }
        else{
            parent.setBackgroundResource(R.drawable.frame_1);
        }

    }


    public void LoadDays(){

        menu_days.setVisibility(View.VISIBLE);
        menu_hours.setVisibility(View.GONE);

        parent_days.removeAllViews();

        for(WeatherOfDay weatherOfDay : weatherDays){

            RelativeLayout ll = new RelativeLayout(MainActivity.this);
            RelativeLayout.LayoutParams ll_params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    180
            );
            ll.setLayoutParams(ll_params);

            TextView day = new TextView(MainActivity.this);
            RelativeLayout.LayoutParams day_params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            day_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            day.setLayoutParams(day_params);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date;
            try {
                date = dateFormat.parse(weatherOfDay.date);
            } catch (ParseException e){throw new RuntimeException(e);}
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String dayOfWeekString;
            switch (dayOfWeek) {
                case Calendar.SUNDAY:
                    dayOfWeekString = "Sunday";
                    break;
                case Calendar.MONDAY:
                    dayOfWeekString = "Monday";
                    break;
                case Calendar.TUESDAY:
                    dayOfWeekString = "Tuesday";
                    break;
                case Calendar.WEDNESDAY:
                    dayOfWeekString = "Wednesday";
                    break;
                case Calendar.THURSDAY:
                    dayOfWeekString = "Thursday";
                    break;
                case Calendar.FRIDAY:
                    dayOfWeekString = "Friday";
                    break;
                case Calendar.SATURDAY:
                    dayOfWeekString = "Saturday";
                    break;
                default:
                    dayOfWeekString = "Week";
                    break;
            }

            day.setText(dayOfWeekString);
            day.setTextColor(getColor(R.color.white));
            day.setTextSize(20);
            day.setGravity(Gravity.LEFT);

            TextView dayTemp =new TextView(MainActivity.this);
            RelativeLayout.LayoutParams dayTemp_params= new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dayTemp_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            dayTemp.setLayoutParams(dayTemp_params);
            dayTemp.setText(String.valueOf(weatherOfDay.temp_avg) + "°");
            dayTemp.setTextColor(getColor(R.color.white));
            dayTemp.setTextSize(20);
            dayTemp.setGravity(Gravity.RIGHT);


            TextView dayCondition =new TextView(MainActivity.this);
            RelativeLayout.LayoutParams dayCondition_params= new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dayCondition_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            dayCondition_params.setMargins(0,70,0,0);
            dayCondition.setLayoutParams(dayCondition_params);

            String conditionText = weatherOfDay.condition;
            conditionText = conditionText.substring(0,1).toUpperCase() + conditionText.substring(1);

            dayCondition.setText(conditionText);
            dayCondition.setTextColor(getColor(R.color.gray));
            dayCondition.setTextSize(20);
            dayCondition.setGravity(Gravity.RIGHT);


            LinearLayout line = new LinearLayout(MainActivity.this);
            LinearLayout.LayoutParams line_params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    2
            );
            line_params.setMargins(0,0,0,10);
            line.setLayoutParams(line_params);
            line.setBackgroundColor(getColor(R.color.white));

            ll.addView(day);
            ll.addView(dayTemp);
            ll.addView(dayCondition);

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LoadHours(weatherOfDay.date, dayOfWeekString);

                }
            });


            parent_days.addView(ll);
            parent_days.addView(line);
        }
    }

    public void BackToDays(View v){
        isHour = false;
        LoadDays();
    }

    public void LoadHours(String date, String dayOfWeekString){

        isHour = true;
        menu_hours.setVisibility(View.VISIBLE);
        menu_days.setVisibility(View.GONE);

        TextView dayOfWeek = findViewById(R.id.dayOfWeek);
        dayOfWeek.setText(dayOfWeekString+ " ▼");

        parent_hours.removeAllViews();;
        for(WeatherOfHour weatherOfHour : weatherHours){
            if(weatherOfHour.date.equals(date)){

                LinearLayout ll = new LinearLayout(MainActivity.this);
                LinearLayout.LayoutParams ll_params = new LinearLayout.LayoutParams(
                        160,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                ll.setGravity(Gravity.CENTER_HORIZONTAL);
                ll.setLayoutParams(ll_params);
                ll.setOrientation(LinearLayout.VERTICAL);

                TextView hour = new TextView(MainActivity.this);
                LinearLayout.LayoutParams hour_params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                hour.setLayoutParams(hour_params);
                hour.setText(String.valueOf(weatherOfHour.hour));
                hour.setTextColor(getColor(R.color.white));
                hour.setTextSize(18);

                ImageView icon = new ImageView(MainActivity.this);
                LinearLayout.LayoutParams icon_params = new LinearLayout.LayoutParams(
                        80
                        ,80
                );
                icon.setLayoutParams(icon_params);


                File file = this.getFileStreamPath(weatherOfHour.icon);
                if(file.exists()){
                    Bitmap bitmap = loadBitmapFromFile(MainActivity.this, weatherOfHour.icon);
                    icon.setImageBitmap(bitmap);
                }
                else {
                    DownloadImageTask.DownloadImageFromUrl("https://yastatic.net/weather/i/icons/funky/dark/" + weatherOfHour.icon + ".svg",
                            new DownloadImageTask.InputStreamCallback() {
                                @Override
                                public void onBitmapLoaded(InputStream is) {
                                    if (is != null) {
                                        try {
                                            SVG svg = SVG.getFromInputStream(is);
                                            PictureDrawable drawable = new PictureDrawable(svg.renderToPicture());
                                            saveFile(MainActivity.this, weatherOfHour.icon, drawable);
                                            icon.setImageDrawable(drawable);

                                        } catch (SVGParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });

                }

                TextView temp = new TextView(MainActivity.this);
                LinearLayout.LayoutParams temp_params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                temp.setLayoutParams(temp_params);
                temp.setText(String.valueOf(weatherOfHour.temp) + "°");
                temp.setTextColor(getColor(R.color.white));
                temp.setTextSize(18);


                ll.addView(hour);
                ll.addView(icon);
                ll.addView(temp);

                parent_hours.addView(ll);
            }
        }
    }
    public static boolean saveFile(Context context, String filename, PictureDrawable pictureDrawable){
        FileOutputStream fos = null;
        try{
            Bitmap bitmap = pictureDrawableToBitmap(pictureDrawable);
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return true;
        }
        catch (IOException e){
            Log.e("Ошибка сохранения", "Не удалось сохранить файл: "+ e.getMessage());
            return false;
        }
        finally {
            if(fos != null)
                try{
                    fos.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
        }
    }

    private static Bitmap pictureDrawableToBitmap(PictureDrawable pictureDrawable) {
        Picture picture = pictureDrawable.getPicture();
        int width = pictureDrawable.getIntrinsicWidth();
        int height = pictureDrawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(picture);
        return bitmap;
    }

    public static Bitmap loadBitmapFromFile(Context context, String filename){
        try{
            FileInputStream fis = context.openFileInput(filename);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();;
            return bitmap;
        }
        catch (FileNotFoundException e){
            Log.e("Ошибка", "Файл не найден: " + e.getMessage());
        }
        catch (Exception e) {
            Log.e("Ошибка", e.getMessage());
        }
        return null;
    }
}