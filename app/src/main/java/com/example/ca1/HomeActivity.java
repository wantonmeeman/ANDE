package com.example.ca1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
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

        //This refreshes each component.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        if(pref.getBoolean("UIMode",true)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            getDelegate().applyDayNight();
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            getDelegate().applyDayNight();
        }
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

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long startOfDay = cal.getTimeInMillis()/1000;
        long endOfDay = startOfDay + 86400;

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");

        DatabaseReference myDbRef = database.getReference("usersInformation").child(userid).child("UserAlarms");

        String alphabet = "123456789";

        // create random string builder
        StringBuilder sb = new StringBuilder();

        // create an object of Random class
        Random random = new Random();

        // specify length of random string
        int length = 21;

        for(int i = 0; i < length; i++) {

            // generate random index number
            int index = random.nextInt(alphabet.length());

            // get character specified by index
            // from the string
            char randomChar = alphabet.charAt(index);

            // append the character to string builder
            sb.append(randomChar);
        }

        String randomString = sb.toString();

        Random rand = new Random();
        //Alarm testAlarm = new Alarm("alarmTitle1","alarmDescription1",103.78462387+(rand.nextDouble()/10),1.42613738+(rand.nextDouble()/10),((System.currentTimeMillis() / 1000L)+(1*60)),randomString);
//
        //myDbRef.child(randomString).setValue(testAlarm);
        //Alarm testAlarm = new Alarm("alarmTitle1","alarmDescription1",103.78462387+(rand.nextDouble()/10),1.42613738+(rand.nextDouble()/10),((System.currentTimeMillis() / 1000L)+(1*60)));


        myDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                ArrListAlarm.clear();
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Alarm alarm = snapshot.getValue(Alarm.class);
                    if(startOfDay < alarm.getUnixTime() && endOfDay > alarm.getUnixTime()) {//Get only today's date
                        ArrListAlarm.add(new Alarm(alarm.getTitle(), alarm.getDescription(), alarm.getLongitude(),alarm.getLatitude(), alarm.getUnixTime() * 1000L,alarm.getUid()));
                    }
                }

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

                myrv.addOnItemTouchListener(
                        new RecyclerItemClickListener(getApplication(), myrv ,new RecyclerItemClickListener.OnItemClickListener() {
                            @Override public void onItemClick(View view, int position) {
                                Log.i("Short press",Integer.toString(position));
                                Intent intent = new Intent(getApplicationContext(), TaskDetails.class);
                                Log.i("nword",ArrListAlarm.get(position).getUid());
                                intent.putExtra("uid",ArrListAlarm.get(position).getUid());
                                startActivity(intent);
                            }

                            @Override public void onLongItemClick(View view, int position) {
                                Log.i("Long Press",Integer.toString(position));
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
        ImageButton editTask = (ImageButton) findViewById(R.id.editTask);
//        editTask.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), TaskDetails.class);
//                intent.putExtra("uid",ArrListAlarm.get(0).getUid());
//                startActivity(intent);
//            }
//        });

        txtDate.setText(currentDate);
        txtDay.setText(currentDay);

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
                        intent = new Intent(getApplicationContext(),HomeActivity.class);
                        startActivity(intent);
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



        //createNotificationChannel();

        Button button = findViewById(R.id.addNewTask);

        button.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddNewTaskActivity.class);
            startActivity(intent);
//            Intent intent = new Intent(HomeActivity.this,ReminderBroadcast.class);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeActivity.this,0,intent,0);
//
//            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//
//            long timeAtButtonClick = System.currentTimeMillis();
//
//            long tenSecondsInMillis = 5000 ;
//
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,timeAtButtonClick + tenSecondsInMillis,pendingIntent);
//            Toast.makeText(this,"Reminder!",Toast.LENGTH_LONG).show();
        });

        Button qrButton = findViewById(R.id.qrScanner);
        qrButton.setOnClickListener(v ->{
            Intent intent = new Intent(this, QRActivity.class);
            startActivity(intent);
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
//    public void createNotificationChannel(){
//
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "LemubitReminderChannel";
//            String description = "Channel for Lemubit Reminder";
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel channel = new NotificationChannel("Alarm",name,importance);
//            channel.setDescription(description);
//            channel.setImportance(importance);
//            channel.enableVibration(true);
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//
//    }
    //@Override






}