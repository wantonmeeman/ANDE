package com.example.ca1;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


public class ScheduleActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todays_tasks_act);
        this.getSupportActionBar().hide();

        Button Monthly = (Button) findViewById(R.id.Monthly);
        Monthly.setOnClickListener(this);

        ArrayList<Alarm> ArrListAlarm = new ArrayList<Alarm>();
        ProgressBar todayProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        TextView percentageCompletion = (TextView)findViewById(R.id.todayProgress);
        TextView completionStatus = (TextView)findViewById(R.id.CompletionStatus);
        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

            //Get the calendar Object today's date.
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            //Get Start and end of date.
            long startOfDay = cal.getTimeInMillis() / 1000;
            long endOfDay = startOfDay + 86400;

        String userid = "";
        GoogleSignInAccount gAcc = GoogleSignIn.getLastSignedInAccount(this);
        if(gAcc != null){
            userid = gAcc.getId();
        }else{
            userid = pref.getString("firebaseUserId","123123");
            Log.i("Message","Cant access google account");
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");
        DatabaseReference myDbRef = database.getReference("usersInformation").child(userid);


        myDbRef.child("UserAlarms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int count = 0;
                ArrListAlarm.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Alarm alarm = snapshot.getValue(Alarm.class);
                    if(startOfDay < alarm.getUnixTime() && endOfDay > alarm.getUnixTime()) {//Get only today's date
                        ArrListAlarm.add(new Alarm(alarm.getTitle(), alarm.getDescription(), "","", alarm.getUnixTime() * 1000L));
                        if(alarm.getUnixTime() * 1000L < System.currentTimeMillis()){
                            count++;
                        }
                    }
                }


                int completedTaskPercentage = (int)Math.round(((double)count/(double)ArrListAlarm.size())*100);
                if(ArrListAlarm.size() == 0){
                    completedTaskPercentage = 100;
                }
                percentageCompletion.setText(Integer.toString(completedTaskPercentage)+"%");
                todayProgressBar.setProgress(completedTaskPercentage);
                completionStatus.setText("You have completed "+count+"/"+ArrListAlarm.size()+" tasks Today");



                //Get the calendar Object today's date.
                //Put a loading animation here
                //
                //
                //
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

            TextView todayDate = (TextView)findViewById(R.id.todayDate);
            
            todayDate.setText(dateFormat.format(cal));



        ImageButton imgButton = findViewById(R.id.backButton);
        imgButton.setOnClickListener(v -> {
            Intent intent = new Intent(this,HomeActivity.class);
            startActivity(intent);
        });

        BottomNavigationView botNavView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        botNavView.getMenu().getItem(1).setChecked(true);
        botNavView.setOnNavigationItemSelectedListener(this);
        FloatingActionButton addNewTask = (FloatingActionButton) findViewById(R.id.fab);

        addNewTask.setOnClickListener(v ->{
            Log.v("myTag","FAB Clicked");
        });
    }

    public void onClick(View v) {//Handle When the Monthly/Today buttons are clicked
        switch (v.getId()) {
            case R.id.Monthly://When Monthly is clicked
                Intent intent = new Intent(getApplicationContext(),MonthlyScheduleActivity.class);
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