package com.example.ca1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class LoginActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    //This handles dismissing of a notification
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_act);
        this.getSupportActionBar().hide();

        Button regButton = findViewById(R.id.Register);
        regButton.setOnClickListener(v -> {
            Intent intent = new Intent(this,RegisterActivity.class);
            startActivity(intent);
        });

        Button logButton = findViewById(R.id.Login);
        logButton.setOnClickListener(v -> {
            Intent intent = new Intent(this,HomeActivity.class);
            startActivity(intent);
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
