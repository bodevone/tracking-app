package com.example.bodevan.trackappfirebase2712;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    ImageView header;
    NavigationView navigationView;
    private String driverForUser;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private String uid;

    private String stateRole;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDriversDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            uid = user.getUid();
        }


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDriversDatabaseReference = mFirebaseDatabase.getReference().child("auth").child("drivers");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("auth").child("users");

        //Checking if your email in a list of drivers
        mDriversDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot driver : dataSnapshot.getChildren()) {
                    String value = String.valueOf(driver.getValue());
                    if (value.equals(uid)) {
                        stateRole = "driver";
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //Checking if your username in a list of drivers and if yes then add driver id
        mUsersDatabaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    driverForUser = String.valueOf(dataSnapshot.child("driver").getValue());
                    stateRole = "user";
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#FDBE38"));
        setSupportActionBar(toolbar);

        navigationView = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.drawer_layout);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.text_color));
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.openDrawer(GravityCompat.START);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new InitialFragment()).commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_map:
                enterMap();
                break;
            case R.id.nav_feedback:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FeedbackFragment()).commit();
                break;
            case R.id.nav_info:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new InfoFragment()).commit();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
        }

        header = findViewById(R.id.header);

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterMap();
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager manager = getSupportFragmentManager();
            if (manager.findFragmentById(R.id.fragment_container) instanceof FeedbackFragment) {
                enterMap();
            } else if (manager.findFragmentById(R.id.fragment_container) instanceof InfoFragment) {
                enterMap();
            }
            else {
                super.onBackPressed();
            }

        }
    }

    public void enterMap(){
        Bundle bundle = new Bundle();
        bundle.putString("driver", uid);
        bundle.putString("driver_for_user", driverForUser);
        MapsDriverFragment fragDriver = new MapsDriverFragment();
        MapsUserFragment fragUser = new MapsUserFragment();
        fragDriver.setArguments(bundle);
        fragUser.setArguments(bundle);
        if (stateRole.equals("driver")) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    fragDriver).commit();
        } else if (stateRole.equals("user")){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    fragUser).commit();
        }
        navigationView.setCheckedItem(R.id.nav_map);
    }
}