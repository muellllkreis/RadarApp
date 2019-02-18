package com.radarapp.mjr9r.radar.services;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.activities.MapsActivity;
import com.radarapp.mjr9r.radar.fragments.MainFragment;
import com.radarapp.mjr9r.radar.helpers.BitmapHelper;
import com.radarapp.mjr9r.radar.model.DropMessage;
import com.radarapp.mjr9r.radar.model.Filter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Matias on 12.09.2018.
 */

public class DataService {
    private static final DataService instance = new DataService();
    private FirebaseFirestore db;

    public static DataService getInstance() {
        return instance;
    }
    private ArrayList<DocumentChange> tmpChanges = new ArrayList<>();
    private boolean handlerStarted = false;

    private DataService() {
    }

    //THIS METHOD FETCHES ALL MESSAGES FROM THE DATABASE
    public void getInitialMessages(final MessageFetchCallback callback, Activity activity) {
        final ArrayList<DropMessage> list = new ArrayList<>();

        //GET DATABASE REFERENCE
        db = ((MapsActivity) activity).getDb();

        //COLLECTION IN FIREBASE TO BE CHECKED IS "messages"
        db.collection("messages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("FIREBASE_FETCH", document.getId() + " => " + document.getData());

                                //PARSE DATA FROM DATABASE
                                UUID uuid = UUID.fromString(document.get("uuid").toString());
                                Date dmDate = parseDate(document.get("date").toString());
                                double dmDuration = Double.valueOf(document.get("duration").toString());
                                double dmDistance = Double.valueOf(document.get("distance").toString());
                                Filter dmFilter = Filter.valueOf(document.get("filter").toString());
                                double dmLatitude = (double) document.get("latitude");
                                double dmLongitude = (double) document.get("longitude");
                                String dmContent = document.get("content").toString();

                                //CREATE NEW DROPMESSAGE FROM PARSED DATA
                                DropMessage dm = new DropMessage(uuid, (float) dmLatitude, (float) dmLongitude, dmDate, dmContent, dmFilter, dmDistance, dmDuration);

                                //LOCAL CHECK FOR DURATION
                                //SERVER SIDE CHECK IS PERFORMED EVERY HOUR SO THIS CATCHES MESSAGES EXPIRED IN BETWEEN
                                if(dm.getUnixTime() + dm.getDuration() * 60 >= Calendar.getInstance().getTimeInMillis()/1000) {
                                    list.add(dm);
                                    Log.d("FIREBASE_FETCH", dm.getContent());
                                    Log.d("FIREBASE_FETCH", list.get(0).toString());
                                }
                                else {
                                    continue;
                                }

                                //LOCAL CHECK FOR DISTANCE
                                // TODO: 18.02.2019: SMART WAY OF LIMITING LOCATION QUERY SERVER-SIDE

                                //SAMPLE MESSAGES THAT SHOULD BE DELETED AT SOME POINT
                                //list.add(new DropMessage(UUID.fromString("a71493c9-3a63-4d66-ab18-974283ac375a"), 52.530673f, 13.328216f, new Date((long) 1537388610962L), "Das hier ist eine coole Tankstelle!", Filter.CUTE, 100, 60));
                                //list.add(new DropMessage(UUID.fromString("3c5c3f3e-1538-412b-9080-bd43b5b887fa"), 52.528086f, 13.324048f, new Date((long) 1537280742 * 1000), "Ei wie toll ein Gasturbinenwerk!", Filter.EVENT, 250, 60));
                                //list.add(new DropMessage(UUID.fromString("89d34b21-837e-4e38-a04d-d66d35cfe45c"), 52.527386f, 13.326866f, new Date((long) 1537284942 * 1000), "Vietnamesisch essen nur hier!", Filter.FOOD, 500, 120));
                                Log.v("FIREBASE_FETCH", "LIST SIZE " + list.size());

                                //THE CALLBACK IS A SIMPLE INTERFACE
                                //THE FUNCTIONALITY IS IMPLEMENTED IN THE MAINFRAGMENT WHEN THE METHOD IS CALLED
                                //SINCE THE DATABASE QUERY IS ASYNCHRONOUS A CALLBACK IS NEEDED TO PASS THE LIST
                                //WHEN READY
                                callback.onCallback(list);
                            }
                        } else {
                            Log.d("FIREBASE_FETCH", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    //LISTENS FOR CHANGES IN THE MESSAGE DOCUMENT IN THE DATABASE
    public void listenForMessageUpdates(final Activity activity, final Context context) {
        db = ((MapsActivity) activity).getDb();
        db.collection("messages")
                .whereGreaterThanOrEqualTo("unixTime", new Date().getTime()/1000)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("FIREBASE_LISTEN", "listen:error", e);
                            return;
                        }
                        //IF THERE ARE NO MESSAGES NEWER THAN NOW, IT IS THE INITIAL DATA FETCH
                        //HERE WE COULD HANDLE WHAT HAPPENS ON FIRST DATA FETCH, TOO
                        if(snapshots.getDocumentChanges().size() == 0) {
                        }
                        else {
                            Log.v("FIREBASE_LISTEN_ADDED", "SIZE: " + snapshots.getDocumentChanges().size());
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                switch (dc.getType()) {
                                    case ADDED:
                                        //HASPENDINGWRITES CHECKS IF CHANGES ARE LOCAL OR REMOTE
                                        //SO WE DON'T WANT TO DISPLAY THE BUTTON WHEN CHANGES WERE MADE LOCALLY
                                        if(dc.getDocument().getMetadata().hasPendingWrites()) {
                                            Log.v("FIREBASE_LISTEN_ADDED", "LOCAL CHANGE, DO NOTHING");
                                            break;
                                        }
                                        int duration = Toast.LENGTH_SHORT;
                                        Toast toast = Toast.makeText(context, "Someone dropped a message!", duration);
                                        toast.show();

                                        //IF REMOTE CHANGES ARE DETECTED, THIS PART FIRES
                                        //THE REFRESHBUTTON WILL BE VISIBLE
                                        //ONCLICK, THE BUTTON WILL LOOP THROUGH THE MESSAGES AND ADD THEM TO THE MAP
                                        tmpChanges.add(dc);
                                        //NOTE THAT WE HAVE TO MAINTAIN A LIST OF CHANGES IF MORE THAN ONE MESSAGES
                                        //ARE ADDED BEFORE THE BUTTON IS CLICKED
                                        //THE LIST ALSO GETS CLEARED ON BUTTON CLICK
                                        Button btn = ((MapsActivity) activity).findViewById(R.id.refresh_button);
                                        if(tmpChanges.size() == 1) {
                                            btn.setText(tmpChanges.size() + " NEW MESSAGE");
                                        }
                                        else {
                                            btn.setText(tmpChanges.size() + " NEW MESSAGES");
                                        }
                                        btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                MainFragment mainFragment = (MainFragment) ((MapsActivity) activity).getSupportFragmentManager().findFragmentByTag("MAP_FRAGMENT");
                                                for(DocumentChange newMessage : tmpChanges) {
                                                    DropMessage dm = new DropMessage(
                                                            UUID.fromString(newMessage.getDocument().get("uuid").toString()),
                                                            Float.valueOf(newMessage.getDocument().get("latitude").toString()),
                                                            Float.valueOf(newMessage.getDocument().get("longitude").toString()),
                                                            parseDate(newMessage.getDocument().get("date").toString()),
                                                            newMessage.getDocument().get("content").toString(),
                                                            Filter.valueOf(newMessage.getDocument().get("filter").toString()),
                                                            Double.valueOf(newMessage.getDocument().get("distance").toString()),
                                                            Double.valueOf(newMessage.getDocument().get("duration").toString()));

                                                    //WE COULD DO THIS BUT DROPMESSAGE WOULD HAVE TO BE SERIALIZABLE
                                                    //dc.getDocument().toObject(DropMessage.class);
                                                    boolean isOnMap = false;
                                                    for(Marker tempMarker: mainFragment.getMarkers()) {
                                                        if(dm.getDmId().toString().equals(((DropMessage) tempMarker.getTag()).getDmId().toString())) {
                                                            isOnMap = true;
                                                            break;
                                                        }
                                                    }
                                                    if(!isOnMap) {
                                                        Marker marker = mainFragment.getmMap().addMarker(new MarkerOptions()
                                                                .position(new LatLng(dm.getLatitude(), dm.getLongitude()))
                                                                .title(dm.getFilter().getName()));

                                                        marker.setTag(dm);
                                                        //marker.setIcon(BitmapDescriptorFactory.defaultMarker(MainFragment.getMarkerColor(dm.getFilter())));
                                                        Bitmap bitmap = BitmapHelper.getBitmap(context, Filter.chooseMarkerIcon(dm.getFilter().getName()));
                                                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                                                        mainFragment.getMarkers().add(marker);
                                                    }

                                                }
                                                tmpChanges.clear();
                                                v.setVisibility(View.GONE);
                                            }
                                        });
                                        btn.setVisibility(View.VISIBLE);
                                        break;
                                    case MODIFIED:
                                        Log.d("FIREBASE_LISTEN_MODIFY", "Modified city: " + dc.getDocument().getData());
                                        break;
                                    case REMOVED:
                                        Log.d("FIREBASE_LISTEN_REMOVE", "Removed city: " + dc.getDocument().getData());
                                        break;
                                }
                            }
                        }
                    }
                });

    }

    private Date parseDate(String string) {
        SimpleDateFormat parserSDF = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzzzzzzz yyyy", Locale.ENGLISH);
        try {
            Date date = parserSDF.parse(string);
            return date;
        } catch (ParseException e) {
            Log.v("PARSE_EXCEPTION", "ERROR WHILE PARSING DATE");
            e.printStackTrace();
        }
    return null;
    }
}
