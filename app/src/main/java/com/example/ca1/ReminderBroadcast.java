package com.example.ca1;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;


public class ReminderBroadcast extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent){

        //Intent to go to SS
        Intent notifIntent = new Intent(context, SplashScreen.class);
        notifIntent.putExtra("notifID",1);
        PendingIntent redirectIntent = PendingIntent.getActivity(context,0,notifIntent,0);

        //Intent to Dismiss notification
        Intent dismissIntent = new Intent(context, NotificationActivity.class);

        //Idk wtf this set flags thing does but put it here
        dismissIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        dismissIntent.putExtra("notifID", 1);
        PendingIntent dismissPendingIntent = PendingIntent.getActivity(context, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Alarm")
                .setSmallIcon(R.drawable.ic_baseline_calendar_today_24)
                .setContentTitle("Alarm")
                .setContentText("Alarm Triggered")
                .setSubText("Alarm X Triggered")
                .setSound(Settings.System.DEFAULT_ALARM_ALERT_URI)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_CALL)
                .setColor(Color.MAGENTA)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher,"Dismiss",dismissPendingIntent)
                .addAction(R.mipmap.ic_launcher,"Get Info",redirectIntent);

        Notification mNotification = builder.build();
        //mNotification.flags |= Notification.FLAG_INSISTENT;


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(1,mNotification);
        turnScreenOn(0,context);
    }
    public static void turnScreenOn(int sec, final Context context)
    {
        final int seconds = sec;

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();

        if( !isScreenOn )
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(seconds*1000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
            wl_cpu.acquire(seconds*1000);
        }
    }
}
