package com.example.bodevan.trackappfirebase2712;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsUserFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFrag;
    Marker currentLocationMarker;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDriverLocationsDatabeReference;
    private ChildEventListener mChildEventListener;

    private String username;
    private String driverForUser;

    private boolean zoomed = false;
    private boolean firstPass = true;

    final private int height = 500;
    final private int width = 500;

    private TextView onlineTime;
    private Button zoom;

    LatLng latLng;

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

//        mDriverLocationsDatabeReference.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                //DriverLocation info = dataSnapshot.child(driverForUser).getValue(DriverLocation.class);
//                Toast.makeText(getActivity(), String.valueOf(dataSnapshot.child(driverForUser).child("latitude").getValue()), Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

//        Query query = mDriverLocationsDatabeReference.orderByChild("username").equalTo(driverForUser);
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    // dataSnapshot is the "issue" node with all children with id 0
//                    HashMap<String, Object> driverInfo = new HashMap<>();
////                    driverInfo.put(dataSnapshot.getKey()+"/latitude", lat);
////                    driverInfo.put(dataSnapshot.getKey()+"/longitude", lon);
////                    String id = String.valueOf(dataSnapshot);
////                    Toast.makeText(getActivity(), id, Toast.LENGTH_LONG).show();
////                    mDriverLocationsDatabeReference.child(username).updateChildren(driverInfo);
//
//                    for (final DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
//                        childSnapshot.getRef().addChildEventListener(new ChildEventListener() {
//                            @Override
//                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                            }
//
//                            @Override
//                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                                double lat = (double) childSnapshot.child("latitude").getValue();
//                                double lon = (double) childSnapshot.child("longitude").getValue();
//                                drawMarker(lat, lon);
//
//                            }
//
//                            @Override
//                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//                            }
//
//                            @Override
//                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
////                        driverInfo.put(childSnapshot.getKey()+"/latitude", lat);
////                        driverInfo.put(childSnapshot.getKey()+"/longitude", lon);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }


    public void drawMarker(double driverLat, double driverLon) {
//        if (currentLocationMarker != null) {
//            currentLocationMarker.remove();
//        }
        mMap.clear();
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.car);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        LatLng currentLocation = new LatLng(driverLat, driverLon);
        latLng = new LatLng(driverLat, driverLon);

        currentLocationMarker = mMap.addMarker(new MarkerOptions().flat(true)
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .anchor(0.5f, 0.5f).position(currentLocation).title("Driver Location"));

        //currentLocationMarker.setPosition(latLng);
        // Move the camera instantly to hamburg with a zoom of 15.

        // Zoom in, animating the camera.
        if (!zoomed) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
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
