package com.example.ca1;

import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class DemoFragment extends Fragment {
    private TextView textView;
    ArrayList<Alarm> ArrListAlarm;

    public DemoFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_demo, container, false);
//        textView = view.findViewById(R.id.textdisplay);
//        textView.setText(getArguments().getString("message"));
//
//
//        return view;
        ArrListAlarm = new ArrayList<Alarm>();
        long JSONtime;
        JSONObject jObject;
        String JSONtitle;
        String JSONdesc;
        long tempTime;
        String tempTitle;
        String tempDesc;
        try {//Read File

            FileInputStream fin = getActivity().openFileInput("JSON STORAGE");
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


            Log.i("Time", Long.toString(cal.getTimeInMillis()));

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

            //Loop to populate ArrListAlarm
            for (int i = 0; jArray.length() > i; i++) {

                jObject = jArray.getJSONObject(i);
                JSONtime = jObject.getInt("time");
                Log.i("Start of Day", Long.toString(startOfDay));
                Log.i("End of Day", Long.toString(endOfDay));
                Log.i("JSON TIME", Long.toString(JSONtime));

                if (startOfDay < JSONtime && endOfDay > JSONtime) {//Get only today's date
                    JSONtitle = jObject.getString("title");
                    JSONdesc = jObject.getString("description");
                    Log.i("Loop", "Loop" + i);
                    ArrListAlarm.add(new Alarm(JSONtitle, JSONdesc, "", JSONtime * 1000L));
                }

            }
            RecyclerView myrv = view.findViewById(R.id.recyclerViewTask);

            //Gets the Adapter from the JAVA file
            RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(getContext(), ArrListAlarm);

            //Set Layout for the RecyclerView
            myrv.setLayoutManager(new LinearLayoutManager(getContext()));

            //Set an adapter for the View
            myrv.setAdapter(myAdapter);

            //Button button = findViewById(R.id.Today);
            // button.setOnClickListener(this);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("Error", e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Error", e.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("Error", e.toString());
        }
        return view;
    }
}