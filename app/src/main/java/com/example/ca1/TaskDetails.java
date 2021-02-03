package com.example.ca1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import org.w3c.dom.Text;
// implements OnMapReadyCallback
public class TaskDetails extends AppCompatActivity {

    //This Snippet changes the UI when the user backbtn's
    private int mLastDayNightMode;

    protected void onRestart() {
        super.onRestart();
        if (AppCompatDelegate.getDefaultNightMode() != mLastDayNightMode) {
            recreate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getSupportActionBar().hide();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("kk:mm");
        super.onCreate(savedInstanceState);

        //This refreshes each component.

        setContentView(R.layout.activity_task_details);

        String userid = "";
        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        GoogleSignInAccount gAcc = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");

        if(gAcc != null){
            userid = gAcc.getId();
        }else{
            userid = pref.getString("firebaseUserId","1");
        }

        DatabaseReference myDbRef = database.getReference("usersInformation").child(userid).child("UserAlarms");
        TextView titleTxt = findViewById(R.id.titleTxt);
        TextView descriptionTxt =findViewById(R.id.descriptionTxt);
        TextView timeTxt = findViewById(R.id.time);
        TextView dateTxt = findViewById(R.id.date);
        TextView locationTxt = findViewById(R.id.location);
        final double[] selectedLatitude = {0};
        final double[] selectedLongitude = {0};

        String Uid = getIntent().getStringExtra("uid");
        Calendar cal = Calendar.getInstance();

        MaterialButton delBtn = findViewById(R.id.delBtn);

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {//Using a handler works, for some reason.....
                    @Override
                    public void run() {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(TaskDetails.this);
                        builder1.setMessage("Are you sure you want to delete this?");
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                        startActivity(intent);
                                        myDbRef.child(Uid).removeValue();
                                        dialog.cancel();
                                    }
                                });

                        builder1.setNegativeButton(
                                "No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                },100);

            }
        });

        ImageButton backBtn = findViewById(R.id.editBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageButton editBtn = findViewById(R.id.editBtn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddNewTaskActivity.class);
                intent.putExtra("edit",(Boolean)true);
                intent.putExtra("uid",Uid);
                intent.putExtra("title",titleTxt.getText());
                intent.putExtra("desc",descriptionTxt.getText());
                intent.putExtra("unixTime",cal.getTimeInMillis());
                intent.putExtra("latitude", selectedLatitude[0]);
                intent.putExtra("longitude", selectedLongitude[0]);
                startActivity(intent);
            }
        });

        myDbRef.child(Uid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Alarm alarm = dataSnapshot.getValue(Alarm.class);
                if(alarm != null) {//This handles when the user deletes the object
                    titleTxt.setText(alarm.getTitle());
                    descriptionTxt.setText(alarm.getDescription());
                    cal.setTimeInMillis(alarm.getUnixTime() * 1000L);
                    timeTxt.setText(timeFormat.format(cal.getTime()));
                    dateTxt.setText(dateFormat.format(cal.getTime()));
                    Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
                    try {
                        selectedLatitude[0] = alarm.getLatitude();
                        selectedLongitude[0] = alarm.getLongitude();
                        Address locationAddress = geocoder.getFromLocation(alarm.getLatitude(), alarm.getLongitude(), 1).get(0);
                        locationTxt.setText(locationAddress.getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e) {//If a location cannot be found.
                        locationTxt.setText(" ");
                        e.printStackTrace();
                    }
                    if (alarm.getUnixTime() * 1000L < System.currentTimeMillis()) {
                        editBtn.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Error",error.toString());
                // Failed to read value
            }
        });

        BottomNavigationView botNavView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        botNavView.getMenu().getItem(2).setChecked(true);//Set Middle(Home) to checked
        botNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.location:
                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.calendar:
                        intent = new Intent(getApplicationContext(), ScheduleActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.home:
                        intent = new Intent(getApplicationContext(), HomeActivity.class);
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
            }

            ;
        });

    }
}

//    public void onMapReady(GoogleMap googleMap) {
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                //.requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
//
//        this.getSupportActionBar().hide();//Remove Title, probably not very good
//
//        String userid = "";
//        GoogleSignInAccount gAcc = GoogleSignIn.getLastSignedInAccount(this);
//        if(gAcc != null){
//            userid = gAcc.getId();
//        }else{
//            userid = pref.getString("firebaseUserId","1");
//        }
//
//        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");
//
//        DatabaseReference myDbRef = database.getReference("usersInformation").child(userid).child("UserAlarms");
//        GoogleMap mMap = googleMap;
//        String Uid = getIntent().getStringExtra("uid");
//        myDbRef.child(Uid).addValueEventListener(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Alarm alarm = dataSnapshot.getValue(Alarm.class);
//                LatLng currentLoc = new LatLng(alarm.getLatitude(),alarm.getLongitude());
//                mMap.clear();
//                mMap.addMarker(new MarkerOptions().position(
//                        new LatLng(alarm.getLatitude(),alarm.getLongitude())
//                ));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc,12));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                Log.i("Error",error.toString());
//                // Failed to read value
//            }
//        });
//
//    }
//
//}