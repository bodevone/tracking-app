package com.example.bodevan.trackappfirebase2712;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class MapsDriverFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mapFrag;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    private boolean zoomed = false;

    public static double driverLat;
    public static double driverLon;
    private String driverEmail;
    private String driverName;

    private List<Polyline> polys = new ArrayList<Polyline>();
    private List<Marker> markers = new ArrayList<Marker>();

    private TextView onlineStatus;
    private ImageView redStatus;
    private ImageView greenStatus;

    private String timeOnline;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDriverLocationsDatabeReference;
    private DatabaseReference mDriverPinsDatabaseReference;

    private ValueEventListener listenerPins;
    private ValueEventListener listenTime;
    private ValueEventListener listenerDelete;
    private Handler hand;
    private Runnable run;

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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDriverLocationsDatabeReference = mFirebaseDatabase.getReference().child("driver-locations");
        mDriverPinsDatabaseReference = mFirebaseDatabase.getReference().child("driver-pins");

        onlineStatus = v.findViewById(R.id.onlineStatus);
        redStatus = v.findViewById(R.id.redStatus);
        greenStatus = v.findViewById(R.id.greenStatus);

        Bundle bundle = this.getArguments();
        driverEmail = bundle.getString("driver");
        driverName = driverEmail.substring(0, driverEmail.indexOf("@"));

        findTime();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            return;
//        this.mFusedLocationClient.requestLocationUpdates(this.mLocationRequest, this.mLocationCallback, Looper.myLooper());
        if (hand != null)
            hand.postDelayed(run, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
//        if (mFusedLocationClient != null)
//            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        if (hand != null)
            hand.removeCallbacks(run);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        if (listenerPins != null)
            mDriverPinsDatabaseReference.child(driverName).removeEventListener(listenerPins);
        if (listenTime != null)
            mDriverLocationsDatabeReference.child(driverName).removeEventListener(listenTime);
        if (listenerDelete != null)
            mDriverPinsDatabaseReference.child(driverName).removeEventListener(listenerDelete);
        if (hand != null)
            hand.removeCallbacks(run);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

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

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }

        drawPins();

        compareTime();
    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);

                if (location != null) {
                    //Place current location marker
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // Zoom in, animating the camera.
                    if (!zoomed) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
                        zoomed = true;
                    }

                    driverLat = location.getLatitude();
                    driverLon = location.getLongitude();
                    removePins();

                    databaseUpdate(driverLat, driverLon);
                }
            }
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
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
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void databaseUpdate(final double lat, final double lon) {
        Locale loc = new Locale("ru", "RU");

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, loc);
        String date = dateFormat.format(new Date());

        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, loc);
        String time = timeFormat.format(new Date());

        String timestamp = date + " " + time;
        DriverLocation driverInfo = new DriverLocation(lat, lon, timestamp);

        mDriverLocationsDatabeReference.child(driverName).setValue(driverInfo);
    }

    public void drawPins() {
        listenerPins = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int num = 1;
                    if (markers != null) {
                        for (int i = 0; i < markers.size(); i++) {
                            markers.get(i).remove();
                        }
                        markers.clear();
                    }

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (String.valueOf(snapshot.child("used").getValue()).equals("0")) {
                            double latitude = (double) snapshot.child("latitude").getValue();
                            double longitude = (double) snapshot.child("longitude").getValue();
                            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                    .title(String.valueOf(num)));
                            if (num == 1) {
                                marker.setSnippet("Следующая точка");
                                marker.showInfoWindow();
                            }
                            markers.add(marker);
                            num += 1;

                        }
                    }

                    if (polys != null) {
                        for (int i = 0; i < polys.size(); i++) {
                            polys.get(i).remove();
                        }
                        polys.clear();
                    }
                    drawPath(markers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mDriverPinsDatabaseReference.child(driverName).addValueEventListener(listenerPins);
    }

    public void findTime() {
        listenTime = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    timeOnline = (String) dataSnapshot.child("timestamp").getValue();
                } else {
                    onlineStatus.setText("ВЫ ЕЩЕ НЕ ВКЛЮЧИЛИ GPS!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        mDriverLocationsDatabeReference.child(driverName).addValueEventListener(listenTime);
    }

    private void compareTime() {
        hand = new Handler();
        run = new Runnable() {
            @Override
            public void run() {
                if (timeOnline != null) {
                    findAndCompare();
                }
                hand.postDelayed(this, 5000);
            }
        };
        hand.postDelayed(run, 5000);
    }

    public void findAndCompare() {
        String timeValue = timeOnline.substring(timeOnline.indexOf("г.") + 3, timeOnline.length() - 3);
        String one = timeValue.substring(timeValue.indexOf(":") + 1);
        String hours = timeValue.substring(0, timeValue.indexOf(":"));
        String minutes = one.substring(0, one.indexOf(":"));
        String seconds = one.substring(one.indexOf(":") + 1);
        int secOne = Integer.valueOf(hours) * 3600 + Integer.valueOf(minutes) * 60 + Integer.valueOf(seconds);

        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT);
        String currentTime = timeFormat.format(new Date());
        String two = currentTime.substring(currentTime.indexOf(":") + 1, currentTime.indexOf(" "));
        String h = currentTime.substring(0, currentTime.indexOf(":"));
        String m = two.substring(0, two.indexOf(":"));
        String s = two.substring(two.indexOf(":") + 1);
        int secTwo = Integer.valueOf(h) * 3600 + Integer.valueOf(m) * 60 + Integer.valueOf(s);

        if (Math.abs(secOne - secTwo) < 15) {
            onlineStatus.setText("ВЫ В СЕТИ!");
            redStatus.setVisibility(View.GONE);
            greenStatus.setVisibility(View.VISIBLE);
        } else {
            onlineStatus.setText("ВЫ НЕ В СЕТИ!");
            greenStatus.setVisibility(View.GONE);
            redStatus.setVisibility(View.VISIBLE);
        }
    }

    private void drawPath(List<Marker> markersToDraw) {
        LatLng posit1;
        LatLng posit2;
        for (int i = 0; i < markersToDraw.size() - 1; i++) {
            posit1 = markersToDraw.get(i).getPosition();
            posit2 = markersToDraw.get(i + 1).getPosition();
            drawPathBetweenTwoPoints(posit1, posit2, markersToDraw.get(i + 1));
        }

    }

    private void drawPathBetweenTwoPoints(LatLng pointOne, LatLng pointTwo, final Marker two) {
        GoogleDirection.withServerKey("AIzaSyD1ealwua5d0IHHlqcO-t05jnY2sWV4CiU")
                .from(pointOne)
                .to(pointTwo)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);
                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getActivity(), directionPositionList, 5, Color.RED);
                            Polyline poly = mMap.addPolyline(polylineOptions);
                            polys.add(poly);
                            Info durationInfo = leg.getDuration();
                            String duration = durationInfo.getText();
                            two.setSnippet(duration);
                        } else {
                            // Do something
                            Toast.makeText(getActivity(), "Проблема в Прорисовывании пути", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something
                    }
                });
    }

    public void removePins() {
        listenerDelete = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        double latitude = (double) snapshot.child("latitude").getValue();
                        double longitude = (double) snapshot.child("longitude").getValue();
                        double distance = Math.sqrt((driverLat - latitude) * (driverLat - latitude) + (driverLon - longitude) * (driverLon - longitude));
                        if (distance < 5.0E-4) {
                            mDriverPinsDatabaseReference.child(driverName).child(snapshot.getKey()).child("used").setValue(1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        mDriverPinsDatabaseReference.child(driverName).addValueEventListener(listenerDelete);
    }
}

