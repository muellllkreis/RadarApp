package com.radarapp.mjr9r.radar.fragments;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.radarapp.mjr9r.radar.Database.MessageDao;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.activities.MapsActivity;
import com.radarapp.mjr9r.radar.helpers.BitmapHelper;
import com.radarapp.mjr9r.radar.helpers.CircleAnimator;
import com.radarapp.mjr9r.radar.helpers.DropAnimator;
import com.radarapp.mjr9r.radar.helpers.MarkerAnimator;
import com.radarapp.mjr9r.radar.services.DatabaseWriter;
import com.radarapp.mjr9r.radar.helpers.TimeAgo;
import com.radarapp.mjr9r.radar.model.DropMessage;
import com.radarapp.mjr9r.radar.model.Filter;
import com.radarapp.mjr9r.radar.services.DataService;
import com.radarapp.mjr9r.radar.services.MessageFetchCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.radarapp.mjr9r.radar.helpers.MarkerAnimator.pulseMarker;


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

    //HOLDS A LIST OF ALL MARKERS CURRENTLY ON MAP
    //THIS HAS TO BE EDITED WHENEVER A MARKER IS ADDED OR REMOVED
    private List<Marker> markers = new ArrayList<>();

    private Marker selectedMarker;
    private Circle selectedCircle;

    private BottomSheetBehavior mBottomSheetBehavior;
    private View bottomsheet;
    private boolean bottomSheetVisible;
    private boolean handlerIsInitialized = false;

    private final static float SELECT_ZOOM_LEVEL = 11.5f;

    FloatingActionButton fab;

    public List<Marker> getMarkers() {
        return markers;
    }

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
        startLocalRemover();
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

                    //FIND IF MARKER IS ALREADY ON MAP
                    //IF YES, PROCEED TO NEXT MARKER
                    boolean isOnMap = false;
                    for(Marker tempMarker: getMarkers()) {
                        if(dm.getDmId().toString().equals(((DropMessage) tempMarker.getTag()).getDmId().toString())) {
                            isOnMap = true;
                            break;
                        }
                    }
                    if(!isOnMap) {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(dm.getLatitude(), dm.getLongitude()))
                                .title(dm.getFilter().getName()));
                        marker.setTag(dm);
                        //                 marker.setIcon(BitmapDescriptorFactory.defaultMarker(MainFragment.getMarkerColor(dm.getFilter())));
                        Bitmap bitmap = BitmapHelper.getBitmap(getContext(), Filter.chooseMarkerIcon(dm.getFilter().getName()));
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        getMarkers().add(marker);
                    }
                }
            }
        },
        getActivity());
    }

    //THIS RUNS EVERY MINUTE AND CHECKS IF MESSAGES EXPIRED
    //THE MESSAGES ARE GOING TO BE REMOVED LOCALLY IN CASE THEY EXPIRED - A SERVERSIDE SCRIPTS REMOVES THEM GLOBALLY EVERY HOUR
    //
    //ATTENTION: THIS KEEPS RUNNING IN THE BACKGROUND EVEN WHEN THE APP IS CLOSED - PROBLEM??
    private void startLocalRemover() {
        if(!handlerIsInitialized) {
            final Handler handler = new Handler();
            handlerIsInitialized = true;
            handler.postDelayed(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(getActivity(), "Running handler!", Toast.LENGTH_SHORT);
                    toast.show();
                    for (Iterator<Marker> iterator = getMarkers().iterator(); iterator.hasNext(); ) {
                        Marker m = iterator.next();
                        if (((DropMessage) m.getTag()).getUnixTime() + ((DropMessage) m.getTag()).getDuration() * 60 <= Calendar.getInstance().getTimeInMillis() / 1000) {
                            //REMOVE MARKER FROM MAP
                            m.remove();
                            iterator.remove();
                        }
                    }
                    handler.postDelayed(this, 60000); //every 1 minute
                }
            }, 60000); //Every 60000 ms (1 minute)
        }
        else {
            return;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        // HANDLE PULSE ANIMATION FOR SELECTED/DESELECTED MARKER
        if(selectedMarker != null) {
            MarkerAnimator.stopAnimation();
            Bitmap bitmap = BitmapHelper.getBitmap(getContext(), Filter.chooseMarkerIcon(((DropMessage) selectedMarker.getTag()).getFilter().getName()));
            selectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
        selectedMarker = marker;
        if(selectedCircle != null) {
            selectedCircle.remove();
        }

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

        //ADD CIRCLE AROUND MARKER
        selectedCircle = getmMap().addCircle(new CircleOptions()
                .center(marker.getPosition())
                .radius(0)
                .strokeColor(getResources().getColor(Filter.chooseCircleStrokeColor((dm.getFilter().getName()))))
                .fillColor(getResources().getColor(Filter.chooseCircleFillColor(dm.getFilter().getName()))));

        CircleAnimator.animateCircle(selectedCircle, ((DropMessage) marker.getTag()).getDistance());

        Bitmap markerIcon = BitmapHelper.getBitmap(getContext(), Filter.chooseMarkerIcon(dm.getFilter().getName()));
        pulseMarker(markerIcon, marker, 1000);


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

        // set buttons in bottomsheet
        final Button bookmarkBtn = bottomsheet.findViewById(R.id.bookmark_button);

        // center map view on marker
        CameraUpdate cu;
        if(getmMap().getCameraPosition().zoom > SELECT_ZOOM_LEVEL) {
            cu = CameraUpdateFactory.newLatLng(marker.getPosition());
        }
        else {
            cu = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15);
        }
        mMap.animateCamera(cu);
        Log.v("ZOOMLEVEL", mMap.getCameraPosition().toString());


        // check if message has already been bookmarked
        // if not, set clicklistener
        if(hasBeenBookmarked(dm)) {
            bookmarkBtn.setText("BOOKMARKED");
            bookmarkBtn.setActivated(false);
            Drawable img = getContext().getResources().getDrawable( R.drawable.ic_bookmark_white_24dp );
            bookmarkBtn.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null );
        }
        else {
            bookmarkBtn.setText("BOOKMARK THIS");
            Drawable img = getContext().getResources().getDrawable( R.drawable.ic_bookmark_border_white_24dp);
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
                        //MAYBE ADD OPTION TO DELETE/UNBOOKMARK MESSAGES
                        //THE WAY BELOW DOES NOT WORK THOUGH
//                        if(hasBeenBookmarked(dm)) {
////                            Log.v("DATABASE_LISTENER", "PERFORMING DELETE");
////                                messageDao.delete(dm);
////                        }
                        if(!hasBeenBookmarked(dm)) {
                            Log.v("DATABASE_LISTENER", "PERFORMING INSERT");
                                messageDao.insertAll(dm);
                                ((MapsActivity) getActivity()).refreshAdapterBookmarkFragment();
                        }
                    }
                });
                if(!hasBeenBookmarked(dm)) {
                    bookmarkBtn.setText("BOOKMARKED");
                    Drawable img = getContext().getResources().getDrawable( R.drawable.ic_bookmark_white_24dp );
                    bookmarkBtn.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null );
                    setBookmarkStatus(dm, true);
                }
            }
        });
        // return true hides info window from appearing, maybe later we need to change this to
        // false
        Log.v("MARKERLIST", "LIST LENGTH: " + getMarkers().size());
        for(Marker testmarker : getMarkers()) {
            Log.v("MARKERLIST", ((DropMessage) testmarker.getTag()).getDmId().toString());
        }
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(mBottomSheetBehavior.getPeekHeight() != 0) {
            mBottomSheetBehavior.setPeekHeight(0);
            bottomSheetVisible = false;
        }
        mBottomSheetBehavior.setHideable(true);//Important to add
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // HANDLE PULSE ANIMATION FOR SELECTED/DESELECTED MARKER
        MarkerAnimator.stopAnimation();
        Bitmap bitmap = BitmapHelper.getBitmap(getContext(), Filter.chooseMarkerIcon(((DropMessage) selectedMarker.getTag()).getFilter().getName()));
        selectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        selectedCircle.remove();

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
                                DropMessage dm = new DropMessage(UUID.randomUUID(),
                                        (float) location.getLatitude(),
                                        (float) location.getLongitude(),
                                        new Date(),
                                        content,
                                        Filter.CUTE,
                                        0,
                                        0);

                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(dm.getLatitude(), dm.getLongitude()))
                                        .title(dm.getFilter().getName()));
                                Log.v("QUICKDROP", marker.getTitle());
                                marker.setTag(dm);
                                DropAnimator.dropPinEffect(marker);
                                //marker.setIcon(BitmapDescriptorFactory.defaultMarker(MainFragment.getMarkerColor(dm.getFilter())));
                                Bitmap bitmap = BitmapHelper.getBitmap(getContext(), Filter.chooseMarkerIcon(dm.getFilter().getName()));
                                marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                                getMarkers().add(marker);
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
        boolean result = sharedPref.getBoolean(dm.getDmId().toString(), false);
        Log.v("DATABASE_LISTENER", "SHAREDPREFS RESULT FOR DM_ID " + dm.getDmId() + ": " + result);
        return result;
    }

    private void setBookmarkStatus(DropMessage dm, boolean status) {
        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.shared_prefs_bookmarks), Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(dm.getDmId().toString(), status).apply();
    }

    public interface RefreshInterface{
        public void refreshAdapterBookmarkFragment();
    }
}
