package com.example.ca1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
    SimpleDateFormat tfhrTimeFormat = new SimpleDateFormat("HHmm",Locale.ENGLISH);
    //SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Calendar calendar = Calendar.getInstance();

    TextView txtTaskTitle;
    TextView txtDate;
    TextView txtDay;
    TextView txtEx;
    TextView txtTaskTime;
    ArrayList<Alarm> ArrListAlarm;

    String currentDate = dateFormat.format(new Date());
    String currentTime = tfhrTimeFormat.format(new Date());
    String currentDay = dayFormat.format(calendar.getTime());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getSupportActionBar().hide();//Remove Title, probably not very good

        BottomNavigationView botNavView;
        FileOutputStream fOut = null;
        txtDate = (TextView) findViewById(R.id.date);
        txtDay = (TextView) findViewById(R.id.day);
        txtTaskTitle = (TextView) findViewById(R.id.taskTitle);
        txtTaskTime = (TextView) findViewById(R.id.taskTime);

        txtDate.setText(currentDate);
        txtDay.setText(currentDay);
        txtTaskTime.setText(currentTime);

        try {//Make new file,
            fOut = openFileOutput("JSON STORAGE", Context.MODE_PRIVATE);
            String str = "{'Data':[{" +
                    "time:" + System.currentTimeMillis() / 1000L +
                    ",title:" + "'Eat hot chip'" +
                    ",description:" + "'WHAT THE FUCK DID YOU JUST SAY ABOUT ME YOU LITTLE BITCH, ILL HAVE YOU THAT I GRADUATED TOP OF MY CLASS IN THE NAVY SEALS AND HAVE OVER 400 CONFIRMED KILLS'" +
                    "},{" +
                    "time: 1" +
                    ",title:" + "'Dont eat Hot chip'" +
                    ",description:" + "'WHAT THE FUCK DID YOU JUST SAY ABOUT ME YOU LITTLE BITCH, ILL HAVE YOU THAT I GRADUATED TOP OF MY CLASS IN THE NAVY SEALS AND HAVE OVER 400 CONFIRMED KILLS'" +
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
            String temp="";

            while( (c = fin.read()) != -1){
                temp = temp + Character.toString((char)c);
            }
            fin.close();
            JSONObject jObject;
            JSONObject pjObject;
            JSONArray jArray;

            ArrListAlarm = new ArrayList<>();

            pjObject = new JSONObject(temp);
            jArray = pjObject.getJSONArray("Data");
            jObject = jArray.getJSONObject(1);

            String JSONtime = tfhrTimeFormat.format(jObject.getInt("time")*1000L);
            String JSONtitle = jObject.getString("title");

            ArrListAlarm.add(new Alarm(JSONtitle,"","", (int) (jObject.getInt("time")*1000L)));


//            txtTaskTitle.setText(JSONtitle);
//            txtTaskTime.setText(JSONtime);

        } catch (IOException  | JSONException e) {
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }


        Button button = findViewById(R.id.addNewTask);

        createNotificationChannel();
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,ReminderBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,intent,0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            long timeAtButtonClick = System.currentTimeMillis();

            long tenSecondsInMillis = 5000 ;
            //No idea how this works alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(System.currentTimeMillis()+2,pendingIntent.getActivity(MainActivity.this,0,intent,0)),pendingIntent);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,timeAtButtonClick ,2*1000,pendingIntent);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,timeAtButtonClick + tenSecondsInMillis,pendingIntent);
            Toast.makeText(this,"Reminder!",Toast.LENGTH_LONG).show();
        });


        botNavView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
    }

    public void createNotificationChannel(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "LemubitReminderChannel";
            String description = "Channel for Lemubit Reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Alarm",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    //@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        switch(id){
            case R.id.location:
                return true;
            case R.id.calendar:
                return true;
            case R.id.qrscanner:
                return true;
            case R.id.msg:
                return true;
            case R.id.settings:
                //txt.setText(getResources().getText(R.string.page_5));
                return true;

        }
        return false;
    }




}