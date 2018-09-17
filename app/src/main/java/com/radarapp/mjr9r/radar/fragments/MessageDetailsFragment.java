package com.radarapp.mjr9r.radar.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.Marker;
import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.model.DropMessage;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessageDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageDetailsFragment extends Fragment {


    public MessageDetailsFragment() {
        // Required empty public constructor
    }

    public static MessageDetailsFragment newInstance(DropMessage message) {
        MessageDetailsFragment fragment = new MessageDetailsFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_details, container, false);

        //CardView cardView = (CardView) view.findViewById(R.id.container_location_details);
        return view;
    }

}
