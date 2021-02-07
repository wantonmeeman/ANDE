package com.example.ca1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Locale;

public class LocationPicker extends FragmentActivity implements OnMapReadyCallback {
    Marker markerName;
    private GoogleMap mMap;

    //This Snippet changes the UI when the user backbtn's
    private int mLastDayNightMode;
    protected void onRestart(){
        super.onRestart();
        if (AppCompatDelegate.getDefaultNightMode() != mLastDayNightMode) {
            recreate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Bottom Navigation Handling
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

    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {//Handles the permission response from user
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            // If request is cancelled, the result arrays are empty.
            LocationTracker loc = new LocationTracker(LocationPicker.this);
            LatLng currLocation;
            if((getIntent().getDoubleExtra("latitude", -1) == -1) || (getIntent().getDoubleExtra("longitude", -1) == -1)){
                //If no location is passed,get the user's current location
                currLocation = new LatLng(loc.getLatitude(),loc.getLongitude());
            }else{
                //Else get the location that is passed
                currLocation = new LatLng(getIntent().getDoubleExtra("latitude", 0),getIntent().getDoubleExtra("longitude", 0));
            }
            //Clears the map
            mMap.clear();
            //Sets the Current Location
            markerName = mMap.addMarker(new MarkerOptions().position(currLocation).draggable(true).title("Current Location"));
            //Move camera to current Location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocation,12));

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("Accepted","Accepted");
            } else {
                Log.i("Denied","Denied");
            }

            return;
        }else{
            Toast.makeText(getApplication(),"Permission was not given",Toast.LENGTH_LONG).show();
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LocationTracker loc = new LocationTracker(LocationPicker.this);
        LatLng currLocation;
        //If a latitude/longitude wasnt passed, use the current Location
        if((getIntent().getDoubleExtra("latitude", -1) == -1) || (getIntent().getDoubleExtra("longitude", -1) == -1)){
            currLocation = new LatLng(loc.getLatitude(),loc.getLongitude());//use Current Location
        }else{
            currLocation = new LatLng(getIntent().getDoubleExtra("latitude", 0),getIntent().getDoubleExtra("longitude", 0));//Use passed in values
        }

        final LatLng[] selectedLatLng = {currLocation};//This stores the selected Location of the marker

        //This handles the first marker, before the user touches the map

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {//Checks for Permissions
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        }else {
            markerName = mMap.addMarker(new MarkerOptions().position(currLocation).draggable(true).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocation,12));
        }
        //Moving the camera to the current Location of the User
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {

            }


            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {//This just updates the location onDrag
                LatLng latLng = marker.getPosition();
                selectedLatLng[0] = latLng;
            }
        });



        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){//Same as Drag
            Marker onClickMarkerName;
            @Override
            public void onMapClick(LatLng latLng) {
                markerName.remove();
                if(onClickMarkerName != null) {
                    onClickMarkerName.remove();
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                onClickMarkerName = mMap.addMarker(new MarkerOptions().position(latLng).draggable(true).title("Location"));
                selectedLatLng[0] = latLng;
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {//This sends the user back to the AddNewTaskPage;
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddNewTaskActivity.class);

                //New Information to pass back to the AddNewTasks Page
                intent.putExtra("latitude",selectedLatLng[0].latitude);
                intent.putExtra("longitude",selectedLatLng[0].longitude);

                //Existing Information that was previously in the AddNewTasks Page,it is receieved so it can passed back.
                intent.putExtra("title",getIntent().getStringExtra("title"));
                intent.putExtra("desc",getIntent().getStringExtra("desc"));
                intent.putExtra("unixTime",getIntent().getLongExtra("unixTime",-1));
                intent.putExtra("edit",getIntent().getBooleanExtra("edit",false));
                intent.putExtra("uid",getIntent().getStringExtra("uid"));
                intent.putExtra("usedLocationPicker",true);
                startActivity(intent);
            }
        });


    }
}