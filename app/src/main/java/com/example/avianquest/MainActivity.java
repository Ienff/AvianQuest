package com.example.avianquest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;

public class MainActivity extends Activity implements SensorEventListener {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;

    // Orientation sensors
    private SensorManager mSensorManager;
    private Sensor mOrientationSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 同意隐私协议
        LocationClient.setAgreePrivacy(true);

        // 获取地图控件引用
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 卫星地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);

        // 设置小蓝点
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));

        try {
            mLocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        myLocationListener = new MyLocationListener(mBaiduMap);
        mLocationClient.registerLocationListener(myLocationListener);

        // 配置定位参数
        initLocation();

        // 初始化方向传感器
        initOrientationSensor();

        Button btnGetLocation = findViewById(R.id.btn_get_location);
        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myLocationListener.requestCenterOnNextLocation();
                if (!mLocationClient.isStarted()) {
                    mLocationClient.start();
                } else {
                    // If already started, directly center to the last known location
                    myLocationListener.centerMapToLocation();
                }
            }
        });
    }

    private void initOrientationSensor() {
        // 初始化传感器
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setOpenGnss(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        option.setEnableSimulateGnss(false);
        option.setNeedNewVersionRgc(true);

        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();

        // 注册传感器监听
        if (mSensorManager != null && mOrientationSensor != null) {
            mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();

        // 取消传感器监听
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.stop();
    }

    // SensorEventListener 接口实现
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float direction = event.values[0]; // 0=北, 90=东, 180=南, 270=西
            Log.d("Sensor", "Direction: " + direction);
            if (myLocationListener != null) {
                myLocationListener.updateDirection(direction);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 精度变化时的处理，可以不做任何操作
    }
}