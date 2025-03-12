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

    public MyLocationListener(BaiduMap baiduMap) {
        this.mBaiduMap = baiduMap;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if (location != null && mBaiduMap != null) {
            lastLatitude = location.getLatitude();
            lastLongitude = location.getLongitude();
            Log.d("Location", "Latitude: " + lastLatitude + ", Longitude: " + lastLongitude);

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(100)
                    .latitude(lastLatitude)
                    .longitude(lastLongitude)
                    .build();
            mBaiduMap.setMyLocationData(locData);

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
}