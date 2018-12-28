package com.example.bodevan.trackappfirebase2712;

public class Account {

    public String username;
    public String password;
    public String role;
    public String driver;


    public Account() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Account(String username, String password, String role, String driver) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.driver = driver;
    }


}
