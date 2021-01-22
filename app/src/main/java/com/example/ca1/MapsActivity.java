 package com.example.ca1;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("hh:mm - dd/MM/yy");
        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
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

        Log.i("CheckingLocation", String.valueOf(loc.canGetLocation()));
        Log.i("Latitude",Double.toString(loc.getLatitude()));
        Log.i("Longitude",Double.toString(loc.getLongitude()));

        myDbRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int selectedDrawable = 0;
                int x = 0;
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                mMap.clear();
                ArrListAlarm.clear();

                mMap.addMarker(new MarkerOptions().position(
                        new LatLng(loc.getLatitude(),loc.getLongitude())
                        ).title(
                        "currentLocation"
                        ).icon(
                        bitmapDescriptorFromVector(getApplicationContext(),R.drawable.current_user_icon))
                ).setTag(0);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    x++;
                    Alarm alarm = snapshot.getValue(Alarm.class);
                    if(System.currentTimeMillis()/1000L < alarm.getUnixTime()) {
                        selectedDrawable = R.drawable.location_pin;
                    }else{
                        selectedDrawable = R.drawable.location_pin_inactive;
                    }
                    ArrListAlarm.add(new Alarm(alarm.getTitle(), alarm.getDescription(), alarm.getLongitude(),alarm.getLongitude(), alarm.getUnixTime() * 1000L));
                    mMap.addMarker(new MarkerOptions().position(
                            new LatLng(alarm.getLatitude(),alarm.getLongitude())
                    ).title(
                            alarm.getTitle()
                    ).icon(
                            bitmapDescriptorFromVector(getApplicationContext(),selectedDrawable))
                    ).setTag(x);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Error",error.toString());
                // Failed to read value
            }
        });
        LatLng currentLoc = new LatLng(loc.getLatitude(),loc.getLongitude());
        //mMap.moveCamera();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc,12));
        Log.i("curLoc",Double.toString(loc.getLatitude()));
        Log.i("curLoc",Double.toString(loc.getLongitude()));
        //current Location Marker
        mMap.addMarker(new MarkerOptions().position(
                new LatLng(loc.getLatitude(),loc.getLongitude())
        ).title(
                "currentLocation"
        ).icon(
                bitmapDescriptorFromVector(getApplicationContext(),R.drawable.msg_icon))
        ).setTag(0);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){
            @Override
            public boolean onMarkerClick(Marker marker) {

                if((int)marker.getTag() != 0){
                    txtTimeDate.setVisibility(View.VISIBLE);
                    String string = "";
                    Alarm alarmObj = ArrListAlarm.get((int)(marker.getTag())-1);
                    Log.i("markerTag",marker.getTag().toString());
                    Log.i("markerTag",Long.toString(alarmObj.getUnixTime()));

                    txtTimeDate.setText(dateTimeFormat.format(new Date((long)alarmObj.getUnixTime())));
                    txtTitle.setText(marker.getTitle());

                    if(System.currentTimeMillis() < alarmObj.getUnixTime()) {
                        editTaskBtn.setVisibility(View.VISIBLE);
                        string = "Event is Active";
                    }else {
                        editTaskBtn.setVisibility(View.INVISIBLE);
                        string = "Event is not Active";
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


}