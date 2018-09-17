package com.radarapp.mjr9r.radar.fragments;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.model.DropMessage;
import com.radarapp.mjr9r.radar.model.Filter;
import com.radarapp.mjr9r.radar.services.DataService;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private MarkerOptions userMarker;


    private BottomSheetBehavior mBottomSheetBehavior;
    private View bottomsheet;
    private boolean bottomSheetVisible;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //HIER will ich Quickdrop einbauen
        final EditText quickdrop = (EditText) view.findViewById(R.id.quickdrop_edit);
        quickdrop.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String content = quickdrop.getText().toString();

                    //Hide Keyboard
                    InputMethodManager mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mInputMethodManager.hideSoftInputFromInputMethod(quickdrop.getWindowToken(), 0);

                    //CREATE MARKER METHOD
                    //DROP IT ON MAP
                }
                return false;
            }
        });

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
        ArrayList<DropMessage> locations = DataService.getInstance().getSampleMessages();

        for(int x = 0; x < locations.size(); x++) {
            DropMessage dm = locations.get(x);
            Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(dm.getLatitude(), dm.getLongitude()))
                                .title(dm.getFilter().getName()));
            marker.setTag(dm);
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(this.getMarkerColor(dm.getFilter())));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //test
        //cardView = LayoutInflater.from(this.getContext()).inflate(R.layout.content_location_cardview,  null);
        Log.v("OHOH", "KLICK AUF MARKER");
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setPeekHeight(300);
        bottomSheetVisible = true;

        //get message associated with marker
        DropMessage dm = (DropMessage) marker.getTag();
        Filter dmFilter = dm.getFilter();
        String dmContent = dm.getContent();
        Date dmDate = dm.getDate();

        //set bottomview according to marker content
        TextView filterText = bottomsheet.findViewById(R.id.message_filter);
        TextView contentText = bottomsheet.findViewById(R.id.message_content);
        TextView dateText = bottomsheet.findViewById(R.id.message_date);
        ImageView messageIcon = bottomsheet.findViewById(R.id.message_icon);
        filterText.setText(dmFilter.getName());
        contentText.setText(dmContent);
        dateText.setText(dmDate.toString());
        messageIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), dmFilter.getIconID()));
        bottomsheet.setBackgroundColor(Color.parseColor(dmFilter.getColor()));

        // return true hides info window from appearing, maybe later we need to change this to
        // false
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

    private float getMarkerColor(Filter filter) {
        return Filter.chooseMarkerColor(filter.getName());
    }

}
