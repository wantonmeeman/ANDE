package com.example.ca1;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import static android.content.Context.NOTIFICATION_SERVICE;


public class DismissActivity extends Activity {

    //This handles dismissing of a notification
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //For some reason this lags when u click dismiss immediately
        //So we just dont click it immediately
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(getIntent().getIntExtra("notifID", 1));
        finish(); // since finish() is called in onCreate(), onDestroy() will be called immediately
    }

}
