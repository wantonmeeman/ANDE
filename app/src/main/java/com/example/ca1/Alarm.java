package com.example.ca1;

import java.util.*;
import java.util.ArrayList;

public class Alarm {
    private String Title;
    private String Description;
    private long UnixTime;
    private Double Longitude;
    private Double Latitude;
    private String Uid;

    public Alarm(String title,String description,Double longitude,Double latitude,long unixtime,String uid){
        Title = title;
        Description = description;
        Latitude = latitude;
        Longitude = longitude;
        UnixTime = unixtime;
        Uid = uid;
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

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }


}
