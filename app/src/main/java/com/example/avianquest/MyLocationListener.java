package com.example.avianquest;

import android.util.Log;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;

public class MyLocationListener extends BDAbstractLocationListener {
    private final BaiduMap mBaiduMap;
    boolean isFirstLocation = true;

    public MyLocationListener(BaiduMap baiduMap) {
        this.mBaiduMap = baiduMap;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if (location != null && mBaiduMap != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(100)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
            mBaiduMap.setMyLocationData(locData);

            if (isFirstLocation) {
                LatLng latLng = new LatLng(latitude, longitude);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(latLng, 18.0f));
                isFirstLocation = false;
            }
        }
    }
}