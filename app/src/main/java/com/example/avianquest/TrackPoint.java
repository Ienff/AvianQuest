package com.example.avianquest;

import com.baidu.mapapi.model.LatLng;
import java.util.Date;

public class TrackPoint {
    private LatLng latLng;
    private double altitude;
    private long timestamp;

    public TrackPoint(LatLng latLng, double altitude, long timestamp) {
        this.latLng = latLng;
        this.altitude = altitude;
        this.timestamp = timestamp;
    }

    public LatLng getLatLng() { return latLng; }
    public double getAltitude() { return altitude; }
    public long getTimestamp() { return timestamp; }
}