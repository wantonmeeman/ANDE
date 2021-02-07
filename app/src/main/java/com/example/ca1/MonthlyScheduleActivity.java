package com.example.ca1;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.listeners.OnMonthChangeListener;
import sun.bob.mcalendarview.vo.DateData;
import sun.bob.mcalendarview.vo.MarkedDates;


public class MonthlyScheduleActivity extends AppCompatActivity implements View.OnClickListener{

    private int mLastDayNightMode;
    protected void onRestart(){
        super.onRestart();
        if (AppCompatDelegate.getDefaultNightMode() != mLastDayNightMode) {
            //This is an older method that works with the 3rd party library
            //recreate() would crash.
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Dateformats
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM, yyyy");
        SimpleDateFormat dayMonthFormat = new SimpleDateFormat("dd/MM");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monthly_tasks_act);
        this.getSupportActionBar().hide();

        //Bottom Navigation Handling
        BottomNavigationView botNavView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        botNavView.getMenu().getItem(1).setChecked(true);//Set Middle(Home) to checked
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
                        intent = new Intent(getApplicationContext(),SettingsActivity.class);
                        startActivity(intent);
                        return true;
                }
                return false;
            };
        });

        //get current Calendar instance
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        ArrayList<Alarm> ArrListAlarm = new ArrayList<Alarm>();

        //Handle login
        String userid = "";
        GoogleSignInAccount gAcc = GoogleSignIn.getLastSignedInAccount(this);
        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        if(gAcc != null){
            userid = gAcc.getId();
        }else{
            userid = pref.getString("firebaseUserId","123123");
            Log.i("Message","Cant access google account");
        }

        //Setting Text
        TextView currentMonthYear = (TextView) findViewById(R.id.currentMonthYear);
        TextView currentDayMonth = (TextView)findViewById(R.id.currentDayMonth);

        currentMonthYear.setText(monthYearFormat.format(cal));
        currentDayMonth.setText("Tasks (" + dayMonthFormat.format(cal) + ")");

        Button Today = (Button) findViewById(R.id.Today);
        Today.setOnClickListener(this);

        MCalendarView calendarView = (MCalendarView) findViewById(R.id.calendarView); // get the reference of CalendarView,3rd part library
        calendarView.hasTitle(false);

        //Clear Previous calendar state and Markings
        calendarView.getMarkedDates().getAll().clear();
        calendarView.travelTo(new DateData(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,1));

        //Get Start and end of date.
        long startOfDay = cal.getTimeInMillis() / 1000;
        long endOfDay = startOfDay + 86400;

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");

        DatabaseReference myDbRef = database.getReference("usersInformation").child(userid);

        myDbRef.child("UserAlarms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                ArrListAlarm.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    //This snippet handles before the user clicks a date.
                    Alarm alarm = snapshot.getValue(Alarm.class);
                    if(startOfDay < alarm.getUnixTime() && endOfDay > alarm.getUnixTime()) {//Get only today's date
                        ArrListAlarm.add(new Alarm(alarm.getTitle(), alarm.getDescription(), alarm.getLongitude(),alarm.getLatitude(), alarm.getUnixTime() * 1000L,alarm.getUid()));
                    }

                    //Sorts thru the arrayList
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

                    //This snippet goes through every date and assigns them into the calendarView.
                    Calendar calendarInstance = Calendar.getInstance();
                    calendarInstance.setTime(new Date(alarm.getUnixTime()*1000));
                    calendarView.markDate(
                            new DateData(
                                    calendarInstance.get(Calendar.YEAR),
                                    calendarInstance.get(Calendar.MONTH)+1,//January is 0 in calendar.
                                    calendarInstance.get(Calendar.DAY_OF_MONTH)
                            ).setMarkStyle(new MarkStyle(MarkStyle.DOT, Color.RED))
                    );
                }

                RecyclerView myrv = (RecyclerView) findViewById(R.id.recyclerViewTask);
                //Handles touch
                myrv.addOnItemTouchListener(
                        new RecyclerItemClickListener(getApplication(), myrv ,new RecyclerItemClickListener.OnItemClickListener() {
                            @Override public void onItemClick(View view, int position) {
                                Log.i("Short press",Integer.toString(position));
                                Intent intent = new Intent(getApplicationContext(), TaskDetails.class);
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
                RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(MonthlyScheduleActivity.this,ArrListAlarm);

                //Set Layout for the RecyclerView
                myrv.setLayoutManager(new LinearLayoutManager(MonthlyScheduleActivity.this));

                //Set an adapter for the View
                myrv.setAdapter(myAdapter);
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                currentMonthYear.setText(monthYearFormat.format(cal));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Error",error.toString());
                // Failed to read value
            }

        });

        calendarView.hasTitle(false);//This has to be declared frequently, the calendarView is very finicky

        calendarView.setOnDateClickListener(new OnDateClickListener() {//When a user clicks on a date
            DateData prevDataDate;
            @Override
            public void onDateClick(View view, DateData date) {

                //Handles marking once a new date is clicked
                //-New date needs to be marked
                //-Old date needs to be unmarked
                if(prevDataDate != null) {
                    calendarView.unMarkDate(prevDataDate);
                }

                for(int i = 0;calendarView.getMarkedDates().getAll().size() > i;i++) {
                    if(date.equals(calendarView.getMarkedDates().getAll().get(i))) {
                        calendarView.unMarkDate(date);//unmark the date(remove event marking, the red dot or the current date marking)
                        calendarView.markDate(//set the marking for a date that is clicked on
                                date.setMarkStyle(
                                        new MarkStyle(MarkStyle.BACKGROUND, Color.parseColor("#0094f3")
                                        )
                                )
                        );//When an Event is already in the date selected.
                    }
                    if(prevDataDate != null && !(prevDataDate.equals(date))) {//The second condition resolves if a marked date is clicked twice.
                        if (prevDataDate.equals(calendarView.getMarkedDates().getAll().get(i))) {//set a custom mark style, if the previous date had events on it
                            calendarView.unMarkDate(prevDataDate);//remove all marks from previous date
                            calendarView.markDate(//add marking for events on the previous date
                                    prevDataDate.setMarkStyle(
                                        new MarkStyle(MarkStyle.DOT, Color.RED)
                                    )
                            );
                        }
                    }
                }

                calendarView.markDate(date).setMarkedStyle(MarkStyle.BACKGROUND);

                prevDataDate = date;//set the current date clicked on as this value to prepare for the next onClickDate event

                cal.set(Calendar.MONTH,date.getMonth()-1);
                cal.set(Calendar.DATE,Integer.parseInt(date.getDayString()));


                //Get the start and end of the day
                long startOfDay = cal.getTimeInMillis() / 1000;
                long endOfDay = startOfDay + 86400;

                currentDayMonth.setText("Tasks (" + dayMonthFormat.format(cal) + ")");
                //Loading Icon is shown
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

                //maybe make this a function
                myDbRef.child("UserAlarms").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        ArrListAlarm.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Alarm alarm = snapshot.getValue(Alarm.class);
                            if(startOfDay < alarm.getUnixTime() && endOfDay > alarm.getUnixTime()) {//Get only today's date
                                ArrListAlarm.add(new Alarm(alarm.getTitle(), alarm.getDescription(), alarm.getLongitude(),alarm.getLatitude(), alarm.getUnixTime() * 1000L,alarm.getUid()));
                            }
                        }

                        //ArrayList Sorting
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

                        //Get the calendar Object today's date.
                        RecyclerView myrv = findViewById(R.id.recyclerViewTask);

                        //Set Layout, here we set LinearLayout
                        myrv.setLayoutManager(new LinearLayoutManager(getApplication()));

                        RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(getApplication(),ArrListAlarm);

                        //Set an adapter for the View
                        myrv.setAdapter(myAdapter);
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.i("Error",error.toString());
                        // Failed to read value
                    }
                });
            }
        });
        //This handles Swiping
        calendarView.setOnMonthChangeListener(new OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month) {

                calendarView.hasTitle(false);
                //YearB -> Previous Month+Year
                int YearB = cal.get(Calendar.YEAR);
                int MonthB = cal.get(Calendar.MONTH);
                int YearA;
                int MonthA;

                //month-1 due to the fact that january starts at 0, but for the 3rd party calendar, it starts at 1
                cal.set(Calendar.MONTH,month-1);
                cal.set(Calendar.YEAR, year);
                //YearA -> Swiped to Month+Year
                YearA = cal.get(Calendar.YEAR);
                MonthA = cal.get(Calendar.MONTH);

                //This code resolves a bug where using the imgButton(to go from Jan2020 -> dec2019),
                //then swiping right would not increment the year by 1.
                //Example:
                //Using ImgButton jan2020 -> dec2019
                //Swipe right dec2019 -> jan2019(Supposed to be jan2020)
                //This supposedly doesnt happen vice versa
                if(YearA == YearB && MonthB - MonthA == 11){
                    cal.set(Calendar.YEAR, year+1);
                }

                //This resolves a bug when prev Month is clicked, then swiped forward to another year
                if(cal.get(Calendar.YEAR) != year){
                    calendarView.travelTo(new DateData(cal.get(Calendar.YEAR),1,1));
                }
                //Set Text
                currentMonthYear.setText(monthYearFormat.format(cal));

            }
        });
        ImageButton prevMnth = (ImageButton)findViewById(R.id.prevMonth);
        ImageButton nextMnth = (ImageButton)findViewById(R.id.nextMonth);

        nextMnth.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Handle moving to the next month
                if(cal.get(Calendar.MONTH) == 11) {//If the month is december, add a year and go back to january
                    calendarView.travelTo(new DateData(cal.get(Calendar.YEAR)+1, 1, 1));
                }else{
                    calendarView.travelTo(new DateData(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+2, 1));
                }
                currentMonthYear.setText(monthYearFormat.format(cal));
                calendarView.hasTitle(false);
            }
        });

        prevMnth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle Previous month
                if(cal.get(Calendar.MONTH) == 0) {//If the month is january, add a year and go back to december
                    calendarView.travelTo(new DateData(cal.get(Calendar.YEAR)-1, 12, 1));
                }else{
                    calendarView.travelTo(new DateData(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1));
                }
                currentMonthYear.setText(monthYearFormat.format(cal));
                calendarView.hasTitle(false);
            }
        });
    }

    public void onClick(View v) {//Handle When the Monthly/Today buttons are clicked
        switch (v.getId()) {
            case R.id.Today://When Monthly is clicked
                Intent intent = new Intent(getApplicationContext(),ScheduleActivity.class);
                startActivity(intent);
                break;
            default:
                Log.i("Error","There has been an error");
                break;
        }
    }
}