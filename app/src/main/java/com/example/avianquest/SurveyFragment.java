package com.example.avianquest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
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
    private ActivityResultLauncher<Intent> saveFileLauncher;

    private class MyLocationListener extends BDAbstractLocationListener {
        private final BaiduMap mBaiduMap;
        private BDLocation lastLocation;
        private boolean shouldCenter = true;
        private float direction = 0;
        private SurveyFragment fragment;

        public MyLocationListener(BaiduMap baiduMap) {
            this.mBaiduMap = baiduMap;
            this.fragment = SurveyFragment.this;
        }

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mBaiduMap == null) return;

            lastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            double altitude = location.getAltitude();

            // Add track point
            addTrackPoint(latLng, altitude);

            MyLocationData.Builder builder = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(direction)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude());
            mBaiduMap.setMyLocationData(builder.build());

            if (shouldCenter) {
                centerMapToLocation();
                shouldCenter = false;
            }
        }

        public void requestCenterOnNextLocation() {
            shouldCenter = true;
        }

        public void centerMapToLocation() {
            if (lastLocation != null) {
                LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(status);
            }
        }

        public void updateDirection(float direction) {
            this.direction = direction;
            if (lastLocation != null) {
                MyLocationData.Builder builder = new MyLocationData.Builder()
                        .accuracy(lastLocation.getRadius())
                        .direction(direction)
                        .latitude(lastLocation.getLatitude())
                        .longitude(lastLocation.getLongitude());
                mBaiduMap.setMyLocationData(builder.build());
            }
        }

        public BDLocation getLastLocation() {
            return lastLocation;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        saveFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            if (GpxExporter.saveGpxToUri(requireContext(), uri, trackPoints)) {
                                Toast.makeText(requireContext(), "轨迹导出成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private void exportSamplePoints() {
        if (trackPoints.isEmpty()) {
            Toast.makeText(requireContext(), "没有轨迹数据可导出", Toast.LENGTH_SHORT).show();
            return;
        }

        GpxExporter.createSaveFileIntent(requireContext(), trackPoints, saveFileLauncher);
    }

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
            BDLocation location = myLocationListener.getLastLocation();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            SamplePoint samplePoint = new SamplePoint(latLng);
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
                mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Bundle extraInfo = marker.getExtraInfo();
                        if (extraInfo != null && extraInfo.containsKey("sample_point_id")) {
                            String id = extraInfo.getString("sample_point_id");
                            // Open SamplePointActivity with the selected marker's data
                            Intent intent = new Intent(requireContext(), SamplePointActivity.class);
                            intent.putExtra(SamplePointActivity.EXTRA_LATITUDE, marker.getPosition().latitude);
                            intent.putExtra(SamplePointActivity.EXTRA_LONGITUDE, marker.getPosition().longitude);
                            intent.putExtra(SamplePointActivity.EXTRA_SAMPLE_POINT_ID, id);
                            startActivity(intent);
                            return true;
                        }
                        return false;
                    }
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

    public boolean onExitSurvey(Runnable onConfirm) {
        new AlertDialog.Builder(requireContext())
                .setTitle("提示")
                .setMessage("退出调查界面后就将保存样线，无法继续更新，确认离开吗？")
                .setPositiveButton("确认", (dialog, which) -> {
                    // Export track points before leaving
                    exportSamplePoints();
                    onConfirm.run();
                })
                .setNegativeButton("取消", null)
                .show();
        return false;
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