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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    ImageView header;
    NavigationView navigationView;
    private String driverForUserEmail;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private String uid;
    private String email;

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
            email = user.getEmail();
        }


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDriversDatabaseReference = mFirebaseDatabase.getReference().child("auth").child("drivers");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("auth").child("users");

        //Checking if your email in a list of drivers
        mDriversDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String value = String.valueOf(childSnapshot.child("driver").getValue());

                    if (value.equals(email)) {
                        stateRole = "driver";
                        enterDriverMap();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //Checking if your username in a list of users
        mUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String value = String.valueOf(childSnapshot.child("user").getValue());
                    if (value.equals(email)) {
                        driverForUserEmail = String.valueOf(childSnapshot.child("driver").getValue());
                        stateRole = "user";
                        enterUserMap();
                    }
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
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_map:
                decideMap();
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
                decideMap();
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
                decideMap();
            } else if (manager.findFragmentById(R.id.fragment_container) instanceof InfoFragment) {
                decideMap();
            } else {
                super.onBackPressed();
            }

        }
    }

    public void enterDriverMap() {
        Bundle bundle = new Bundle();
        bundle.putString("driver", email);
        MapsDriverFragment fragDriver = new MapsDriverFragment();
        fragDriver.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                fragDriver).commit();
        navigationView.setCheckedItem(R.id.nav_map);
    }

    public void enterUserMap() {
        Bundle bundle = new Bundle();
        bundle.putString("driver_for_user", driverForUserEmail);
        MapsUserFragment fragUser = new MapsUserFragment();
        fragUser.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                fragUser).commit();
        navigationView.setCheckedItem(R.id.nav_map);
    }

    public void decideMap() {
        if (stateRole.equals("driver")) {
            enterDriverMap();
        } else if (stateRole.equals("user")) {
            enterUserMap();
        }
    }
}