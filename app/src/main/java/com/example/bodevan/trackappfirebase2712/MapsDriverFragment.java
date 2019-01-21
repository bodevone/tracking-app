package com.example.bodevan.trackappfirebase2712;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MapsDriverFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    SupportMapFragment mapFrag;

    private FusedLocationProviderClient mFusedLocationClient;

    private boolean zoomed = false;

    public static double driverLat;
    public static double driverLon;
    private String username;
    private String uid;
    private String driverEmail;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDriverLocationsDatabeReference;
    private DatabaseReference mDriverPinsDatabaseReference;
    private FirebaseUser user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_driver_maps, container, false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFrag == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFrag = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mapFrag).commit();
        }
        mapFrag.getMapAsync(this);
        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDriverLocationsDatabeReference = mFirebaseDatabase.getReference().child("driver-locations");
        mDriverPinsDatabaseReference = mFirebaseDatabase.getReference().child("driver-pins");

        Bundle bundle = this.getArguments();
        driverEmail = bundle.getString("driver");

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null &&
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //Styling
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.mapstyle));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        //drawPins();

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    public void drawPins() {
        final List<Marker> markers = new ArrayList<Marker>();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int i = 0; i < markers.size(); i++) {
                    markers.get(i).remove();
                }
                markers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double latitude = (double) snapshot.child("latitude").getValue();
                    double longitude = (double) snapshot.child("longitude").getValue();
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
                    markers.add(marker);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mDriverPinsDatabaseReference.child(driverEmail).addValueEventListener(listener);
    }

    public void removePins() {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double latitude = (double) snapshot.child("latitude").getValue();
                    double longitude = (double) snapshot.child("longitude").getValue();
                    double distance = Math.sqrt((driverLat - latitude) * (driverLat - latitude) + (driverLon - longitude) * (driverLon - longitude));
                    if(distance < 5.0E-4) {
                        mDriverPinsDatabaseReference.child(driverEmail).child(snapshot.getKey() ).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mDriverPinsDatabaseReference.child(driverEmail).addValueEventListener(listener);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(10);

        //TODO: decide on priority to be chosen - might be PRIORITY_HIGH_ACCURACY
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            // ---Get current location latitude, longitude---
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            // Zoom in, animating the camera.
            if (!zoomed) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
                zoomed = true;
            }
//            Toast.makeText(getActivity(), "Latitude = " +
//                            location.getLatitude() + " " + "Longitude = " + location.getLongitude(),
//                    Toast.LENGTH_LONG).show();

            driverLat = location.getLatitude();
            driverLon = location.getLongitude();
//            removePins();

            databaseUpdate(driverLat, driverLon);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    public void databaseUpdate(final double lat, final double lon) {
        Locale loc = new Locale("ru", "RU");

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, loc);
        String date = dateFormat.format(new Date());

        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, loc);
        String time = timeFormat.format(new Date());

        String timestamp = date +  " " + time;
        DriverLocation driverInfo = new DriverLocation(lat, lon, timestamp);

        String driverKey = driverEmail.substring(0, driverEmail.indexOf("@"));
        mDriverLocationsDatabeReference.child(driverKey).setValue(driverInfo);
    }
}

