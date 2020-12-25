package com.example.ca1;


import android.content.Intent;
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
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


public class ScheduleActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,View.OnClickListener,ViewPager.OnPageChangeListener{

    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todays_tasks_act);
        this.getSupportActionBar().hide();

        viewPager = findViewById(R.id.pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(this);

        Button Monthly = (Button) findViewById(R.id.Monthly);
        Monthly.setOnClickListener(this);

        Button Today = (Button) findViewById(R.id.Today);
        Today.setOnClickListener(this);

        ArrayList<Alarm> ArrListAlarm = new ArrayList<Alarm>();

        long JSONtime;
        JSONObject jObject;
        String JSONtitle;
        String JSONdesc;
        long tempTime;
        String tempTitle;
        String tempDesc;
        try {//Read File

            FileInputStream fin = openFileInput("JSON STORAGE");
            int c;
            String temp = "";

            while ((c = fin.read()) != -1) {
                temp = temp + (char) c;
            }
            fin.close();

            //Get JSON Object(which is an array)
            JSONObject pjObject = new JSONObject(temp);
            JSONArray jArray = pjObject.getJSONArray("Data");

            //Get the calendar Object today's date.
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            //Get Start and end of date.
            long startOfDay = cal.getTimeInMillis() / 1000;
            long endOfDay = startOfDay + 86400;

            for (int i = 0; jArray.length() > i; i++) {
                for (int j = i + 1; jArray.length() > j; j++) {
                    if (jArray.getJSONObject(i).getLong("time") > jArray.getJSONObject(j).getLong("time")) {
                        tempTime = jArray.getJSONObject(i).getLong("time");
                        tempTitle = jArray.getJSONObject(i).getString("title");
                        tempDesc = jArray.getJSONObject(i).getString("description");

                        jArray.getJSONObject(i).put("time", jArray.getJSONObject(j).getLong("time"));
                        jArray.getJSONObject(i).put("title", jArray.getJSONObject(j).getString("title"));
                        jArray.getJSONObject(i).put("description", jArray.getJSONObject(j).getString("description"));

                        jArray.getJSONObject(j).put("time", tempTime);
                        jArray.getJSONObject(j).put("title", tempTitle);
                        jArray.getJSONObject(j).put("description", tempDesc);
                    }
                }
            }

            //Loop to populate ArrListAlarm, and for seeing which tasks havent been completed yet
            int count = 0;
            for (int i = 0; jArray.length() > i; i++) {

                jObject = jArray.getJSONObject(i);
                JSONtime = jObject.getInt("time");

                if (startOfDay < JSONtime && endOfDay > JSONtime) {//Get only today's date
                    JSONtitle = jObject.getString("title");
                    JSONdesc = jObject.getString("description");
                    ArrListAlarm.add(new Alarm(JSONtitle, JSONdesc, "", JSONtime * 1000L));
                    if(JSONtime*1000L > System.currentTimeMillis()){//This should count Events that havent Happened yet
                        count++;
                    }
                }
            }

            ProgressBar todayProgressBar = (ProgressBar)findViewById(R.id.progressBar);
            TextView percentageCompletion = (TextView)findViewById(R.id.todayProgress);
            TextView completionStatus = (TextView)findViewById(R.id.CompletionStatus);
            TextView todayDate = (TextView)findViewById(R.id.todayDate);
            
            todayDate.setText(dateFormat.format(cal));

            int completedTaskPercentage = (int)Math.round(((double)count/(double)ArrListAlarm.size())*100);

            percentageCompletion.setText(Integer.toString(completedTaskPercentage)+"%");
            todayProgressBar.setProgress(completedTaskPercentage);
            completionStatus.setText("You have completed "+count+"/"+ArrListAlarm.size()+" tasks Today");
            

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ImageButton imgButton = findViewById(R.id.backButton);
        imgButton.setOnClickListener(v -> {
            Intent intent = new Intent(this,HomeActivity.class);
            startActivity(intent);
        });
        BottomNavigationView botNavView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        botNavView.getMenu().getItem(1).setChecked(true);//Set Middle(Home) to checked
        botNavView.setOnNavigationItemSelectedListener(this);
    }

    public void onClick(View v) {//Handle When the Monthly/Today buttons are clicked
            Button Monthly = (Button)findViewById(R.id.Monthly);
            Button Today = (Button)findViewById(R.id.Today);

        switch (v.getId()) {
            case R.id.Today://When Today is clicked
                viewPager.setCurrentItem(getItem(-1), true);//Move to the Today's Tab
                Today.setEnabled(false);//Change button states
                Monthly.setEnabled(true);
                break;
            case R.id.Monthly://When Monthly is clicked
                viewPager.setCurrentItem(getItem(+1), true);//Move to the Monthly's Tab
                Today.setEnabled(true);//Change button states
                Monthly.setEnabled(false);
                break;
            default:
                Log.i("Error","There has been an error");
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {//This handles when the user swipes.
        Button Monthly = (Button)findViewById(R.id.Monthly);
        Button Today = (Button)findViewById(R.id.Today);
        //Position == 0 will make the buttons flicker due to Incosistent trigger area.
        //This makes it so the buttons only change when the pager is swiped fully to the other page
        //from monthly to today
        if (position+positionOffset == 0){
            Today.setEnabled(false);
            Monthly.setEnabled(true);
        }else if(position == 1 ){
            Today.setEnabled(true);
            Monthly.setEnabled(false);
        }
    }
    //Compulsory Methods
    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
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
                    return true;
                case R.id.settings:
                    return true;

            }
            return false;
        };


}