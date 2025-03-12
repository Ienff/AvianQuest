package com.example.avianquest;

import android.util.Log;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;

public class MyLocationListener extends BDAbstractLocationListener {
    private final BaiduMap mBaiduMap;
    private boolean isFirstLocation = true;
    private double lastLatitude = 0;
    private double lastLongitude = 0;
    private boolean shouldCenterMap = true;
    private float direction = 0;  // Default direction (North)

    public MyLocationListener(BaiduMap baiduMap) {
        this.mBaiduMap = baiduMap;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if (location != null && mBaiduMap != null) {
            lastLatitude = location.getLatitude();
            lastLongitude = location.getLongitude();
            Log.d("Location", "Latitude: " + lastLatitude + ", Longitude: " + lastLongitude);

            updateLocationData();

            if (isFirstLocation || shouldCenterMap) {
                centerMapToLocation();
                isFirstLocation = false;
                shouldCenterMap = false;
            }
        }
    }

    public void centerMapToLocation() {
        if (lastLatitude != 0 && lastLongitude != 0) {
            LatLng latLng = new LatLng(lastLatitude, lastLongitude);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(latLng, 18.0f));
        }
    }

    public void requestCenterOnNextLocation() {
        shouldCenterMap = true;
    }

    // New method to update the direction
    public void updateDirection(float newDirection) {
        this.direction = newDirection;
        updateLocationData();
    }

    // Method to update location data with current position and direction
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
}