package com.example.ca1;

public class Alarm {
    private String Title;
    private String Description;
    private String Location;
    private int UnixTime;

    public Alarm(String title,String description,String location,int unixtime){
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

    public int getUnixTime() {
        return UnixTime;
    }

    public void setUnixTime(int unixTime) {
        UnixTime = unixTime;
    }
}
