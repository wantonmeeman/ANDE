package com.example.ca1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;



public class ScheduleActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,View.OnClickListener,ViewPager.OnPageChangeListener{

    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todays_tasks_act);
        this.getSupportActionBar().hide();

        viewPager = findViewById(R.id.pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(this);

        Button Monthly = (Button)findViewById(R.id.Monthly);
        Monthly.setOnClickListener(this);

        Button Today = (Button)findViewById(R.id.Today);
        Today.setOnClickListener(this);
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Button Monthly = (Button)findViewById(R.id.Monthly);
        Button Today = (Button)findViewById(R.id.Today);
        if (position == 1){
            Today.setEnabled(false);
            Monthly.setEnabled(true);
        }else{
            Today.setEnabled(true);
            Monthly.setEnabled(false);
        }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    public void onClick(View v) {
        Button Monthly = (Button)findViewById(R.id.Monthly);
        Button Today = (Button)findViewById(R.id.Today);
        switch (v.getId()) {
            case R.id.Today:
                viewPager.setCurrentItem(getItem(+1), true);
                Today.setEnabled(false);
                Monthly.setEnabled(true);
                break;
            case R.id.Monthly:
                viewPager.setCurrentItem(getItem(-1), true);
                Today.setEnabled(true);
                Monthly.setEnabled(false);
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
                return true;
            case R.id.qr:
                return true;
            case R.id.settings:
                return true;

        }
        return false;
    }


}