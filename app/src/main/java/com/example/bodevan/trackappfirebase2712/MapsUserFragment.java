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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MapsUserFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFrag;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDriverLocationsDatabeReference;

    private String username;
    private String driverForUser;

    private boolean zoomed = false;
    private boolean firstPass = true;

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
        username = bundle.getString("username");
        driverForUser = bundle.getString("driver");

        updateDatabase();

        return v;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void updateDatabase(){
        Query query = mDriverLocationsDatabeReference.orderByChild("username").equalTo(driverForUser);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    HashMap<String, Object> driverInfo = new HashMap<>();
//                    driverInfo.put(dataSnapshot.getKey()+"/latitude", lat);
//                    driverInfo.put(dataSnapshot.getKey()+"/longitude", lon);
//                    String id = String.valueOf(dataSnapshot);
//                    Toast.makeText(getActivity(), id, Toast.LENGTH_LONG).show();
//                    mDriverLocationsDatabeReference.child(username).updateChildren(driverInfo);

                    for (final DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                        childSnapshot.getRef().addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                double lat = (double) childSnapshot.child("latitude").getValue();
                                double lon = (double) childSnapshot.child("longitude").getValue();
                                drawMarker(lat, lon);

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
//                        driverInfo.put(childSnapshot.getKey()+"/latitude", lat);
//                        driverInfo.put(childSnapshot.getKey()+"/longitude", lon);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void drawMarker(double driverLat, double driverLon){
        int height = 500;
        int width = 500;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.car);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        LatLng currentLocation = new LatLng(driverLat, driverLon);
        LatLng latLng = new LatLng(driverLat, driverLon);
        //TODO: Location is set only upon launch of map without constant update;
        //TODO: updates for dirvers are observed only using setMyLocationEnabled
        Marker currentLocationMarker = mMap.addMarker(new MarkerOptions().flat(true)
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .anchor(0.5f, 0.5f).position(currentLocation).title("Current Location"));

        //currentLocationMarker.setPosition(latLng);
        // Move the camera instantly to hamburg with a zoom of 15.

        // Zoom in, animating the camera.
        if (!zoomed) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
            zoomed = true;
        }
        if (!firstPass) {
            currentLocationMarker.remove();
        }
        firstPass = false;
        Toast.makeText(getActivity(), "Latitude = " +
                        driverLat + "" + "Longitude = " + driverLon,
                Toast.LENGTH_LONG).show();
    }

}
