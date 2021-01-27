package com.example.ca1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


public class AddNewTaskActivity extends AppCompatActivity {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("kk:mm");

    private int mLastDayNightMode;
    protected void onRestart(){
        super.onRestart();
        if (AppCompatDelegate.getDefaultNightMode() != mLastDayNightMode) {
            recreate();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task_act);
        this.getSupportActionBar().hide();//Remove Title, probably not very good;

        EditText titleTxt = findViewById(R.id.title);
        EditText descriptionTxt = findViewById(R.id.description);
        EditText timeTxt = findViewById(R.id.Time);
        EditText dateTxt = findViewById(R.id.Date);
        EditText locTxt = findViewById(R.id.Location);
        MaterialButton submitBtn = findViewById(R.id.submitBtn);

        //Values that were passed in thru the previous activity
        Double selectedLatitude = getIntent().getDoubleExtra("latitude",-1);
        Double selectedLongtitude = getIntent().getDoubleExtra("longtitude",-1);
        Long selectedDate = getIntent().getLongExtra("unixTime",-1);
        String Uid = getIntent().getStringExtra("uid");

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        final String userid;
        GoogleSignInAccount gAcc = GoogleSignIn.getLastSignedInAccount(this);
        if(gAcc != null){
            userid = gAcc.getId();
        }else{
            userid = pref.getString("firebaseUserId","1");
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");

        DatabaseReference myDbRef = database.getReference("usersInformation").child(userid).child("UserAlarms");

        //Prepping the geocoder to get the Location of the Pin
        Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
        try {
            Address locationAddress = geocoder.getFromLocation(selectedLatitude,selectedLongtitude, 1).get(0);
            if(selectedLatitude == -1 || selectedLongtitude == -1 ){
                locTxt.setText(" ");
            }else if(locationAddress == null){
                locTxt.setText("Street Name Unknown");
            }else{
                locTxt.setText(locationAddress.getAddressLine(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e){
            locTxt.setText(" ");
            e.printStackTrace();
        }

        //Values that are not passed in are null, these snippets handle the setting of the EditText's
        if(getIntent().getStringExtra("title") != null){
            titleTxt.setText(getIntent().getStringExtra("title"));
        }
        if(getIntent().getStringExtra("desc") != null){
           descriptionTxt.setText(getIntent().getStringExtra("desc"));
        }



        Calendar cal = Calendar.getInstance();

        if(selectedDate != -1) {
            cal.setTimeInMillis(selectedDate);
        }

        timeTxt.setText(timeFormat.format(cal.getTime()));
        dateTxt.setText(dateFormat.format(cal.getTime()));


        //This prevents Focusing
        timeTxt.setInputType(InputType.TYPE_NULL);
        dateTxt.setInputType(InputType.TYPE_NULL);


        locTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LocationPicker.class);

                //This passes information to the next activity
                //The flow looks like this
                //AddNewTask (+title,+desc,+unixTime) --> LocationPicker (title,desc,unixTime,+location) ---> AddNewTask (title,desc,unixTime,location)
                //
                intent.putExtra("title",titleTxt.getText().toString());
                intent.putExtra("desc",descriptionTxt.getText().toString());
                intent.putExtra("unixTime",cal.getTimeInMillis());
                intent.putExtra("edit",getIntent().getBooleanExtra("edit",false));
                intent.putExtra("uid",getIntent().getStringExtra("uid"));
                startActivity(intent);
            }
        });

        dateTxt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int doM = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog picker;
                picker = new DatePickerDialog(AddNewTaskActivity.this,
                        new DatePickerDialog.OnDateSetListener(){//This creates a dialog where the user can choose the Date
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                dateTxt.setText(dayOfMonth+"/"+month+1+"/"+year);
                                cal.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                                cal.set(Calendar.MONTH,month);
                                cal.set(Calendar.YEAR,year);
                            }
                        },year,month,doM);
                picker.show();
            }
        });


        timeTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog picker;
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minutes = cal.get(Calendar.MINUTE);
                picker = new TimePickerDialog(AddNewTaskActivity.this,
                        new TimePickerDialog.OnTimeSetListener(){//This creates a dialog where the user can choose the Time
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                timeTxt.setText(hourOfDay + ":" + minute);
                                cal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                cal.set(Calendar.MINUTE,minute);
                            }
                        },hour,minutes,true);
                picker.show();
            }
        });

        submitBtn.setOnClickListener(v -> {

            //Here we pass in the Calendar object which we modified in the Dialogs.
            if(userid != "1" && !(getIntent().getBooleanExtra("edit",true))) {

                String alphabet = "123456789";

                // create random string builder
                StringBuilder sb = new StringBuilder();

                // create an object of Random class
                Random random = new Random();

                // specify length of random string
                int length = 21;

                for(int i = 0; i < length; i++) {

                    // generate random index number
                    int index = random.nextInt(alphabet.length());

                    // get character specified by index
                    // from the string
                    char randomChar = alphabet.charAt(index);

                    // append the character to string builder
                    sb.append(randomChar);
                }

                String randomString = sb.toString();

                Alarm newAlarm = new Alarm(titleTxt.getText().toString(),descriptionTxt.getText().toString(),selectedLongtitude,selectedLatitude,cal.getTimeInMillis()/1000L,randomString);
                myDbRef.child(randomString).setValue(newAlarm);
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);

            }else if(getIntent().getBooleanExtra("edit",false)){
                Map<String, Object> postValues = new HashMap<String,Object>();
                myDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                             for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                             Alarm alarm = snapshot.getValue(Alarm.class);
                             if((Uid).equals(alarm.getUid())){

                              postValues.put("title",titleTxt.getText().toString());
                              postValues.put("description",descriptionTxt.getText().toString());
                              postValues.put("longitude",selectedLongtitude);
                              postValues.put("latitude",selectedLatitude);
                              postValues.put("unixTime",cal.getTimeInMillis()/1000L);
                              myDbRef.child(Uid).updateChildren(postValues);
                             } } }
                             @Override
                             public void onCancelled(DatabaseError databaseError) {

                             }});

                if(getIntent().getBooleanExtra("usedLocationPicker",false)){
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                }else{
                    finish();
                }

            }else{
                Toast.makeText(getApplication(),"You are not Logged in",Toast.LENGTH_LONG).show();
            }

        });

        BottomNavigationView botNavView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        botNavView.getMenu().getItem(2).setChecked(true);//Set Middle(Home) to checked
        botNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            public boolean onNavigationItemSelected(@NonNull MenuItem item){
                switch(item.getItemId()){
                    case R.id.location:
                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.calendar:
                        intent = new Intent(getApplicationContext(), ScheduleActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.home:
                        intent = new Intent(getApplicationContext(),HomeActivity.class);
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
            };
        });
    }

}



