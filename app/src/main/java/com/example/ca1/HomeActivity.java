package com.example.ca1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
    SimpleDateFormat tfhrTimeFormat = new SimpleDateFormat("HHmm",Locale.ENGLISH);
    //SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    Calendar calendar = Calendar.getInstance();

    TextView txtTaskTitle;
    TextView txtDate;
    TextView txtDay;
    TextView txtTaskTime;
    ArrayList<Alarm> ArrListAlarm;

    String currentDate = dateFormat.format(new Date());
    String currentTime = tfhrTimeFormat.format(new Date());
    String currentDay = dayFormat.format(calendar.getTime());

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_act);

        this.getSupportActionBar().hide();//Remove Title, probably not very good

        //Declare Variables
        BottomNavigationView botNavView;
        JSONObject jObject;
        JSONObject pjObject;
        JSONArray jArray;
        long JSONtime;
        String JSONtitle;
        String JSONdesc;
        long tempTime;
        String tempTitle;
        String tempDesc;
        ArrListAlarm = new ArrayList<Alarm>();

        FileOutputStream fOut = null;
        txtDate = (TextView) findViewById(R.id.date);
        txtDay = (TextView) findViewById(R.id.day);
        txtTaskTitle = (TextView) findViewById(R.id.taskTitle);
        txtTaskTime = (TextView) findViewById(R.id.taskTime);

        txtDate.setText(currentDate);
        txtDay.setText(currentDay);

        try {//Make new file,maybe put this in splashscreen
            fOut = openFileOutput("JSON STORAGE", Context.MODE_PRIVATE);
            String str = "{'Data':[{" +
                    "time:" + System.currentTimeMillis() / 1000L +
                    ",title:" + "'Wake up'" +
                    ",description:" + "'Y SEALS AND HAVE OVER 400 CONFIRMED KILLS'" +

                    "},{" +

                    "time: " + ((System.currentTimeMillis() / 1000L)+ 10 * 60) +
                    ",title:" + "'Drink Water'" +
                    ",description:" + "' '" +

                    "},{" +

                    "time: " + ((System.currentTimeMillis() / 1000L)+ 5 * 60) +
                    ",title:" + "'Brush Teeth'" +
                    ",description:" + "''" +

                    "},{" +

                    "time: " + ((System.currentTimeMillis() / 1000L)+ 15 * 60) +
                    ",title:" + "'Eat Breakfast'" +
                    ",description:" + "' '" +

                    "},{" +

                    "time: " + ((System.currentTimeMillis() / 1000L)+ 20 * 60) +
                    ",title:" + "'You have to Scroll to view this!'" +
                    ",description:" + "' '" +

                    "}]}";

            fOut.write(str.getBytes());
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        try {//Read File

            FileInputStream fin = openFileInput("JSON STORAGE");
            int c;
            String temp = "";

            while( (c = fin.read()) != -1){
                temp = temp + Character.toString((char)c);
            }
            fin.close();

            //Get JSON Object(which is an array)
            pjObject = new JSONObject(temp);
            jArray = pjObject.getJSONArray("Data");

            //Get the calendar Object today's date.
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            //Get Start and end of date.
            long startOfDay = cal.getTimeInMillis()/1000;
            long endOfDay = startOfDay + 86400;

            Log.i("Time",Long.toString(cal.getTimeInMillis()));


            //Loop to Sort JSON
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

                    ArrListAlarm.add(new Alarm(JSONtitle, JSONdesc, "", JSONtime * 1000L));
                }

            }


            //This gets the RecyclerView from the XML File
            RecyclerView myrv = (RecyclerView) findViewById(R.id.recyclerViewTask);

            //Gets the Adapter from the JAVA file
            RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(this,ArrListAlarm);

            //Set Layout for the RecyclerView
            myrv.setLayoutManager(new LinearLayoutManager(this));

            //Set an adapter for the View
            myrv.setAdapter(myAdapter);

        } catch (IOException  | JSONException e) {
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }




        createNotificationChannel();


        Button button = findViewById(R.id.addNewTask);

        button.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this,ReminderBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeActivity.this,0,intent,0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            long timeAtButtonClick = System.currentTimeMillis();

            long tenSecondsInMillis = 5000 ;

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,timeAtButtonClick + tenSecondsInMillis,pendingIntent);
            Toast.makeText(this,"Reminder!",Toast.LENGTH_LONG).show();
        });

        Button qrButton = findViewById(R.id.qrScanner);
        qrButton.setOnClickListener(v ->{
            Intent intent = new Intent(this, QRActivity.class);
            startActivity(intent);
        });


        botNavView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        botNavView.getMenu().getItem(2).setChecked(true);//Set Middle(Home) to checked
        botNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            public boolean onNavigationItemSelected(@NonNull MenuItem item){
                switch(item.getItemId()){
                    case R.id.location:

                        return true;
                    case R.id.calendar:
                        Intent intent = new Intent(getApplicationContext(),ScheduleActivity.class);
                        Log.e("Clicked","Location");
                        startActivity(intent);
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
        });

    }

    public void createNotificationChannel(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "LemubitReminderChannel";
            String description = "Channel for Lemubit Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("Alarm",name,importance);
            channel.setDescription(description);
            channel.setImportance(importance);
            channel.enableVibration(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }
    //@Override






}