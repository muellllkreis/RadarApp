package com.radarapp.mjr9r.radar.services;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.activities.MapsActivity;
import com.radarapp.mjr9r.radar.model.DropMessage;

public class DatabaseWriter {
    public static void storeMessageInDatabase(DropMessage dm, Activity activity, final Context context) {
        FirebaseFirestore db = ((MapsActivity) activity).getDb();
        db.collection("messages")
                .add(dm)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("FIRETEST", "DropMessage added with ID: " + documentReference.getId());
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, R.string.toast_success, duration);
                        toast.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FIRETEST", "Error adding document", e);
                    }
                });
    }
}
