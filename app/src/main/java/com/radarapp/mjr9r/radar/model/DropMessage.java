package com.radarapp.mjr9r.radar.model;

import java.util.Date;

/**
 * Created by Matias on 12.09.2018.
 */

public class DropMessage {

    private float latitude;
    private float longitude;
    private Date date;
    private String content;
    private Filter filter;
    private double duration;
    private double distance;

    public DropMessage(float latitude, float longitude, Date date, String content, Filter filter) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.content = content;
        this.filter = filter;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public Date getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public Filter getFilter() {
        return filter;
    }

    public double getDuration() {
        return duration;
    }

    public double getDistance() {
        return distance;
    }

}
