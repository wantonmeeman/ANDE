package com.example.ca1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.net.Network;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.os.Vibrator;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
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
public class TaskDetails extends AppCompatActivity implements AsyncResponse {

    //This Snippet changes the UI when the user backbtn's
    private int mLastDayNightMode;

    protected void onRestart() {
        super.onRestart();
        if (AppCompatDelegate.getDefaultNightMode() != mLastDayNightMode) {
            recreate();
        }
    }

    private static class GetGmapStaticAsync extends AsyncTask<URL, Integer, Bitmap> {//Async Task

        public AsyncResponse delegate = null;

        public GetGmapStaticAsync(AsyncResponse delegate){
                this.delegate = delegate;
        }

        @Override
        protected Bitmap doInBackground(URL... urls) {
            Bitmap bmp = null;

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) urls[0].openConnection();
                InputStream in = null;
                in = new BufferedInputStream(urlConnection.getInputStream());
                bmp = BitmapFactory.decodeStream(in);
                in.close();
                urlConnection.disconnect();
            }catch(IOException e){
                e.printStackTrace();
            }
            return bmp;

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            delegate.processFinish(result);
        }
    }

    @Override
    public void processFinish(Bitmap output) {
        ImageView locationImg = findViewById(R.id.location);
        locationImg.setImageBitmap(output);
    }

    public Bitmap getGoogleMapThumbnail(double lati, double longi,Context context){
        Bitmap bmp = null;

        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/staticmap?center="+lati+",+"+longi+"&zoom=13&scale=2&size=444x190&maptype=roadmap&key=AIzaSyA-voF_yzWD4p1vGcrFNjZ6aKIo-2ep8YE&format=png&visual_refresh=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:%7C"+lati+",+"+longi);
            new GetGmapStaticAsync(new AsyncResponse(){
                @Override
                public void processFinish(Bitmap output) {
                    ImageView locationImg = findViewById(R.id.location);
                    Bitmap croppedBitmap = Bitmap.createBitmap(output,0,0,output.getWidth(),output.getHeight()-40);
                    locationImg.setImageBitmap(croppedBitmap);
                }
            }).execute(url);
        } catch (NetworkOnMainThreadException e){

            e.printStackTrace();
            Drawable drawable = ContextCompat.getDrawable(context,R.drawable.location_pin_inactive);
            bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            drawable.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
            drawable.draw(canvas);

            return bmp;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bmp;
    }

//    GetGmapStaticAsync asyncTask =new GetGmapStaticAsync();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        asyncTask.delegate = this;
        this.getSupportActionBar().hide();

        //Handles Date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("kk:mm");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_details);

        //Handles Login
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
        TextView descriptionTxt = findViewById(R.id.descriptionTxt);
        TextView timeTxt = findViewById(R.id.time);
        TextView dateTxt = findViewById(R.id.date);
        TextView locationHeaderText = findViewById(R.id.locNotifText);
        //TextView locationTxt = findViewById(R.id.location);
        ImageView locationImg = findViewById(R.id.location);
        final double[] selectedLatitude = {0};
        final double[] selectedLongitude = {0};

        String Uid = getIntent().getStringExtra("uid");//Uid meaning Alarm id

        if(getIntent().getBooleanExtra("cancelNotification",false)){
            Vibrator vibrator = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.cancel();
        }


        Calendar cal = Calendar.getInstance();

        MaterialButton delBtn = findViewById(R.id.delBtn);

        dateTxt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                long dateUnixTime = 0;
                try {
                    dateUnixTime = dateFormat.parse((String)dateTxt.getText()).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(), MonthlyScheduleActivity.class);
                intent.putExtra("datetime",dateUnixTime);
                startActivity(intent);
            }
        });

        //Handles deleting of the Alarm
        delBtn.setOnClickListener(new View.OnClickListener() {
            boolean delButtonExpanded = false;
            @Override
            public void onClick(View v) {
                //v.animate().translationX(70*2f);
//                v.setPadding(100,0,0,0);
                if(!delButtonExpanded) {
                    float factor = v.getContext().getResources().getDisplayMetrics().density;
                    MaterialButton matButton = ((MaterialButton) v);
                    matButton.setIconPadding(Math.round(factor * 15));
                    matButton.setIcon(ContextCompat.getDrawable(getApplication(), R.drawable.calendar_icon));
                    ResizeWidthAnimation anim = new ResizeWidthAnimation(matButton, 550);
                    anim.setDuration(500);
                    anim.setAnimationListener(new Animation.AnimationListener(){
                        @Override
                        public void onAnimationStart(Animation arg0){
                            matButton.setText("Confirm Delete");
                        }
                        @Override
                        public void onAnimationRepeat(Animation arg0) {
                        }
                        @Override
                        public void onAnimationEnd(Animation arg0) {

                        }
                    });
                    matButton.startAnimation(anim);
                    delButtonExpanded = true;
                }else{
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);

                    myDbRef.child(Uid).removeValue();
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent intent1 = new Intent(TaskDetails.this,ReminderBroadcast.class);
                    intent1.putExtra("alarmTitle",titleTxt.getText());
                    intent1.putExtra("alarmDescription",descriptionTxt.getText());
                    intent1.putExtra("alarmID",Uid);

                    alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(),pref.getInt(Uid,0),intent1,PendingIntent.FLAG_UPDATE_CURRENT));
                    SharedPreferences.Editor editor = pref.edit();
                    editor.remove(Uid);
                    editor.commit();
                }

//                v.setLayoutParams(new LinearLayout.LayoutParams(v.getWidth()*4,v.getHeight()));//(int)(100*factor)));

//                new Handler().postDelayed(new Runnable() {//Using a handler works, for some reason.....
//                    @Override
//                    public void run() {
//                        //Creates an Alert Dialog to confirm the user's Action
//                        AlertDialog.Builder builder1 = new AlertDialog.Builder(TaskDetails.this);
//                        builder1.setMessage("Are you sure you want to delete this?");
//                        builder1.setCancelable(true);
//
//                        builder1.setPositiveButton(
//                                "Yes",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                                        startActivity(intent);
//
//                                        myDbRef.child(Uid).removeValue();
//                                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                                        Intent intent1 = new Intent(TaskDetails.this,ReminderBroadcast.class);
//                                        intent1.putExtra("alarmTitle",titleTxt.getText());
//                                        intent1.putExtra("alarmDescription",descriptionTxt.getText());
//                                        alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(),pref.getInt(Uid,0),intent1,PendingIntent.FLAG_UPDATE_CURRENT));
//                                        SharedPreferences.Editor editor = pref.edit();
//                                        editor.remove(Uid);
//                                        editor.commit();
//                                        dialog.cancel();
//                                    }
//                                });
//
//                        builder1.setNegativeButton(
//                                "No",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        dialog.cancel();
//                                    }
//                                });
//
//                        AlertDialog alert11 = builder1.create();
//                        alert11.show();
//                    }
//                },100);

            }
        });
        //Back Button Handling
        ImageButton backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Handles Editing
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

        //Handles getting the Task Details and setting them to the Text
        myDbRef.child(Uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Alarm alarm = dataSnapshot.getValue(Alarm.class);
                if(alarm != null) {//This handles when the user deletes the object
                    //Sets text
                    titleTxt.setText(alarm.getTitle());
                    descriptionTxt.setText(alarm.getDescription());
                    cal.setTimeInMillis(alarm.getUnixTime() * 1000L);
                    timeTxt.setText(timeFormat.format(cal.getTime()));
                    dateTxt.setText(dateFormat.format(cal.getTime()));

                    //Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
                    try {
//                        Bitmap croppedBitmap = null;
                        selectedLatitude[0] = alarm.getLatitude();
                        selectedLongitude[0] = alarm.getLongitude();
                        if(selectedLatitude[0] == -1 && selectedLongitude[0] == -1) {
//                            Drawable drawable = ContextCompat.getDrawable(getApplication(),R.drawable.location_pin_inactive);
//                            googleMapBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
//                            Canvas canvas = new Canvas(googleMapBitmap);
//                            drawable.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
//                            drawable.draw(canvas);
                            locationImg.setVisibility(View.INVISIBLE);
                            locationHeaderText.setVisibility(View.INVISIBLE);
                        }else{
                            Bitmap googleMapBitmap;
                            googleMapBitmap = getGoogleMapThumbnail(selectedLatitude[0], selectedLongitude[0], getApplicationContext());
                            locationImg.setImageBitmap(googleMapBitmap);
                        }


                        //Address locationAddress = geocoder.getFromLocation(alarm.getLatitude(), alarm.getLongitude(), 1).get(0);
                        //locationTxt.setText(locationAddress.getAddressLine(0));
                    } catch (IndexOutOfBoundsException e) {//If a location cannot be found.
                        //locationTxt.setText(" ");
                        e.printStackTrace();
                    }
                    if (alarm.getUnixTime() * 1000L < System.currentTimeMillis()) {
                        editBtn.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        //Bottom navigation Handling
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