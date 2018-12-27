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


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    ImageView header;
    NavigationView navigationView;
    public Boolean state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        state = getIntent().getExtras().getBoolean("state");

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

        if (state){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MapsDriverFragment()).commit();
        }
        else{
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MapsUserFragment()).commit();
        }
        navigationView.setCheckedItem(R.id.nav_map);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_map:
                if (state){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new MapsDriverFragment()).commit();
                }
                else{
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new MapsUserFragment()).commit();
                }
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
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
        }

        header = findViewById(R.id.header);

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new MapsDriverFragment()).commit();
                }
                else{
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new MapsUserFragment()).commit();
                }
                navigationView.setCheckedItem(R.id.nav_map);
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
                if (state){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new MapsDriverFragment()).commit();
                }
                else{
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new MapsUserFragment()).commit();
                }
                navigationView.setCheckedItem(R.id.nav_map);
            } else if (manager.findFragmentById(R.id.fragment_container) instanceof InfoFragment) {
                if (state){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new MapsDriverFragment()).commit();
                }
                else{
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new MapsUserFragment()).commit();
                }
                navigationView.setCheckedItem(R.id.nav_map);
            }
            else{
                super.onBackPressed();
            }

        }
    }
}