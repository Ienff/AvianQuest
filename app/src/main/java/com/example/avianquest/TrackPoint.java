package com.example.avianquest;

import com.baidu.mapapi.model.LatLng;
import java.util.Date;

public class TrackPoint {
    private final LatLng latLng;
    private final Date timestamp;
    private final double altitude;

    public TrackPoint(LatLng latLng, Date timestamp, double altitude) {
        this.latLng = latLng;
        this.timestamp = timestamp;
        this.altitude = altitude;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public double getAltitude() {
        return altitude;
    }
}