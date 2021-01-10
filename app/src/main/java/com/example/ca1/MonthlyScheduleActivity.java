package com.example.ca1;


import android.content.Intent;
import android.content.res.TypedArray;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.vo.DateData;


public class MonthlyScheduleActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM, yyyy");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monthly_tasks_act);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.getSupportActionBar().hide();
        TextView currentMonthYear = (TextView) findViewById(R.id.currentMonthYear);
        currentMonthYear.setText(dateFormat.format(cal));
        Button Today = (Button) findViewById(R.id.Today);
        Today.setOnClickListener(this);

        MCalendarView calendarView = (MCalendarView) findViewById(R.id.calendarView); // get the reference of CalendarView
        calendarView.markDate(2020,12,30);
        calendarView.hasTitle(false);
        calendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {
                    view.setBackgroundResource(R.drawable.ripple);
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