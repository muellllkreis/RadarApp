package com.radarapp.mjr9r.radar.fragments;


import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.radarapp.mjr9r.radar.Database.MessageDao;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.activities.MapsActivity;
import com.radarapp.mjr9r.radar.services.DatabaseWriter;
import com.radarapp.mjr9r.radar.helpers.TimeAgo;
import com.radarapp.mjr9r.radar.model.DropMessage;
import com.radarapp.mjr9r.radar.model.Filter;
import com.radarapp.mjr9r.radar.services.DataService;
import com.radarapp.mjr9r.radar.services.MessageFetchCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    public GoogleMap getmMap() {
        return mMap;
    }

    private GoogleMap mMap;
    private MarkerOptions userMarker;


    private BottomSheetBehavior mBottomSheetBehavior;
    private View bottomsheet;
    private boolean bottomSheetVisible;

    FloatingActionButton fab;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actiobar_main, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Floating Action Button
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft ;
                if (fm.findFragmentByTag("COMPOSE_FRAGMENT") == null) {
                    ft = fm.beginTransaction();
                    ft.add(R.id.container_main, ComposeFragment.newInstance(), "COMPOSE_FRAGMENT").commit();
                    fm.executePendingTransactions();
                    ft = fm.beginTransaction();
                    ft.show(fm.findFragmentByTag("COMPOSE_FRAGMENT"));
                    ft.hide(fm.findFragmentByTag("MAP_FRAGMENT"));
                    ft.commit();
                    ((MapsActivity) getActivity()).getmBottomNavigationView().setSelectedItemId(R.id.bottom_nav_compose);
                }
                else {
                    ft = fm.beginTransaction();
                    ft.show(fm.findFragmentByTag("COMPOSE_FRAGMENT"));
                    ft.hide(fm.findFragmentByTag("MAP_FRAGMENT"));
                    ft.commit();
                    ((MapsActivity) getActivity()).getmBottomNavigationView().setSelectedItemId(R.id.bottom_nav_compose);
                }
            }
        });


        //THE FOLLOWING CODE HANDLES QUICKDROP
        final EditText quickdrop = (EditText) view.findViewById(R.id.quickdrop_edit);
        final ImageButton quickDropBtn = view.findViewById(R.id.quickdrop_send);

        quickDropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Log.v("QUICKDROP", "Registered Return Key");
                    String content = quickdrop.getText().toString();

                    //Hide Keyboard
                    InputMethodManager mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mInputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    quickdrop.setText("");
                    quickdrop.clearFocus();
                    getActivity().findViewById(R.id.container_main).requestFocus();

                    //CREATE MARKER METHOD
                    quickDropMessage(content);
                    //DROP IT ON MAP
                }
        });

        quickdrop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()>0 && s.subSequence(s.length()-1, s.length()).toString().equalsIgnoreCase("\n")) {
                    quickDropBtn.performClick();
                    //HIDE KEYBOARD
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(quickdrop.getWindowToken(), 0);
                    quickdrop.setText("");
                    quickdrop.clearFocus();
                    getActivity().findViewById(R.id.container_main).requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //LISTEN FOR CHANGES IN DATABASE
        DataService.getInstance().listenForMessageUpdates(getActivity(), getContext());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mBottomSheetBehavior == null) {
            bottomsheet = getView().findViewById(R.id.bottom_sheet);
            Log.v("BOTTOMSHEET", bottomsheet.toString());
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomsheet);
            mBottomSheetBehavior.setPeekHeight(0);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetVisible = false;
        }
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
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException exception) {
            Log.v("SANDWICH", "Something went terribly wrong");
        }

    }

    public void setUserMarker(LatLng latLng) {
//        if(userMarker == null) {
//            userMarker = new MarkerOptions().position(latLng).title("Current Location");
//            mMap.addMarker(userMarker);
//            Log.v("SANDWICH", "Current location: " + latLng.latitude + " - " + latLng.longitude);
//        }
        updateMap();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void updateMap() {
        DataService.getInstance()
                .getInitialMessages(new MessageFetchCallback() {
            @Override
            public void onCallback(List<DropMessage> messageList) {
                for(int x = 0; x < messageList.size(); x++) {
                    Log.v("UPDATEMAP", messageList.get(x).getContent());
                    DropMessage dm = messageList.get(x);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(dm.getLatitude(), dm.getLongitude()))
                            .title(dm.getFilter().getName()));
                    marker.setTag(dm);
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(MainFragment.getMarkerColor(dm.getFilter())));
                }
            }
        },
        getActivity());
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //test
        //cardView = LayoutInflater.from(this.getContext()).inflate(R.layout.content_location_cardview,  null);
        Log.v("OHOH", "KLICK AUF MARKER");
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setPeekHeight(500);
        bottomSheetVisible = true;

        //get message associated with marker
        final DropMessage dm = (DropMessage) marker.getTag();
        Filter dmFilter = dm.getFilter();
        String dmContent = dm.getContent();
        Date dmDate = dm.getDate();

        //get "xyz seconds/minutes/hours... ago" string
        String timeFromNow = TimeAgo.toDuration(new Date().getTime() - dmDate.getTime());

        //set bottomview according to marker content
        TextView filterText = bottomsheet.findViewById(R.id.message_filter);
        TextView contentText = bottomsheet.findViewById(R.id.message_content);
        TextView dateText = bottomsheet.findViewById(R.id.message_date);
        ImageView messageIcon = bottomsheet.findViewById(R.id.message_icon);
        filterText.setText(dmFilter.getName());
        contentText.setText(dmContent);
        dateText.setText(timeFromNow);
        messageIcon.setImageResource(dmFilter.getIconID());
        //messageIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), dmFilter.getIconID()));

        //bottomsheet.setBackgroundColor(Color.parseColor(dmFilter.getColor()));

        // return true hides info window from appearing, maybe later we need to change this to
        // false

        // set buttons in bottomsheet
        final Button bookmarkBtn = bottomsheet.findViewById(R.id.bookmark_button);

        // check if message has already been bookmarked
        // if not, set clicklistener
        if(hasBeenBookmarked(dm)) {
            bookmarkBtn.setText("BOOKMARKED");
            bookmarkBtn.setActivated(false);
            Drawable img = getContext().getResources().getDrawable( R.drawable.ic_bookmark_white_24dp );
            bookmarkBtn.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null );
        }
        bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MessageDao messageDao = ((MapsActivity) getActivity()).getLocalDb().messageDao();
                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("DATABASE_LISTENER", "IN RUN: " + Boolean.toString(hasBeenBookmarked(dm)));
                        if(hasBeenBookmarked(dm)) {
//                            Log.v("DATABASE_LISTENER", "PERFORMING DELETE");
//                            synchronized (messageDao) {
//                                messageDao.delete(dm);
//                            }
                        }
                        else {
                            Log.v("DATABASE_LISTENER", "PERFORMING INSERT");
                            synchronized (messageDao) {
                                messageDao.insertAll(dm);
                            }
                        }
                    }
                });
                Log.v("DATABASE_LISTENER", "OUTSIDE RUN: " + Boolean.toString(hasBeenBookmarked(dm)));
                if(hasBeenBookmarked(dm)) {
//                    bookmarkBtn.setText(R.string.bookmark_button);
//                    Drawable changedIcon = getContext().getResources().getDrawable(R.drawable.ic_bookmark_border_white_24dp);
//                    changedIcon.setBounds(24,24,24,24);
//                    bookmarkBtn.setCompoundDrawables(changedIcon, null, null, null);
//                    setBookmarkStatus(dm, false);
                }
                else {
                    bookmarkBtn.setText("BOOKMARKED");
                    Drawable img = getContext().getResources().getDrawable(R.drawable.ic_bookmark_white_24dp);
                    img.setBounds(24,24,24,24);
                    bookmarkBtn.setCompoundDrawables(img, null, null, null);
                    setBookmarkStatus(dm, true);
                }
            }
        });
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(mBottomSheetBehavior.getPeekHeight() != 0) {
            mBottomSheetBehavior.setPeekHeight(0);
            bottomSheetVisible = false;
        }
        //else new quickdrop?
    }

    public static float getMarkerColor(Filter filter) {
        return Filter.chooseMarkerColor(filter.getName());
    }

    private void quickDropMessage(final String content) {
        FusedLocationProviderClient locationClient = ((MapsActivity) getActivity()).getmFusedLocationClient();
        try {
            Log.v("QUICKDROP", "IN TRY BLOCK");
            locationClient.getLastLocation().addOnSuccessListener(this.getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location != null) {
                                DropMessage dm = new DropMessage((float) location.getLatitude(),
                                        (float) location.getLongitude(),
                                        new Date(),
                                        content,
                                        Filter.CUTE);

                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(dm.getLatitude(), dm.getLongitude()))
                                        .title(dm.getFilter().getName()));
                                Log.v("QUICKDROP", marker.getTitle());
                                marker.setTag(dm);
                                marker.setIcon(BitmapDescriptorFactory.defaultMarker(MainFragment.getMarkerColor(dm.getFilter())));

                                //CALL HELPER CLASS TO WRITE TO DB
                                DatabaseWriter.storeMessageInDatabase(dm, getActivity(), getContext());
                            }
                            else {
                                //SOMETHING BAD HAPPENED HERE
                            }
                        }
                    });
        } catch(SecurityException e) {
            //Message error that location is not known
            return;
        }
    }

    private boolean hasBeenBookmarked(DropMessage dm) {
        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.shared_prefs_bookmarks), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(dm.getDmId().toString(), false);
    }

    private void setBookmarkStatus(DropMessage dm, boolean status) {
        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.shared_prefs_bookmarks), Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(dm.getDmId().toString(), status).apply();
    }
}
