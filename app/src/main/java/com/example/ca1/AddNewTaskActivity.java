package com.example.ca1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.TextView;
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
    //Date Formats
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("kk:mm");

    //This makes the activity change its theme when restarting/backbuttoning
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

        //Variable Declaration
        EditText titleTxt = findViewById(R.id.title);
        EditText descriptionTxt = findViewById(R.id.description);
        EditText timeTxt = findViewById(R.id.Time);
        EditText dateTxt = findViewById(R.id.Date);
        EditText locTxt = findViewById(R.id.Location);
        TextView txtHeader = findViewById(R.id.textHeader);
        MaterialButton submitBtn = findViewById(R.id.submitBtn);

        //Values that were passed in thru the previous activity
        Double selectedLatitude = getIntent().getDoubleExtra("latitude",-1);
        Double selectedLongitude = getIntent().getDoubleExtra("longitude",-1);
        Long selectedDate = getIntent().getLongExtra("unixTime",-1);
        String Uid = getIntent().getStringExtra("uid");

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        //This snippet handles Login via Google or Schedular Account
        final String userid;
        GoogleSignInAccount gAcc = GoogleSignIn.getLastSignedInAccount(this);
        if(gAcc != null){
            userid = gAcc.getId();//Google Account is signed in
        }else{
            userid = pref.getString("firebaseUserId","1");//Schedular Account is signed in
        }

        //Database connection to firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");

        DatabaseReference myDbRef = database.getReference("usersInformation").child(userid).child("UserAlarms");

        //This gets the current Time right now
        Calendar cal = Calendar.getInstance();

        //Prepping the geocoder to get the Location of the Pin
        Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
        try {
            locTxt.setText( geocoder.getFromLocation(selectedLatitude,selectedLongitude, 1).get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e){//If a location cannot be found.
            locTxt.setText(" ");
            e.printStackTrace();
        }

        //Values that are not passed in are null, these snippets handle the setting of the EditText's
        if(getIntent().getStringExtra("title") != null) {
            titleTxt.setText(getIntent().getStringExtra("title"));
        }
        if(getIntent().getStringExtra("desc") != null) {
            descriptionTxt.setText(getIntent().getStringExtra("desc"));
        }
        if(selectedDate != -1) {
            cal.setTimeInMillis(selectedDate);
        }

        //Set the Time using the calendar instance, and the formatting declared at the start
        timeTxt.setText(timeFormat.format(cal.getTime()));
        dateTxt.setText(dateFormat.format(cal.getTime()));

        //This prevents Focusing, and thus solves the problem of having the double click to bring up dialog
        timeTxt.setInputType(InputType.TYPE_NULL);
        dateTxt.setInputType(InputType.TYPE_NULL);

        //Change header text and misc depending on the previous activity
        //If the previous activity was adding task, it will pass in edit = false
        if(getIntent().getBooleanExtra("edit",false)){
            txtHeader.setText("Edit Task");
            submitBtn.setText("Save");
        }else{
            txtHeader.setText("Add Task");
            submitBtn.setText("Add");
        }

        //Back Buttons programming, finish() goes to the previous activity, and doesnt add to the backstack
        ImageButton backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MaterialButton cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Bring up Location Picker when this is clicked
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

                if(!locTxt.getText().equals("")) {//If no location found,aka creating a new task.
                    intent.putExtra("latitude", selectedLatitude);
                    intent.putExtra("longitude", selectedLongitude);
                }
                startActivity(intent);
            }
        });

        //Bring up Date Dialog when this is clicked
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

        //Bring up Time Dialog when this is clicked
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
                                cal.set(Calendar.SECOND,0);
                            }
                        },hour,minutes,true);
                picker.show();
            }
        });

        //Submits the task to the server, either editing or adding it.
        submitBtn.setOnClickListener(v -> {
            //Here we pass in the Calendar object which we modified in the Dialogs.
            if(titleTxt.getText().toString() == null || titleTxt.getText().toString().equals("")){//If the Title is empty
                //Creates an alert to handle the Title emptiness, tells the user to fill it in.
                AlertDialog.Builder nullDialog = new AlertDialog.Builder(AddNewTaskActivity.this);
                nullDialog.setMessage("Title cannot be empty!");
                nullDialog.setCancelable(true);

                nullDialog.setPositiveButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog nullDialog1 = nullDialog.create();
                nullDialog1.show();
            }else {
                if (userid != "1" && !getIntent().getBooleanExtra("edit", false)) {//If adding Alarm
                    //This below snippet generates a random numerical string
                    //We use this as a Unique IDentification(UID)
                    String alphabet = "123456789";

                    // create random string builder
                    StringBuilder sb = new StringBuilder();

                    // create an object of Random class
                    Random random = new Random();

                    // specify length of random string
                    int length = 21;

                    for (int i = 0; i < length; i++) {

                        // generate random index number
                        int index = random.nextInt(alphabet.length());

                        // get character specified by index
                        // from the string
                        char randomChar = alphabet.charAt(index);

                        // append the character to string builder
                        sb.append(randomChar);
                    }

                    String randomString = sb.toString();
                    //Create a new Object using the user provided information
                    Alarm newAlarm = new Alarm(titleTxt.getText().toString(), descriptionTxt.getText().toString(), selectedLongitude, selectedLatitude, cal.getTimeInMillis() / 1000L, randomString);
                    //Sets inside the database
                    myDbRef.child(randomString).setValue(newAlarm);
                    //Redirect back to the Homescreen
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);

                } else if (getIntent().getBooleanExtra("edit", false)) {//Editing Alarm
                    Map<String, Object> postValues = new HashMap<String, Object>();
                    myDbRef.addListenerForSingleValueEvent(new ValueEventListener() {//We just want this to happen once
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {//Search through the database
                                Alarm alarm = snapshot.getValue(Alarm.class);
                                if ((Uid).equals(alarm.getUid())) {// If the unique id of the alarm being edited matches one in the database.

                                    postValues.put("title", titleTxt.getText().toString());
                                    postValues.put("description", descriptionTxt.getText().toString());
                                    postValues.put("longitude", selectedLongitude);
                                    postValues.put("latitude", selectedLatitude);
                                    postValues.put("unixTime", cal.getTimeInMillis() / 1000L);

                                    myDbRef.child(Uid).updateChildren(postValues);
                                    //Update values accordingly.
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.i("Error",databaseError.toString());
                        }
                    });

                    if (getIntent().getBooleanExtra("usedLocationPicker", false)) {
                        //If they used the location picker,
                        //send them back to homepage(calling finish() will land them into the locationpicker activity)
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                    } else {
                        finish();
                    }
                }
            }
        });

        //Bottom Nav Bar Logic
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



