package com.radarapp.mjr9r.radar.activities;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.radarapp.mjr9r.radar.Database.AppDatabase;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.fragments.BookmarkFragment;
import com.radarapp.mjr9r.radar.fragments.MainFragment;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.radarapp.mjr9r.radar.helpers.OnBottomNavigationItemSelectedListener;

public class MapsActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    final int PERMISSION_LOCATION = 111;
    private GoogleApiClient mGoogleApiClient;

    final private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FirebaseFirestore getDb() {
        return db;
    }

    public AppDatabase getLocalDb() {
        return localDb;
    }

    private AppDatabase localDb;

    private FusedLocationProviderClient mFusedLocationClient;

    private MainFragment mainFragment;

    public BottomNavigationView getmBottomNavigationView() {
        return mBottomNavigationView;
    }

    private BottomNavigationView mBottomNavigationView;
    private BottomNavigationView.OnNavigationItemSelectedListener mBottomNavigationSelectedListener;

    public FusedLocationProviderClient getmFusedLocationClient() {
        return mFusedLocationClient;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Create bottom navigation
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationSelectedListener = new OnBottomNavigationItemSelectedListener(this);
        this.setupBottomNavigation(mBottomNavigationView);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.container_main);
        if(mainFragment == null) {
            mainFragment = MainFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.container_main, mainFragment, "MAP_FRAGMENT").commit();
        }

        //GET DATABASE OBJECT
        localDb = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database").build();

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if(locationResult == null) {
                return;
            }
            for(Location location: locationResult.getLocations()) {
                Log.v("SANDWICH", "Long:" + location.getLongitude() + " - Lat:" + location.getLatitude());
                mainFragment.setUserMarker(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
            Log.v("SANDWICH", "Requesting permissions");
        }
        else {
            Log.v("SANDWICH", "Starting Location Services from onConnected");
            startLocationServices();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case PERMISSION_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationServices();
                    Log.v("SANDWICH", "Permission granted - Started services");
                }
                else {
                    Log.v("SANDWICH", "Permission not granted");
                    //Show dialogue "Can't run location"
                }
            }
        }
    }

    public void startLocationServices() {
        Log.v("SANDWICH", "Starting Location Services Called");

        try {
            LocationRequest req = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
            mFusedLocationClient.requestLocationUpdates(req, mLocationCallback, null);
            Log.v("SANDWICH", "Requesting Location updates");
        } catch (SecurityException exception) {
            //Show dialogue to user saying we can't get location unless they give app permissions
            Log.v("SANDWICH", exception.toString());
        }
    }


    // BOTTOM NAVIGATION
    private void setupBottomNavigation(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setOnNavigationItemSelectedListener(mBottomNavigationSelectedListener);
    }


}
