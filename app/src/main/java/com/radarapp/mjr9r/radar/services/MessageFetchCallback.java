package com.radarapp.mjr9r.radar.services;

import com.radarapp.mjr9r.radar.model.DropMessage;

import java.util.List;

public interface MessageFetchCallback {
    void onCallback(List<DropMessage> messageList);
}
