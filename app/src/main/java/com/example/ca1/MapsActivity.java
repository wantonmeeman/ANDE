 package com.example.ca1;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.ca1.LocationTracker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int mLastDayNightMode;
    Marker CurrentMarker;
    protected void onRestart(){
        super.onRestart();
        if (AppCompatDelegate.getDefaultNightMode() != mLastDayNightMode) {
            recreate();
        }
    }

    @Override
    protected void onCreate (Bundle savedInstanceState){
        this.getSupportActionBar().hide();
        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        SearchView searchView = findViewById(R.id.searchLoc);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location  = searchView.getQuery().toString();
                List<Address> addressArr = null;

                if(location != null || !location.equals("")){
                   Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        addressArr = geocoder.getFromLocationName(location,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressArr.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    //mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        BottomNavigationView botNavView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        botNavView.getMenu().getItem(0).setChecked(true);//Set Middle(Home) to checked
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
                        intent = new Intent(getApplicationContext(),SettingsActivity.class);
                        startActivity(intent);
                        return true;
                }
                return false;
            };
        });
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("hh:mm - dd/MM/yy");

        String userid = "";
        ArrayList<Alarm> ArrListAlarm = new ArrayList<Alarm>();

        GoogleSignInAccount gAcc = GoogleSignIn.getLastSignedInAccount(this);
        if(gAcc != null){
            userid = gAcc.getId();
        }else{
            userid = pref.getString("firebaseUserId","1");
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");

        DatabaseReference myDbRef = database.getReference("usersInformation").child(userid).child("UserAlarms");

        mMap = googleMap;
        LocationTracker loc;
        TextView txtTitle;
        TextView txtDesc;
        TextView txtTimeDate;
        ImageButton editTaskBtn;

        txtDesc = findViewById(R.id.alarmDesc);
        txtTitle = findViewById(R.id.alarmTitle);
        txtTimeDate = findViewById(R.id.alarmDateTime);
        editTaskBtn = findViewById(R.id.editTaskBtn);
        //Default invis, only when clicked on a valid task.
        editTaskBtn.setVisibility(View.INVISIBLE);
        txtTimeDate.setVisibility(View.INVISIBLE);

        loc = new LocationTracker(MapsActivity.this);
        LatLng currentLoc = new LatLng(loc.getLatitude(),loc.getLongitude());
        myDbRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int selectedDrawable = 0;
                int x = 0;
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                mMap.clear();
                ArrListAlarm.clear();
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
                }else {
                    CurrentMarker = mMap.addMarker(new MarkerOptions().position(
                           currentLoc
                            ).title(
                            "currentLocation"
                            ).icon(
                            bitmapDescriptorFromVector(getApplicationContext(),R.drawable.current_user_icon))
                    );
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc,12));
                }


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    x++;
                    Alarm alarm = snapshot.getValue(Alarm.class);
                    if(System.currentTimeMillis()/1000L < alarm.getUnixTime()) {
                        selectedDrawable = R.drawable.location_pin;
                    }else{
                        selectedDrawable = R.drawable.location_pin_inactive;
                    }

                        ArrListAlarm.add(new Alarm(alarm.getTitle(), alarm.getDescription(), alarm.getLongitude(), alarm.getLatitude(), alarm.getUnixTime() * 1000L, alarm.getUid()));
                    if(alarm.getLatitude() != -1 && alarm.getLongitude() != -1) {
                        mMap.addMarker(new MarkerOptions().position(
                                new LatLng(alarm.getLatitude(), alarm.getLongitude())
                                ).title(
                                alarm.getTitle()
                                ).icon(
                                bitmapDescriptorFromVector(getApplicationContext(), selectedDrawable))
                        ).setTag(x);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Error",error.toString());
                // Failed to read value
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){
            @Override
            public boolean onMarkerClick(Marker marker) {

                if((int)marker.getTag() != 0){
                    txtTimeDate.setVisibility(View.VISIBLE);
                    String string = "";
                    Alarm alarmObj = ArrListAlarm.get((int)(marker.getTag())-1);

                    txtTimeDate.setText(dateTimeFormat.format(new Date((long)alarmObj.getUnixTime())));
                    txtTitle.setText(marker.getTitle());

                    if(System.currentTimeMillis() < alarmObj.getUnixTime()) {
                        editTaskBtn.setVisibility(View.VISIBLE);
                        editTaskBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), AddNewTaskActivity.class);
                                intent.putExtra("edit",(Boolean)true);
                                intent.putExtra("uid",alarmObj.getUid());
                                intent.putExtra("title",alarmObj.getTitle());
                                intent.putExtra("desc",alarmObj.getDescription());
                                intent.putExtra("unixTime",alarmObj.getUnixTime());
                                intent.putExtra("latitude", alarmObj.getLatitude());
                                intent.putExtra("longitude", alarmObj.getLongitude());
                                startActivity(intent);
                            }
                        });
                        //string = "Event is Active";
                    }else {
                        editTaskBtn.setVisibility(View.INVISIBLE);
                        //string = "Event is not Active";
                    }
                    if(alarmObj.getDescription().length() > 150){
                        txtDesc.setText(alarmObj.getDescription().substring(0,105)+"...");
                    }else{
                        txtDesc.setText(alarmObj.getDescription()+string);
                    }
//                    txtDesc.setText(alarmObj.getDescription()+string);
                }else{

                    editTaskBtn.setVisibility(View.INVISIBLE);
                    txtTimeDate.setVisibility(View.INVISIBLE);
                    txtTitle.setText("Your Current Location");
                    txtDesc.setText("You are Here!");

                }

                return false;
            }
        });
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {//Handles the permission response from user


        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            // If request is cancelled, the result arrays are empty.
            LocationTracker loc = new LocationTracker(MapsActivity.this);
            LatLng currentLoc = new LatLng(loc.getLatitude(),loc.getLongitude());
            if(CurrentMarker != null) {
                CurrentMarker.setPosition(currentLoc);
            }else {
                CurrentMarker = mMap.addMarker(new MarkerOptions().position(currentLoc).draggable(true).title("Location"));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 12));
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("Accepted", "Accepted");
            } else {
                Log.i("Denied", "Denied");
            }

            return;
        }else{
            Toast.makeText(getApplication(),"Permission was not given",Toast.LENGTH_LONG).show();
        }


        // Other 'case' lines to check for other
        // permissions this app might request.
    }
}