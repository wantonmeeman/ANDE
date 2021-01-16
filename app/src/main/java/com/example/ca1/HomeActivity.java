package com.example.ca1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
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
import android.view.View;
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
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);

    Calendar calendar = Calendar.getInstance();

    TextView txtTaskTitle;
    TextView txtDate;
    TextView txtDay;
    TextView txtTaskTime;
    ArrayList<Alarm> ArrListAlarm;

    String currentDate = dateFormat.format(new Date());
    String currentDay = dayFormat.format(calendar.getTime());

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_act);

        this.getSupportActionBar().hide();//Remove Title, probably not very good

        //Declare Variables
        BottomNavigationView botNavView;
//        JSONObject jObject;
//        JSONObject pjObject;
//        JSONArray jArray;
//        long JSONtime;
//        String JSONtitle;
//        String JSONdesc;
//        long tempTime;
//        String tempTitle;
//        String tempDesc;
        ArrListAlarm = new ArrayList<Alarm>();
        String userid = "";
        GoogleSignInAccount gAcc = GoogleSignIn.getLastSignedInAccount(this);
        if(gAcc != null){
            userid = gAcc.getId();
        }else{
            Log.i("Message","Cant access google account");
        }

        FileOutputStream fOut = null;
        txtDate = (TextView) findViewById(R.id.date);
        txtDay = (TextView) findViewById(R.id.day);
        txtTaskTitle = (TextView) findViewById(R.id.taskTitle);
        txtTaskTime = (TextView) findViewById(R.id.taskTime);

        txtDate.setText(currentDate);
        txtDay.setText(currentDay);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long startOfDay = cal.getTimeInMillis()/1000;
        long endOfDay = startOfDay + 86400;

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");

        DatabaseReference myDbRef = database.getReference("usersInformation").child(userid);

//        Alarm testAlarm = new Alarm("testTitle","testDescription","","",((System.currentTimeMillis() / 1000L)+ 15 * 60));
//        Alarm testAlarm1 = new Alarm("testTitle1","testDescription1","","",((System.currentTimeMillis() / 1000L)+ 15 * 60));
//        User testUser = new User("testUsername","testPass","Email@email.com");
//
//        myDbRef.child("UserAlarms").push().setValue(testAlarm);
//        myDbRef.child("UserAlarms").push()/*push sets the key to be a random Value, allowing us to put multiple into 1 child*/.setValue(testAlarm1);
//        myDbRef.child("UserInfomation").setValue(testUser);
        myDbRef.child("UserAlarms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                ArrListAlarm.clear();
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                //We can use this snippet to prolong the loading animation thingy
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Alarm alarm = snapshot.getValue(Alarm.class);
                    if(startOfDay < alarm.getUnixTime() && endOfDay > alarm.getUnixTime()) {//Get only today's date
                        ArrListAlarm.add(new Alarm(alarm.getTitle(), alarm.getDescription(), "","", alarm.getUnixTime() * 1000L));
                    }
                }
                //Remove Loading Animation
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                //Get the calendar Object today's date.
                RecyclerView myrv = (RecyclerView) findViewById(R.id.recyclerViewTask);

                //Gets the Adapter from the JAVA file
                RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(getApplication(),ArrListAlarm);

                //Set Layout for the RecyclerView
                myrv.setLayoutManager(new LinearLayoutManager(getApplication()));

                //Set an adapter for the View
                myrv.setAdapter(myAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Error",error.toString());
                // Failed to read value
            }
        });

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

//        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode &
//                Configuration.UI_MODE_NIGHT_MASK;
//        switch (nightModeFlags) {
//            case Configuration.UI_MODE_NIGHT_YES:
//                Log.i("Darkmode","Yes");
//                break;
//
//            case Configuration.UI_MODE_NIGHT_NO:
//                Log.i("Darkmode","No");
//                break;
//
//            case Configuration.UI_MODE_NIGHT_UNDEFINED:
//                Log.i("Darkmode","IDK");
//                break;
//        }

        botNavView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        botNavView.getMenu().getItem(2).setChecked(true);//Set Middle(Home) to checked
        botNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            public boolean onNavigationItemSelected(@NonNull MenuItem item){
                switch(item.getItemId()){
                    case R.id.location:
                        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode &
                                Configuration.UI_MODE_NIGHT_MASK;
                        switch (nightModeFlags) {
                            case Configuration.UI_MODE_NIGHT_YES:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                Log.i("Darkmode","Yes");
                                break;

                            case Configuration.UI_MODE_NIGHT_NO:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                Log.i("Darkmode","No");
                                break;

                            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                Log.i("Darkmode","IDK");
                                break;
                        }
                        return true;
                    case R.id.calendar:
                        Intent intent = new Intent(getApplicationContext(),ScheduleActivity.class);
                        Log.e("Clicked","Location");
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
            }
        });

    }
    public void onBackPressed() {
        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            super.onBackPressed();
        } else {
        }
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