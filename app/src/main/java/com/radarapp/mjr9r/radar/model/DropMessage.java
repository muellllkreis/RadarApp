package com.radarapp.mjr9r.radar.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Matias on 12.09.2018.
 */

@Entity
public class DropMessage {

    @PrimaryKey
    @NonNull
    private UUID dmId;

    private float latitude;
    private float longitude;
    private Date date;
    private String content;
    private Filter filter;
    private double duration;
    private double distance;
    private long unixTime;

    public DropMessage(float latitude, float longitude, Date date, String content, Filter filter) {
        this.dmId = UUID.randomUUID();
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.content = content;
        this.filter = filter;
        this.unixTime = date.getTime()/1000;
    }

    public DropMessage(String uuid, float latitude, float longitude, Date date, String content, Filter filter) {
        this.dmId = UUID.fromString(uuid);
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.content = content;
        this.filter = filter;
        this.unixTime = date.getTime()/1000;
    }

    @Override
    public String toString() {
        return "LAT: " + this.latitude + "- LONG: " + this.longitude + "\n" +
                "DATE: " + this.date + "\n" +
                this.content + " ---- " + this.filter.getName();
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

    public long getUnixTime() { return unixTime; }

    public UUID getDmId() {
        return dmId;
    }

    public void setDmId(UUID dmId) {
        this.dmId = dmId;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setUnixTime(long unixTime) {
        this.unixTime = unixTime;
    }

}
