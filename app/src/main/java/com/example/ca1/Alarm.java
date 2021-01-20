package com.example.ca1;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Alarm  {
    private String Title;
    private String Description;
    private long UnixTime;
    private Double Longitude;
    private Double Latitude;

    public Alarm(String title,String description,Double longitude,Double latitude,long unixtime){
        Title = title;
        Description = description;
        Latitude = latitude;
        Longitude = longitude;
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

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }

}
