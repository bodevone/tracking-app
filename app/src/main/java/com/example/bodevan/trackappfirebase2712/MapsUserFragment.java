package com.example.bodevan.trackappfirebase2712;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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

import static android.content.ContentValues.TAG;

public class MapsUserFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFrag;
    Marker currentLocationMarker;
    LatLng latLng;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDriverLocationsDatabeReference;
    private DatabaseReference mDriverPinsDatabaseReference;

    private String driverEmail;
    public String driverName;
    private boolean zoomed = false;
    private boolean firstTime = false;
    private boolean oneTime = false;

    CameraPosition driver;

    public double myLat;
    public double myLon;

    private double prevLat;
    private double prevLon;
    private LatLng prevLoc;

    final private int height = 180;
    final private int width = 180;

    private List<Polyline> polys = new ArrayList<Polyline>();
    private List<Marker> markers = new ArrayList<Marker>();

    private int total;

    private TextView onlineTime;
    private ImageView zoom;
    private TextView durationView;
    private ImageView redStatus;
    private ImageView greenStatus;

    private BitmapDrawable bitmapdraw;
    private Bitmap b;
    private Bitmap smallMarker;

    private ValueEventListener listenerLocations;
    private ValueEventListener listenerPins;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_maps, container, false);
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
        mFirebaseDatabase.goOnline();
        mDriverLocationsDatabeReference = mFirebaseDatabase.getReference().child("driver-locations");
        mDriverPinsDatabaseReference = mFirebaseDatabase.getReference().child("driver-pins");

        Bundle bundle = this.getArguments();
        driverEmail = bundle.getString("driver_for_user");
        driverName = driverEmail.substring(0, driverEmail.indexOf("@"));

        onlineTime = v.findViewById(R.id.lastonline);
        zoom = v.findViewById(R.id.zoom);
        durationView = v.findViewById(R.id.duration);
        redStatus = v.findViewById(R.id.redStatus);
        greenStatus = v.findViewById(R.id.greenStatus);

        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.carrr);
        b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(this.mLocationRequest, this.mLocationCallback, Looper.myLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        mDriverLocationsDatabeReference.removeEventListener(listenerLocations);
        mDriverPinsDatabaseReference.removeEventListener(listenerPins);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //Styling
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.mapstyle));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        updateDatabase();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

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

                    myLat = location.getLatitude();
                    myLon = location.getLongitude();
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
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
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


    private void updateDatabase() {
        listenerLocations = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    if (snap.getKey().equals(driverName)) {
                        DriverLocation info = dataSnapshot.child(driverName).getValue(DriverLocation.class);
                        drawMarker(info.latitude, info.longitude);
                        lastOnline(info.timestamp);
                    } else {
                        onlineTime.setText("Водитель еще не заходил");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDriverLocationsDatabeReference.addValueEventListener(listenerLocations);
    }

    public void lastOnline(String time) {
        String timeValue = time.substring(time.indexOf("г.") + 3, time.length() - 3);
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

        if (Math.abs(secOne - secTwo) < 20) {
            onlineTime.setTextColor(Color.parseColor("#007f00"));
            onlineTime.setText("ВОДИТЕЛЬ В ПУТИ");
        } else {
            onlineTime.setText(Html.fromHtml("<font color=red>ВОДИТЕЛЬ НЕ В СЕТИ</font><br>Был в сети " + time));
        }
    }

    public void drawPins() {
        listenerPins = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int num = 1;
                for (int i = 0; i < markers.size(); i++) {
                    markers.get(i).remove();
                }
                markers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double latitude = (double) snapshot.child("latitude").getValue();
                    double longitude = (double) snapshot.child("longitude").getValue();
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                            .title(String.valueOf(num))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    if (num == 1) {
                        marker.setSnippet("Следующая точка");
                        marker.showInfoWindow();
                    }
                    markers.add(marker);
                    num += 1;
                }
                if (polys != null) {
                    for (int i = 0; i < polys.size(); i++) {
                        polys.get(i).remove();
                    }
                    polys.clear();
                }

                drawPath(markers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mDriverPinsDatabaseReference.child(driverName).addValueEventListener(listenerPins);
    }

    private void drawPath(List<Marker> markersToDraw) {
        LatLng posit1;
        LatLng posit2;
        total = 0;

        for (int i = 0; i < markersToDraw.size() - 1; i++) {
            posit1 = markersToDraw.get(i).getPosition();
            posit2 = markersToDraw.get(i + 1).getPosition();
            drawPathBetweenTwoPoints(posit1, posit2, markersToDraw.get(i + 1));
        }

        if (markersToDraw.size() == 0)
            durationView.setText("Маршрута Нет");

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
                            if (direction != null) {
                                Route route = direction.getRouteList().get(0);
                                Leg leg = route.getLegList().get(0);
                                ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                PolylineOptions polylineOptions = DirectionConverter.createPolyline(getActivity(), directionPositionList, 5, Color.RED);
                                Polyline poly = mMap.addPolyline(polylineOptions);
                                polys.add(poly);
                                Info durationInfo = leg.getDuration();
                                String duration = durationInfo.getText();
                                two.setSnippet(duration);
                                String dur = duration.substring(0, duration.indexOf(" "));
                                total += Integer.parseInt(dur);
                                durationView.setText("Маршрут займет " + String.valueOf(total) + " мин");

                            }

                        } else {
                            Toast.makeText(getActivity(), "Проблема в uu Маршрута", Toast.LENGTH_LONG).show();
                            Log.i("ISSUE", rawBody);
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Toast.makeText(getActivity(), "Проблема в Прорисовывании Маршрута", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void drawMarker(double driverLat, double driverLon) {
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        LatLng currentLocation = new LatLng(driverLat, driverLon);
        latLng = new LatLng(driverLat, driverLon);

        currentLocationMarker = mMap.addMarker(new MarkerOptions().flat(true)
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .anchor(0.5f, 0.5f).position(currentLocation));

        // Zoom in, animating the camera.
        if (!zoomed) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
            zoomed = true;
        }

        if (latLng != null) {
            zoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    driver = new CameraPosition.Builder().target(latLng)
                            .zoom(15.5f)
                            .bearing(0)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(driver), Math.max(1000, 1), null);
                }
            });
        }

        if (firstTime) {
            double distance = Math.sqrt((driverLat - myLat) * (driverLat - myLat) + (driverLon - myLon) * (driverLon - myLon));
            if (distance < 5.0E-4) {
                if (!oneTime) {
                    sendNotification();
                    oneTime = true;
                }
            }
        }
        firstTime = true;

        prevLat = driverLat;
        prevLon = driverLon;
        prevLoc = new LatLng(prevLat, prevLon);
    }

    private void sendNotification() {
        String channel_name = "CHANNEL_NAME";
        String channel_description = "CHANNEL_DESCRIPTION";
        String CHANNEL_ID = "CHANNEL_ID";


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = channel_name;
            String description = channel_description;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(getContext(), UserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Водитель Прибыл")
                .setContentText("Ваш Водитель Вас Ожидает")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, mBuilder.build());

    }
}
