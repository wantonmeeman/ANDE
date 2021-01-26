package com.example.ca1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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

    private GoogleMap mMap;

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
        setContentView(R.layout.activity_maps2);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LocationTracker loc = new LocationTracker(LocationPicker.this);
        LatLng currLocation = new LatLng(loc.getLatitude(),loc.getLongitude());

        final LatLng[] selectedLatLng = {currLocation};//This stores the selected Location of the marker

        //This handles the first marker, before the user touches the map
        Marker markerName = mMap.addMarker(new MarkerOptions().position(currLocation).draggable(true).title("Location"));
        //Moving the camera to the current Location of the User
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocation,12));

        //Prepping the geocoder to get the Location of the Pin
        Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
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

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
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
                try {
                    //Finding the Closest Address
                    Address locationAddress = geocoder.getFromLocation(selectedLatLng[0].latitude,selectedLatLng[0].longitude, 1).get(0);
                    //If there is no good address, send null so we can process it later
                    if(locationAddress.getAddressLine(0) == null){
                        intent.putExtra("address",(String)null);
                    }else{
                        intent.putExtra("address",locationAddress.getAddressLine(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e){//If there is no good address, send null so we can process it later
                    intent.putExtra("address",(String)null);
                }
                //New Information to pass back to the AddNewTasks Page
                intent.putExtra("latitude",selectedLatLng[0].latitude);
                intent.putExtra("longtitude",selectedLatLng[0].longitude);

                //Existing Information that was previously in the AddNewTasks Page,it is receieved so it can passed back.
                intent.putExtra("title",getIntent().getStringExtra("title"));
                intent.putExtra("desc",getIntent().getStringExtra("desc"));
                intent.putExtra("unixTime",getIntent().getLongExtra("unixTime",-1));
                startActivity(intent);
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