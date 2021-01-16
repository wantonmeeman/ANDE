package com.example.ca1;


import android.content.Intent;
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
import java.util.Random;

import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.listeners.OnMonthChangeListener;
import sun.bob.mcalendarview.vo.DateData;
import sun.bob.mcalendarview.vo.MarkedDates;


public class MonthlyScheduleActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM, yyyy");
        SimpleDateFormat dayMonthFormat = new SimpleDateFormat("dd/MM");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monthly_tasks_act);
        this.getSupportActionBar().hide();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        ArrayList<Alarm> ArrListAlarm = new ArrayList<Alarm>();

        String userid = "";
        GoogleSignInAccount gAcc = GoogleSignIn.getLastSignedInAccount(this);

        if(gAcc != null){
            userid = gAcc.getId();
        }else{
            Log.i("Message","Cant access google account");
        }

        TextView currentMonthYear = (TextView) findViewById(R.id.currentMonthYear);
        TextView currentDayMonth = (TextView)findViewById(R.id.currentDayMonth);

        currentMonthYear.setText(monthYearFormat.format(cal));
        currentDayMonth.setText(dayMonthFormat.format(cal));

        Button Today = (Button) findViewById(R.id.Today);
        Today.setOnClickListener(this);

        MCalendarView calendarView = (MCalendarView) findViewById(R.id.calendarView); // get the reference of CalendarView
        calendarView.hasTitle(false);

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
                    Alarm alarm = snapshot.getValue(Alarm.class);
                    if(startOfDay < alarm.getUnixTime() && endOfDay > alarm.getUnixTime()) {//Get only today's date
                        ArrListAlarm.add(new Alarm(alarm.getTitle(), alarm.getDescription(), "","", alarm.getUnixTime() * 1000L));
                    }
                }

                RecyclerView myrv = (RecyclerView) findViewById(R.id.recyclerViewTask);

                //Gets the Adapter from the JAVA file
                RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(getApplication(),ArrListAlarm);

                //Set Layout for the RecyclerView
                myrv.setLayoutManager(new LinearLayoutManager(getApplication()));

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
        calendarView.hasTitle(false);

        calendarView.markDate(
                new DateData(2021,1,30).setMarkStyle(new MarkStyle(MarkStyle.DOT, Color.RED))
        );

        calendarView.setOnDateClickListener(new OnDateClickListener() {
            DateData prevDataDate;
            @Override
            public void onDateClick(View view, DateData date) {
                if(prevDataDate != null) {
                    calendarView.unMarkDate(prevDataDate);
                }
                //THERE IS 100% AN EASIER WAY TO DO THIS BUT IM TOO TIRED PLACEHOLDER FOR NOW.
                for(int i = 0;calendarView.getMarkedDates().getAll().size() > i;i++) {
                    if(date.equals(calendarView.getMarkedDates().getAll().get(i))){
                        calendarView.unMarkDate(date);
                        calendarView.markDate(2021, 1, 30).setMarkedStyle(MarkStyle.BACKGROUND);//When an Event is already in the date selected.
                    }
                    if(prevDataDate != null) {
                        if (prevDataDate.equals(calendarView.getMarkedDates().getAll().get(i))) {
                            calendarView.unMarkDate(prevDataDate);
                            calendarView.markDate(
                                    new DateData(2021, 1, 30).setMarkStyle(new MarkStyle(MarkStyle.DOT, Color.RED))
                            );
                        }
                    }
                }
                calendarView.markDate(date).setMarkedStyle(MarkStyle.BACKGROUND);

                prevDataDate = date;

                cal.set(Calendar.MONTH,date.getMonth()-1);
                cal.set(Calendar.DATE,Integer.parseInt(date.getDayString()));

                long startOfDay = cal.getTimeInMillis() / 1000;
                long endOfDay = startOfDay + 86400;

                currentDayMonth.setText(dayMonthFormat.format(cal));
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
                                ArrListAlarm.add(new Alarm(alarm.getTitle(), alarm.getDescription(), "","", alarm.getUnixTime() * 1000L));
                            }
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

        calendarView.setOnMonthChangeListener(new OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month) {
                int YearB = cal.get(Calendar.YEAR);
                int MonthB = cal.get(Calendar.MONTH);
                int YearA;
                int MonthA;

                cal.set(Calendar.MONTH,month-1);
                cal.set(Calendar.YEAR, year);

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
                currentMonthYear.setText(monthYearFormat.format(cal));

            }
        });
        ImageButton prevMnth = (ImageButton)findViewById(R.id.prevMonth);
        ImageButton nextMnth = (ImageButton)findViewById(R.id.nextMonth);

        nextMnth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("NextOnClick","true");
                if(cal.get(Calendar.MONTH) == 11) {
                    calendarView.travelTo(new DateData(cal.get(Calendar.YEAR)+1, 1, 1));
                }else{
                    calendarView.travelTo(new DateData(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+2, 1));
                }
                calendarView.hasTitle(false);
            }
        });

        prevMnth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cal.get(Calendar.MONTH) == 0) {
                    calendarView.travelTo(new DateData(cal.get(Calendar.YEAR)-1, 12, 1));
                }else{
                    calendarView.travelTo(new DateData(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1));
                }
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

    
        public boolean onNavigationItemSelected(@NonNull MenuItem item){
            switch(item.getItemId()){
                case R.id.location:

                    return true;
                case R.id.calendar:

                    return true;
                case R.id.home:
                    Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                    Log.e("Clicked","Location");
                    startActivity(intent);
                    return true;
                case R.id.qr:
                    intent = new Intent(getApplicationContext(), QRActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.settings:
                    return true;

            }
            return false;
        };


}