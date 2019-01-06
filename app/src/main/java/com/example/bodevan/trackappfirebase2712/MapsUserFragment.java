package com.example.bodevan.trackappfirebase2712;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapsUserFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFrag;
    Marker currentLocationMarker;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDriverLocationsDatabeReference;
    private DatabaseReference mDriverPinsDatabaseReference;

    private String driverForUser;

    private boolean zoomed = false;

    final private int height = 500;
    final private int width = 500;

    private TextView onlineTime;
    private ImageView zoom;

    LatLng latLng;
    ArrayList<LatLng> pins;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_maps, container, false);

        mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFrag == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFrag = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mapFrag).commit();
        }
        mapFrag.getMapAsync(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDriverLocationsDatabeReference = mFirebaseDatabase.getReference().child("driver-locations");
        mDriverPinsDatabaseReference = mFirebaseDatabase.getReference().child("driver-pins");

        Bundle bundle = this.getArguments();
        driverForUser = bundle.getString("driver_for_user");

        onlineTime = v.findViewById(R.id.lastonline);
        zoom = v.findViewById(R.id.zoom);

        updateDatabase();

        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawPins();
    }

    public void drawPins() {
        DatabaseReference ref = mDriverPinsDatabaseReference.child(driverForUser);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //LatLng location = (LatLng) snapshot.getValue();
                    double latitude = (double) snapshot.child("latitude").getValue();
                    double longitude = (double) snapshot.child("longitude").getValue();
                    //Toast.makeText(getActivity(), String.valueOf(latitude), Toast.LENGTH_LONG).show();
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void updateDatabase() {

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DriverLocation info = dataSnapshot.getValue(DriverLocation.class);
                drawMarker(info.latitude, info.longitude);
                onlineTime.setText("Водитель был в сети: " + info.timestamp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mDriverLocationsDatabeReference.child(driverForUser).addValueEventListener(listener);
    }

    public void drawMarker(double driverLat, double driverLon) {
        //mMap.clear();
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.car);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        LatLng currentLocation = new LatLng(driverLat, driverLon);
        latLng = new LatLng(driverLat, driverLon);

        currentLocationMarker = mMap.addMarker(new MarkerOptions().flat(true)
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .anchor(0.5f, 0.5f).position(currentLocation).title("Driver Location"));

        // Move the camera instantly to hamburg with a zoom of 15.

        // Zoom in, animating the camera.
        if (!zoomed) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
            zoomed = true;
        }
        Toast.makeText(getActivity(), "Latitude = " +
                        driverLat + " " + "Longitude = " + driverLon,
                Toast.LENGTH_LONG).show();

        if (latLng != null) {
            zoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 500, null);
                }
            });
        }
    }

}
