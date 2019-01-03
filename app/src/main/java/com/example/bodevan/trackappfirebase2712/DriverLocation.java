package com.example.bodevan.trackappfirebase2712;

public class DriverLocation {

    public double latitude;
    public double longitude;
    public String timestamp;


    public DriverLocation() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public DriverLocation(double latitude, double longitude, String timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }


}
