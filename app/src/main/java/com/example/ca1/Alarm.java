package com.example.ca1;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Alarm  {
    private String Title;
    private String Description;
    private String Location;
    private long UnixTime;

    public Alarm(String title,String description,String location,long unixtime){
        Title = title;
        Description = description;
        Location = location;
        UnixTime = unixtime;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public long getUnixTime() {
        return UnixTime;
    }

    public void setUnixTime(long unixTime) {
        UnixTime = unixTime;
    }

}
