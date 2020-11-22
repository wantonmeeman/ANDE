package com.example.ca1;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ScheduleActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todays_tasks_act);

        this.getSupportActionBar().hide();//Remove Title, probably not very good

        //Declare Variables
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