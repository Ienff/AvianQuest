package com.example.avianquest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class SurveyFragment extends Fragment implements SensorEventListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;
    private SensorManager mSensorManager;
    private Sensor mOrientationSensor;
    private BitmapDescriptor markerIcon;
    private List<SamplePoint> samplePoints = new ArrayList<>();
    private boolean markerClickListenerSet = false;
    private Button btnExport;
    private List<TrackPoint> trackPoints = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_survey, container, false);

        // Initialize MapView
        mMapView = view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mMapView.showZoomControls(false);
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));
        btnExport = view.findViewById(R.id.btn_export);
        btnExport.setOnClickListener(v -> exportSamplePoints());

        // Initialize LocationClient
        LocationClient.setAgreePrivacy(true);
        try {
            mLocationClient = new LocationClient(requireContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        myLocationListener = new MyLocationListener(mBaiduMap);
        mLocationClient.registerLocationListener(myLocationListener);
        initLocation();

        // Initialize orientation sensor
        initOrientationSensor();

        // Set up buttons
        Button btnGetLocation = view.findViewById(R.id.btn_get_location);
        btnGetLocation.setOnClickListener(v -> {
            myLocationListener.requestCenterOnNextLocation();
            if (!mLocationClient.isStarted()) {
                mLocationClient.start();
            } else {
                myLocationListener.centerMapToLocation();
            }
        });

        Button btnAddSample = view.findViewById(R.id.btn_add_sample);
        markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker);
        btnAddSample.setOnClickListener(v -> addSamplePoint());

        return view;
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setOpenGnss(true);
        option.setLocationNotify(true);
        mLocationClient.setLocOption(option);
    }

    private void initOrientationSensor() {
        mSensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
    }

    private void addSamplePoint() {
        if (myLocationListener != null && myLocationListener.getLastLocation() != null) {
            SamplePoint samplePoint = new SamplePoint(myLocationListener.getLastLocation());
            samplePoints.add(samplePoint);

            LatLng position = samplePoint.getPosition();
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(markerIcon)
                    .position(position)
                    .title(samplePoint.getName())
                    .zIndex(9);

            Marker marker = (Marker) mBaiduMap.addOverlay(markerOptions);
            marker.setExtraInfo(new Bundle());
            marker.getExtraInfo().putString("sample_point_id", samplePoint.getId());

            if (!markerClickListenerSet) {
                mBaiduMap.setOnMarkerClickListener(clickedMarker -> {
                    Bundle extraInfo = clickedMarker.getExtraInfo();
                    if (extraInfo != null && extraInfo.containsKey("sample_point_id")) {
                        String id = extraInfo.getString("sample_point_id");
                        Toast.makeText(requireContext(), "Marker ID: " + id, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                });
                markerClickListenerSet = true;
            }

            Toast.makeText(requireContext(), "Sample point added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void addTrackPoint(LatLng latLng, double altitude) {
        TrackPoint trackPoint = new TrackPoint(latLng, altitude, System.currentTimeMillis());
        trackPoints.add(trackPoint);
    }

    private void exportSamplePoints() {
        if (GpxExporter.exportToGpx(requireContext(), trackPoints)) {
            Toast.makeText(requireContext(), "轨迹导出成功", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (mSensorManager != null && mOrientationSensor != null) {
            mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.stop();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float direction = event.values[0];
            if (myLocationListener != null) {
                myLocationListener.updateDirection(direction);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action needed
    }
}