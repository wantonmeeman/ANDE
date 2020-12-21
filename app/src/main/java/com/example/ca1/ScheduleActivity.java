package com.example.ca1;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
    SimpleDateFormat tfhrTimeFormat = new SimpleDateFormat("HHmm",Locale.ENGLISH);
    //SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    Calendar calendar = Calendar.getInstance();
    ArrayList<Alarm> ArrListAlarm;


    String currentDate = dateFormat.format(new Date());
    String currentTime = tfhrTimeFormat.format(new Date());
    String currentDay = dayFormat.format(calendar.getTime());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todays_tasks_act);
        this.getSupportActionBar().hide();

        ArrListAlarm = new ArrayList<Alarm>();
        long JSONtime;
        JSONObject jObject;
        String JSONtitle;
        String JSONdesc;
        long tempTime;
        String tempTitle;
        String tempDesc;

        try {//Read File

            FileInputStream fin = openFileInput("JSON STORAGE");
            int c;
            String temp = "";

            while ((c = fin.read()) != -1) {
                temp = temp + Character.toString((char) c);
            }
            fin.close();

            //Get JSON Object(which is an array)
            JSONObject pjObject = new JSONObject(temp);
            JSONArray jArray = pjObject.getJSONArray("Data");

            //Get the calendar Object today's date.
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            //Get Start and end of date.
            long startOfDay = cal.getTimeInMillis() / 1000;
            long endOfDay = startOfDay + 86400;

            Log.i("Time", Long.toString(cal.getTimeInMillis()));

            for(int i = 0; jArray.length() > i;i++) {
                for(int j = i+1; jArray.length() > j;j++) {
                    if(jArray.getJSONObject(i).getLong("time") > jArray.getJSONObject(j).getLong("time"))
                    {
                        tempTime = jArray.getJSONObject(i).getLong("time");
                        tempTitle = jArray.getJSONObject(i).getString("title");
                        tempDesc = jArray.getJSONObject(i).getString("description");

                        jArray.getJSONObject(i).put("time",jArray.getJSONObject(j).getLong("time"));
                        jArray.getJSONObject(i).put("title",jArray.getJSONObject(j).getString("title"));
                        jArray.getJSONObject(i).put("description",jArray.getJSONObject(j).getString("description"));

                        jArray.getJSONObject(j).put("time",tempTime);
                        jArray.getJSONObject(j).put("title",tempTitle);
                        jArray.getJSONObject(j).put("description",tempDesc);
                    }
                }
            }

            //Loop to populate ArrListAlarm
            for(int i = 0; jArray.length() > i;i++) {

                jObject = jArray.getJSONObject(i);
                JSONtime = jObject.getInt("time");
                Log.i("Start of Day",Long.toString(startOfDay));
                Log.i("End of Day",Long.toString(endOfDay));
                Log.i("JSON TIME",Long.toString(JSONtime));

                if(startOfDay < JSONtime && endOfDay > JSONtime) {//Get only today's date
                    JSONtitle = jObject.getString("title");
                    JSONdesc = jObject.getString("description");
                    Log.i("Loop","Loop"+i);
                    ArrListAlarm.add(new Alarm(JSONtitle, JSONdesc, "", JSONtime * 1000L));
                }

            }
            RecyclerView myrv = (RecyclerView) findViewById(R.id.recyclerViewTask);

            //Gets the Adapter from the JAVA file
            RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(this,ArrListAlarm);

            //Set Layout for the RecyclerView
            myrv.setLayoutManager(new LinearLayoutManager(this));

            //Set an adapter for the View
            myrv.setAdapter(myAdapter);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("Error",e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Error",e.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("Error",e.toString());
        }

    }
        //@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        switch(item.getItemId()){
            case R.id.location:
                return true;

            case R.id.calendar:
                return true;
            case R.id.home:
                return true;
            case R.id.qr:
                return true;
            case R.id.settings:
                return true;

        }
        return false;
    }




}