package com.radarapp.mjr9r.radar.fragments;


import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.activities.MapsActivity;
import com.radarapp.mjr9r.radar.helpers.BitmapHelper;
import com.radarapp.mjr9r.radar.helpers.DropAnimator;
import com.radarapp.mjr9r.radar.services.DatabaseWriter;
import com.radarapp.mjr9r.radar.helpers.ViewTagHelper;
import com.radarapp.mjr9r.radar.model.DropMessage;
import com.radarapp.mjr9r.radar.model.Filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ComposeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ComposeFragment extends Fragment {

    private View.OnClickListener mHorizontalClickListener;
    private HashMap<String, Boolean> mFilterSelected = new HashMap<>(7);
    private ArrayList<String> mFilterLabels = new ArrayList<>();
    private ObjectAnimator scaleDown;
    private HorizontalScrollView scrollView;

    private String currentlySelectedFilter;
    private TextView distanceValue;
    private TextView durationValue;
    private EditText contentText;
    private SeekBar distanceBar;
    private SeekBar durationBar;
    private ImageView attachedIcon;
    private ImageView deleteIcon;

    private MapsActivity mainActivity;

    private byte[] attachedPhoto;
    private File attachedPhotoLocation;
    private StorageReference storageReference;
    public byte[] getAttachedPhoto() {
        return attachedPhoto;
    }
    public void setAttachedPhoto(byte[] attachedPhoto) {
        this.attachedPhoto = attachedPhoto;
    }
    public File getAttachedPhotoLocation() {
        return attachedPhotoLocation;
    }
    public void setAttachedPhotoLocation(File attachedPhotoLocation) {
        this.attachedPhotoLocation = attachedPhotoLocation;
    }
    public StorageReference getStorageReference() {
        return storageReference;
    }
    public void setStorageReference(StorageReference storageReference) {
        this.storageReference = storageReference;
    }


    public ComposeFragment() {
        // Required empty public constructor
    }

    public static ComposeFragment newInstance() {
        ComposeFragment fragment = new ComposeFragment();
        fragment.mFilterSelected.put("CUTE", false);
        fragment.mFilterSelected.put("DEAL", false);
        fragment.mFilterSelected.put("LOL", false);
        fragment.mFilterSelected.put("EVENT", false);
        fragment.mFilterSelected.put("EDU", false);
        fragment.mFilterSelected.put("SECRET", false);
        fragment.mFilterSelected.put("FOOD", false);

        fragment.mFilterLabels.add("CUTE");
        fragment.mFilterLabels.add("DEAL");
        fragment.mFilterLabels.add("LOL");
        fragment.mFilterLabels.add("EVENT");
        fragment.mFilterLabels.add("EDU");
        fragment.mFilterLabels.add("SECRET");
        fragment.mFilterLabels.add("FOOD");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mainActivity = (MapsActivity) this.getActivity();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actiobar_compose, menu);
        mainActivity.getSupportActionBar().setTitle(R.string.actionbar_title_compose);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send : {
                Log.i("SANDWICH", "Pressed action send");

                //SOMETHING IS STILL WRONG HERE
                //1. IF NO FILTER IS SELECTED = CRASH
                //2. IF FILTERS WHERE THE NAMES DON'T MATCH ARE SELECTED = CRASH (FOOD, SECRET, EDUCATION)
                if(currentlySelectedFilter == null || currentlySelectedFilter.equals("") || contentText.getText().toString().equals("")) {
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(getContext(), R.string.toast_incomplete, duration);
                    toast.show();
                    return false;
                }

                FusedLocationProviderClient locationClient = ((MapsActivity) getActivity()).getmFusedLocationClient();
                try {
                    //CHECK IF THERE IS A LAST LOCATION
                    locationClient.getLastLocation().addOnSuccessListener(this.getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location != null) {
                                DropMessage dm;
                                if(attachedPhoto == null) {
                                    //CREATE DROPMESSAGE FROM VIEWS IN FRAGMENT
                                    dm = new DropMessage(UUID.randomUUID(),
                                            (float) location.getLatitude(),
                                            (float) location.getLongitude(),
                                            new Date(),
                                            contentText.getText().toString(),
                                            Filter.valueOf(currentlySelectedFilter),
                                            (double) distanceBar.getProgress(),
                                            (double) durationBar.getProgress());
                                    addToMapAndWriteToDb(dm, location);
                                }
                                else {
                                    composeMessageWithPhoto(location);
                                }
                            }
                            else {
                                //SOMETHING BAD HAPPENED HERE
                            }
                        }
                    });
                } catch(SecurityException e) {
                    //Message error that location is not known
                    return false;
                }
                return true;
            }
            case R.id.action_photo: {
                Log.v("MENUCLICK", "CAMERA BUTTON");
                mainActivity.callCameraFragment(mainActivity.getSupportFragmentManager().findFragmentByTag("COMPOSE_FRAGMENT"));
                return true;
            }
            case R.id.action_settings: {
                mainActivity.openSettings(this);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compose, container, false);

        contentText = view.findViewById(R.id.content_text);
        attachedIcon = view.findViewById(R.id.compose_img_icon);
        deleteIcon = view.findViewById(R.id.compose_delete_img_icon);

        View sliderCardView = view.findViewById(R.id.sliders_cardview);
        distanceBar = sliderCardView.findViewById(R.id.distance_slider);
        durationBar = sliderCardView.findViewById(R.id.duration_slider);
        distanceValue = sliderCardView.findViewById(R.id.distance_slider_value);
        durationValue = sliderCardView.findViewById(R.id.duration_slider_value);

        distanceValue.setText(Integer.toString(distanceBar.getProgress()) + "m");
        durationValue.setText(Integer.toString(durationBar.getProgress()) + "min");

        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceValue.setText(Integer.toString(progress) + "m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        durationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                durationValue.setText(Integer.toString(progress) + "min");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        scrollView = view.findViewById(R.id.compose_scrollView);
        mHorizontalClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView scrollIcon = v.findViewWithTag("scroll_icon");
                TextView scrollLabel = v.findViewWithTag("scroll_label");

                String labelText = scrollLabel.getText().toString();
                //FIND IF FILTER HAS ALREADY BEEN SELECTED
                String selectedFilter = checkFilterSelected();
                if(selectedFilter.length() > 0) {
                    List<View> labelList = ViewTagHelper.findViewWithTagRecursively(scrollView, "scroll_label");
                    for(View label: labelList) {
                        TextView temp = (TextView) label;
                        temp.setTypeface(null, Typeface.NORMAL);
                    }
                }
                //THIS GETS CALLED IF FILTER GETS CLICKED TWICE (=UNSELECTED)
                if(selectedFilter.equals(scrollLabel.getText().toString())) {
                    currentlySelectedFilter = "";
                    scrollLabel.setTypeface(null, Typeface.NORMAL);
                }
                //THIS SPECIFIES THE FILTER THAT HAS EVENTUALLY BEEN CHOSEN
                //currentlySelectedFilter HAS TO BE USED FOR DROPMESSAGE CREATION
                else {
                    currentlySelectedFilter = labelText;
                    Log.v("ONCLICKLISTENER", currentlySelectedFilter);
                    scrollLabel.setTypeface(null, Typeface.BOLD);
                    mFilterSelected.put(labelText, true);
                }
            }
        };
        setListenerForFilterViews(view);

        return view;
    }

    public void showAttachedIcon() {
        attachedIcon.setVisibility(View.VISIBLE);
        deleteIcon.setVisibility(View.VISIBLE);

        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.photo_dialog_title)
                        .setMessage(R.string.photo_dialog_message)

                        .setPositiveButton(R.string.photo_dialog_positive, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                setAttachedPhoto(null);
                                setAttachedPhotoLocation(null);
                                setStorageReference(null);
                                attachedIcon.setVisibility(View.GONE);
                                deleteIcon.setVisibility(View.GONE);
                            }
                        })

                        .setNegativeButton(R.string.photo_dialog_negative, null)
                        .show();
            }
        });
    }

    //GETS ALL ITEMS IN HORIZONTAL SCROLLVIEW AND SETS THE ONCLICKLISTENER FOR THEM
    private void setListenerForFilterViews(View view) {
        HorizontalScrollView scrollView = view.findViewById(R.id.compose_scrollView);
        LinearLayout scrollChild = (LinearLayout) scrollView.getChildAt(0);

        for(int i = 0; i < scrollChild.getChildCount(); i++) {
            View firstView = scrollChild.getChildAt(i);
            firstView.setOnClickListener(mHorizontalClickListener);
        }
    }

    private boolean checkFilterReSelected(String label) {
        Log.v("ONCLICKLISTENER", mFilterSelected.get(label).toString());
        return mFilterSelected.get(label);
    }

    //CHECKS A HASHMAP THAT INCLUDES ALL FILTERS AND ASSIGNS THEM A BOOLEAN, IF ANY OF THEM HAD
    //BEEN SELECTED, RETURNS THE FILTERNAME OF THE SELECTED FILTER IF FOUND
    private String checkFilterSelected() {
        for(String s: mFilterLabels) {
            if(mFilterSelected.get(s)) {
                mFilterSelected.put(s, false);
                return s;
            }
        }
        return "";
    }

    //DESELECTS ALL FILTERS, USED FOR RESETTING AFTER SENDING
    private void deselectAllFilters() {
        List<View> labelList = ViewTagHelper.findViewWithTagRecursively(scrollView, "scroll_label");
        for(View label: labelList) {
            TextView temp = (TextView) label;
            temp.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void addToMapAndWriteToDb(DropMessage dm, Location location) {
        //ADD MARKER TO MAPFRAGMENT
        FragmentManager fm = getActivity().getSupportFragmentManager();
        MainFragment mainFragment = (MainFragment) fm.findFragmentByTag("MAP_FRAGMENT");
        GoogleMap mMap = mainFragment.getmMap();

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(dm.getLatitude(), dm.getLongitude()))
                .title(dm.getFilter().getName()));

        marker.setTag(dm);
        DropAnimator.dropPinEffect(marker);
//                                marker.setIcon(BitmapDescriptorFactory.defaultMarker(MainFragment.getMarkerColor(dm.getFilter())));
        Bitmap bitmap = BitmapHelper.getBitmap(getContext(), Filter.chooseMarkerIcon(dm.getFilter().getName()));
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        mainFragment.getMarkers().add(marker);
        //GO BACK TO MAP FRAGMENT
        FragmentTransaction ft = fm.beginTransaction();
        ft.show(mainFragment);
        ft.hide(fm.findFragmentByTag("MAP_FRAGMENT"));
        ft.commit();
        fm.executePendingTransactions();
        ((MapsActivity) getActivity()).getmBottomNavigationView().setSelectedItemId(R.id.bottom_nav_map);

        //RESET FILTER AND TEXT VIEWS
        contentText.setText("");
        deselectAllFilters();
        currentlySelectedFilter = "";

        //WRITE TO DATABASE
        DatabaseWriter.storeMessageInDatabase(dm, getActivity(), getContext());
    }

    private void composeMessageWithPhoto(final Location location) {
        //CREATE DROPMESSAGE FROM VIEWS IN FRAGMENT AND WITH THE PHOTO FILE
        final DropMessage dm = new DropMessage(UUID.randomUUID(),
                    (float) location.getLatitude(),
                    (float) location.getLongitude(),
                    new Date(),
                    contentText.getText().toString(),
                    Filter.valueOf(currentlySelectedFilter),
                    (double) distanceBar.getProgress(),
                    (double) durationBar.getProgress());

        final ProgressDialog progressDialog = new ProgressDialog(mainActivity);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        final UploadTask uploadTask = getStorageReference().putBytes(getAttachedPhoto());
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast toast = Toast.makeText(getActivity(), "Unable to post, check connection.", Toast.LENGTH_SHORT);
                toast.show();
                progressDialog.dismiss();
            }
        });

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.v("UPLOADLOG", "Upload has succeded");
                try {
                    getStorageReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            dm.setImageRef(uri.toString());
                            addToMapAndWriteToDb(dm, location);

                            //RESET BUFFERED IMG
                            setAttachedPhoto(null);
                            setAttachedPhotoLocation(null);
                            setStorageReference(null);
                            attachedIcon.setVisibility(View.GONE);
                            deleteIcon.setVisibility(View.GONE);

                            Log.v("UPLOADLOG", "DOWNLOADLINK: " + uri.toString());
                        }
                    });
                    FileOutputStream fos = new FileOutputStream(getAttachedPhotoLocation());
                    fos.write(getAttachedPhoto());
                    fos.close();
                    Log.v("UPLOADLOG", "Written to Storage");
                    progressDialog.dismiss();
                    Log.v("CAMERALOG", "File created under " + getAttachedPhotoLocation().getAbsolutePath());
                    MediaScannerConnection.scanFile(getContext(), new String[]{getAttachedPhotoLocation().getPath()}, null, null);
                } catch (FileNotFoundException e) {
                    Log.v(TAG, e.getMessage());
                } catch (IOException e) {
                    Log.v(TAG, e.getMessage());
                }
            }
        });

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                        .getTotalByteCount());
                progressDialog.setMessage("Uploaded "+(int)progress+"%");
            }
        });
        return;
    }
}

//    String labelText = scrollLabel.getText().toString();
//                if(checkFilterSelected(labelText)){
//                        scaleDown.end();
//                        scrollLabel.setTypeface(null, Typeface.NORMAL);
//                        mFilterSelected.put(labelText, false);
//                        }
//                        else {
//                        scrollLabel.setTypeface(null, Typeface.BOLD);
//
//                        scaleDown = ObjectAnimator.ofPropertyValuesHolder(
//                        scrollIcon,
//                        PropertyValuesHolder.ofFloat("scaleX", 0.8f),
//                        PropertyValuesHolder.ofFloat("scaleY", 0.8f));
//                        scaleDown.setDuration(1000);
//
//                        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
//                        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
//
//                        scaleDown.start();
//                        mFilterSelected.put(labelText, true);
//                        }
