package com.example.ca1;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class RegisterActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    //This handles dismissing of a notification
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_act);

        this.getSupportActionBar().hide();

        ImageButton imgButton = findViewById(R.id.backButton);
        imgButton.setOnClickListener(v -> {
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
