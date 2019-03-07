package com.radarapp.mjr9r.radar.activities;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.radarapp.mjr9r.radar.Database.AppDatabase;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.fragments.BookmarkFragment;
import com.radarapp.mjr9r.radar.fragments.MainFragment;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.radarapp.mjr9r.radar.fragments.SettingsFragment;
import com.radarapp.mjr9r.radar.helpers.OnBottomNavigationItemSelectedListener;
import com.radarapp.mjr9r.radar.model.DropMessage;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements BookmarkFragment.OnListFragmentInteractionListener, MainFragment.RefreshInterface, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

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

    public ArrayList getSelectedItems() {
        return selectedItems;
    }

    public List<DropMessage> getDropMessages() {
        return dropMessages;
    }

    public boolean[] getCheckedItems() {
        return checkedItems;
    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public SharedPreferences sharedPref;
    private boolean qdEnabled;
    private int qdDistance;
    private int qdDuration;

    final ArrayList selectedItems = new ArrayList();
    List<DropMessage> dropMessages = new ArrayList<>();
    boolean[] checkedItems = new boolean[7];

    private Boolean firstTime = null;

    /**
     * Checks if the user is opening the app for the first time.
     * Note that this method should be placed inside an activity and it can be called multiple times.
     * @return boolean
     */

    private boolean isFirstTime() {
        if (firstTime == null) {
            firstTime = sharedPref.getBoolean("firstTime", true);
            if (firstTime) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("firstTime", false);
                editor.commit();
            }
        }
        return firstTime;
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

        //GET SHARED PREFERENCES
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        //CHECK IF THIS IS THE FIRST TIME RUNNING THE APP
        //IF YES, DO THE FOLLOWING
        // TODO: 02.03.2019 - SHOW TUTORIAL
        // - SET DEFAULT SHARED PREFERENCES (QUICKDROP TRUE)
        if(isFirstTime()) {
            Log.v("SHAREDPREFS", "ISFIRSTTIME");
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.shared_prefs_qd), true);
            editor.putString(getString(R.string.shared_prefs_qd_category), getString(R.string.label_cute));
            editor.putInt(getString(R.string.shared_prefs_qd_distance), 1000);
            editor.putInt(getString(R.string.shared_prefs_qd_duration), 60);
            editor.commit();
        }
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

    @Override
    public void refreshAdapterBookmarkFragment() {
        if (getSupportFragmentManager().findFragmentByTag("BOOKMARK_FRAGMENT") != null) {
            BookmarkFragment fragment = (BookmarkFragment) getSupportFragmentManager().findFragmentByTag("BOOKMARK_FRAGMENT");
            fragment.refreshAdapter();
            Log.v("DATABASE_LISTENER", "REFRESHED ADAPTER");
        }
    }

    public void toggleMarkerFilter() {
        if (getSupportFragmentManager().findFragmentByTag("MAP_FRAGMENT") != null) {
            MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag("MAP_FRAGMENT");
            fragment.filterMarkers();
        }
    }

    @Override
    public void onListFragmentInteraction(DropMessage message) {
        Log.v("LISTCLICK", "Clicked on list element");
        FragmentManager fm = this.getSupportFragmentManager();
        FragmentTransaction ft;
        ft = fm.beginTransaction();
        ft.show(mainFragment);
        ft.hide(fm.findFragmentByTag("BOOKMARK_FRAGMENT"));
        ft.commit();
        getmBottomNavigationView().setSelectedItemId(R.id.bottom_nav_map);
        mainFragment.centerCameraOnMessage(message);
        mainFragment.selectMessage(message);
        //REMOVE CURRENT FRAGMENT (WHICH SHOULD BE BOOKMARK = LISTVIEW FRAGMENT)
        //OPEN MAP FRAGMENT AT POSITION OF DROPMESSAGE
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof BookmarkFragment) {
            BookmarkFragment bookmarkFragment = (BookmarkFragment) fragment;
            bookmarkFragment.setOnListFragmentInteractionListener(this);
        }
    }

    public void openSettings(Fragment f) {
        getmBottomNavigationView().setVisibility(View.GONE);
        FragmentManager fm = this.getSupportFragmentManager();
        FragmentTransaction ft;
        ft = fm.beginTransaction();
        ft.add(R.id.container_main, SettingsFragment.newInstance(), "SETTINGS_FRAGMENT").commit();
        fm.executePendingTransactions();
        ft = fm.beginTransaction();
        SettingsFragment sf = (SettingsFragment) fm.findFragmentByTag("SETTINGS_FRAGMENT");
        sf.setCaller(f);
        ft.show(sf);
        ft.hide(f);
        ft.commit();
    }

    public void closeSettings(Fragment f) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(fm.findFragmentByTag("SETTINGS_FRAGMENT"));
        ft.commit();
        if(f.getTag().equals("MAP_FRAGMENT")) {
            ((MainFragment) f).checkSettingsChange();
        }
        ft.show(f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getmBottomNavigationView().setVisibility(View.VISIBLE);
    }

    //DEFINES WHAT HAPPENS ON HARDWARE BACK BUTTON PRESS: APP GOES BACK TO MAP FRAGMENT OR STAYS THERE
    @Override
    public void onBackPressed() {
        Fragment currentFragment = getCurrentFragment();
        FragmentManager fm = getSupportFragmentManager();
        if(currentFragment.getTag().equals("MAP_FRAGMENT")) {
            //PROBABLY THIS SHOULD CLOSE/HIDE THE APP
            return;
        }
        else if (currentFragment.getTag().equals("SETTINGS_FRAGMENT")) {
            closeSettings(fm.findFragmentByTag("MAP_FRAGMENT"));
        }
        else {
                FragmentTransaction ft = fm.beginTransaction();
                ft.show(fm.findFragmentByTag("MAP_FRAGMENT"));
                ft.hide(currentFragment);
                ft.commit();
                getmBottomNavigationView().setSelectedItemId(R.id.bottom_nav_map);
        }
    }

    public Fragment getCurrentFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for(Fragment f: fragments) {
            if(f.isVisible()) {
                return f;
            }
        }
        return null;
    }

    public Location requestLastLocation() {
        final Location lastLocation = new Location(LocationManager.GPS_PROVIDER);
        try {

            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Log.v("DISTANCECHECK ACTIVITY: ", location.getLatitude() + " " + location.getLongitude());
                    lastLocation.set(location);
                }
            });
            return lastLocation;
        } catch (SecurityException e) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
            return null;
        }
    }

}
