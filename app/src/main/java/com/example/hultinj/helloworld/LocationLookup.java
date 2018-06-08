package com.example.hultinj.helloworld;

import org.joda.time.DateTime;
import org.parceler.Parcel;

@Parcel
public class LocationLookup {
    private double origLat;
    private double origLng;
    private double endLat;
    private double endLng;
    private String timestamp;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private String key;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    public double getOrigLat() {
        return origLat;
    }

    public void setOrigLat(double origLat) {
        this.origLat = origLat;
    }

    public double getOrigLng() {
        return origLng;
    }

    public void setOrigLng(double origLng) {
        this.origLng = origLng;
    }

    public double getEndLat() {
        return endLat;
    }

    public void setEndLat(double endLat) {
        this.endLat = endLat;
    }

    public double getEndLng() {
        return endLng;
    }

    public void setEndLng(double endLng) {
        this.endLng = endLng;
    }
}