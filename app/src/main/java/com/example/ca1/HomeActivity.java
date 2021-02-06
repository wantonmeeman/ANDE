package com.example.ca1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {
    //DateFormats to be used
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);

    //Get current Time and Date
    Calendar calendar = Calendar.getInstance();

    TextView txtTaskTitle;
    TextView txtDate;
    TextView txtDay;
    TextView txtTaskTime;
    ArrayList<Alarm> ArrListAlarm;

    String currentDate = dateFormat.format(new Date());
    String currentDay = dayFormat.format(calendar.getTime());

    //This Snippet changes the UI when the user backbtn's
    private int mLastDayNightMode;
    protected void onRestart(){
        super.onRestart();
        if (AppCompatDelegate.getDefaultNightMode() != mLastDayNightMode) {
            recreate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        this.getSupportActionBar().hide();//Remove Title, probably not very good

        //Declare Variables
        ArrListAlarm = new ArrayList<Alarm>();
        String userid = "";
        GoogleSignInAccount gAcc = GoogleSignIn.getLastSignedInAccount(this);
        if(gAcc != null){
            userid = gAcc.getId();
        }else{
            userid = pref.getString("firebaseUserId","1");
        }

        //Get another calendar instance
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        //Calculate the start and end of day in UnixTime
        long startOfDay = cal.getTimeInMillis()/1000;
        long endOfDay = startOfDay + 86400;

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");

        DatabaseReference myDbRef = database.getReference("usersInformation").child(userid).child("UserAlarms");

        Random rand = new Random();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        myDbRef.addListenerForSingleValueEvent(new ValueEventListener() {//This has to be singleValueEvent, to prevent a bug
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int rndInt;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Alarm alarm = snapshot.getValue(Alarm.class);
                    if(alarm.getUnixTime()*1000L > System.currentTimeMillis()){
                        Intent intent1 = new Intent(HomeActivity.this,ReminderBroadcast.class);
                        intent1.putExtra("alarmTitle",alarm.getTitle());
                        intent1.putExtra("alarmDescription",alarm.getDescription());

                        //Need a different integer to tell the alarms apart, so i use a random integer
                        rndInt = rand.nextInt();
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), rndInt, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                        alarmManager.cancel(pendingIntent);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt(alarm.getUid(),rndInt);
                        editor.commit();

                        createNotificationChannel();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        myDbRef.addValueEventListener(new ValueEventListener() {//This is reactive
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int rndInt;
                ArrListAlarm.clear();//Clear the ArrayList to prevent the same activities being added again and again
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);//Set this to visualize loading

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Alarm alarm = snapshot.getValue(Alarm.class);
                    if(startOfDay < alarm.getUnixTime() && endOfDay > alarm.getUnixTime()) {//Getting only today's date
                        //Fill Array list
                        ArrListAlarm.add(new Alarm(alarm.getTitle(), alarm.getDescription(), alarm.getLongitude(),alarm.getLatitude(), alarm.getUnixTime() * 1000L,alarm.getUid()));
                    }
                }

                //Sorts the ArrayList
                for(int i=0;i<ArrListAlarm.size()-1;i++){
                    int m = i;
                    for(int j=i+1;j<ArrListAlarm.size();j++){
                        if(ArrListAlarm.get(m).getUnixTime() > ArrListAlarm.get(j).getUnixTime())
                            m = j;
                    }
                    //swapping elements at position i and m
                    Alarm temp = ArrListAlarm.get(i);
                    ArrListAlarm.set(i, ArrListAlarm.get(m));
                    ArrListAlarm.set(m, temp);

                }

                //Remove Loading Animation
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                //Get the calendar Object today's date.
                RecyclerView myrv = (RecyclerView) findViewById(R.id.recyclerViewTask);

                //Clicking on an child in an Recycler View
                myrv.addOnItemTouchListener(
                        new RecyclerItemClickListener(getApplication(), myrv ,new RecyclerItemClickListener.OnItemClickListener() {
                            @Override public void onItemClick(View view, int position) {
                                Intent intent = new Intent(getApplicationContext(), TaskDetails.class);
                                intent.putExtra("uid",ArrListAlarm.get(position).getUid());
                                startActivity(intent);
                            }

                            @Override public void onLongItemClick(View view, int position) {
                                Intent intent = new Intent(getApplicationContext(), TaskDetails.class);
                                intent.putExtra("uid",ArrListAlarm.get(position).getUid());
                                startActivity(intent);
                            }
                        })

                );




                //Gets the Adapter from the JAVA file
                RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(HomeActivity.this,ArrListAlarm);

                //Set Layout for the RecyclerView
                myrv.setLayoutManager(new LinearLayoutManager(HomeActivity.this));

                //Set an adapter for the View
                myrv.setAdapter(myAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Error",error.toString());
                // Failed to read value
            }
        });

        setContentView(R.layout.homepage_act);
        txtDate = (TextView) findViewById(R.id.date);
        txtDay = (TextView) findViewById(R.id.day);
        txtTaskTitle = (TextView) findViewById(R.id.taskTitle);
        txtTaskTime = (TextView) findViewById(R.id.taskTime);

        //Setting Date and Day
        txtDate.setText(currentDate);
        txtDay.setText(currentDay);

        //On Click Handlers
        Button button = findViewById(R.id.addNewTask);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddNewTaskActivity.class);
            intent.putExtra("edit",false);
            startActivity(intent);
        });

        Button qrButton = findViewById(R.id.qrScanner);
        qrButton.setOnClickListener(v ->{
            Intent intent = new Intent(this, QRActivity.class);
            startActivity(intent);
        });

        //Bottom Navigation Handling
        BottomNavigationView botNavView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        botNavView.getMenu().getItem(2).setChecked(true);//Set Middle(Home) to checked
        botNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            public boolean onNavigationItemSelected(@NonNull MenuItem item){
                switch(item.getItemId()){
                    case R.id.location:
                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.calendar:
                        intent = new Intent(getApplicationContext(), ScheduleActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.home:
                        return true;
                    case R.id.qr:
                        intent = new Intent(getApplicationContext(), QRActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.settings:
                        intent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(intent);
                        return true;
                }
                return false;
            };
        });

    }

    public void onBackPressed() {
        //If the user is logged in, he should not be able to relogin by pressing back btn
        //He should logout, then login
        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        if (GoogleSignIn.getLastSignedInAccount(this) == null && pref.getString("firebaseUserId","1") == "1") {//If there is no google account detected, and if there is no account detected
            super.onBackPressed();
        }else{

        }
    }

    public void createNotificationChannel(){//Creates an Notif Channel

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SchedularAlarmChannel";
            String description = "Channel for SchedularAlarm";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("Alarm",name,importance);
            channel.setDescription(description);
            channel.setImportance(importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }






}