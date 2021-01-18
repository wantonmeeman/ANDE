package com.example.ca1;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Alarm  {
    private String Title;
    private String Description;
    private long UnixTime;
    private String LocationX;
    private String LocationY;

    public Alarm(String title,String description,String locationX,String locationY,long unixtime){
        Title = title;
        Description = description;
        LocationY = locationY;
        LocationX = locationX;
        UnixTime = unixtime;
    }
    public Alarm(){
        //This needs to be here for firebase data reading
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

    public long getUnixTime() {
        return UnixTime;
    }

    public void setUnixTime(long unixTime) {
        UnixTime = unixTime;
    }

    public String getLocationY() {
        return LocationY;
    }

    public void setLocationY(String locationY) {
        LocationY = locationY;
    }

    public String getLocationX() {
        return LocationX;
    }

    public void setLocationX(String locationX) {
        LocationX = locationX;
    }

}
