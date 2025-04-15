package com.example.avianquest;

import android.util.Log;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyLocationListener extends BDAbstractLocationListener {
    private final BaiduMap mBaiduMap;
    private boolean isFirstLocation = true;
    private double lastLatitude = 0;
    private double lastLongitude = 0;
    private boolean shouldCenterMap = true;
    private float direction = 0;  // Default direction (North)

    // Path tracking variables
    private List<TrackPoint> trackPoints = new ArrayList<>();
    private Polyline trackLine;

    public MyLocationListener(BaiduMap baiduMap) {
        this.mBaiduMap = baiduMap;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if (location != null && mBaiduMap != null) {
            lastLatitude = location.getLatitude();
            lastLongitude = location.getLongitude();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            double altitude = location.getAltitude();
            addTrackPoint(latLng, altitude);

            Log.d("Location", "Latitude: " + lastLatitude + ", Longitude: " + lastLongitude);

            updateLocationData();

            // Add current point to track
            LatLng currentPoint = new LatLng(lastLatitude, lastLongitude);
            addTrackPoint(currentPoint, location.getAltitude());

            if (isFirstLocation || shouldCenterMap) {
                centerMapToLocation();
                isFirstLocation = false;
                shouldCenterMap = false;
            }
        }
    }

    // Method to add a point to the track
    private void addTrackPoint(LatLng point, double altitude) {
        // Only add points that are a minimum distance from the last point
        if (!trackPoints.isEmpty()) {
            TrackPoint lastPoint = trackPoints.get(trackPoints.size() - 1);
            double distance = getDistance(lastPoint.getLatLng(), point);

            // If distance is less than 3 meters, don't add the point
            if (distance < 3) {
                return;
            }
        }

        TrackPoint trackPoint = new TrackPoint(point, altitude, System.currentTimeMillis());
        trackPoints.add(trackPoint);
        drawTrack();
    }

    // Get all track points for export
    public List<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    // Calculate distance between two points
    private double getDistance(LatLng point1, LatLng point2) {
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(point2.latitude - point1.latitude);
        double dLng = Math.toRadians(point2.longitude - point1.longitude);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(point1.latitude)) * Math.cos(Math.toRadians(point2.latitude)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }

    // Draw the track on the map
    private void drawTrack() {
        if (trackPoints.size() < 2) {
            return; // Need at least 2 points to draw a line
        }

        if (trackLine != null) {
            trackLine.remove(); // Remove existing line
        }

        // Convert TrackPoints to LatLngs for drawing
        List<LatLng> points = new ArrayList<>();
        for (TrackPoint tp : trackPoints) {
            points.add(tp.getLatLng());
        }

        // Create new polyline
        PolylineOptions polylineOptions = new PolylineOptions()
                .width(10) // Line width
                .color(0xAAFF0000) // Red with transparency
                .points(points);

        trackLine = (Polyline) mBaiduMap.addOverlay(polylineOptions);
    }

    // Existing methods...
    public void centerMapToLocation() {
        if (lastLatitude != 0 && lastLongitude != 0) {
            LatLng latLng = new LatLng(lastLatitude, lastLongitude);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(latLng, 18.0f));
        }
    }

    public void requestCenterOnNextLocation() {
        shouldCenterMap = true;
    }

    public void updateDirection(float newDirection) {
        this.direction = newDirection;
        updateLocationData();
    }

    private void updateLocationData() {
        if (lastLatitude != 0 && lastLongitude != 0) {
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(50) // Default accuracy if not available
                    .direction(direction)  // Use the current direction
                    .latitude(lastLatitude)
                    .longitude(lastLongitude)
                    .build();
            mBaiduMap.setMyLocationData(locData);
        }
    }

    public LatLng getLastLocation() {
        return new LatLng(lastLatitude, lastLongitude);
    }
}