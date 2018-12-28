package com.example.bodevan.trackappfirebase2712;

public class Driver {

    public String username;
    public double latitude;
    public double longitude;


    public Driver() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Driver(String username, double latitude, double longitude) {
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
    }


}
