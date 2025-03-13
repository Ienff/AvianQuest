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
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.OutputStream;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
//import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;
    private ActivityResultLauncher<Intent> createDocumentLauncher;
    private List<SamplePoint> samplePoints = new ArrayList<>();
    private Button btnAddSample;
    private MarkerOptions markerOptions;
    private BitmapDescriptor markerIcon;

    // Orientation sensors
    private SensorManager mSensorManager;
    private Sensor mOrientationSensor;

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1001;

    private boolean markerClickListenerSet = false;

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

        Button btnExportGpx = findViewById(R.id.btn_export_gpx);
        btnExportGpx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportTrackToGpx();
            }
        });

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            saveGpxToUri(uri, myLocationListener.getTrackPoints());
                        }
                    }
                }
        );

        btnAddSample = findViewById(R.id.btn_add_sample);
        markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker);
        if (markerIcon == null) {
            Log.e("MainActivity", "Failed to load marker icon");
        } else {
            Log.d("MainActivity", "Marker icon loaded successfully");
        }
        markerOptions = new MarkerOptions().icon(markerIcon).zIndex(9);

        btnAddSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSamplePoint();
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

    private void exportTrackToGpx() {
        // Check if we have any track points to export
        List<TrackPoint> trackPoints = myLocationListener.getTrackPoints();
        if (trackPoints == null || trackPoints.isEmpty()) {
            Toast.makeText(this, "No track data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create filename with timestamp
        SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String suggestedName = "track_" + fileNameFormat.format(new Date()) + ".gpx";

        // Create document creation intent
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/gpx+xml");
        intent.putExtra(Intent.EXTRA_TITLE, suggestedName);

        // Launch the file picker
        createDocumentLauncher.launch(intent);
    }

    private void saveGpxToUri(Uri uri, List<TrackPoint> trackPoints) {
        if (trackPoints == null || trackPoints.isEmpty()) {
            Toast.makeText(this, "No track data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                String gpxContent = GpxExporter.createGpxContent(trackPoints);
                outputStream.write(gpxContent.getBytes());
                outputStream.close();
                Toast.makeText(this, "Track exported successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error saving GPX file", e);
            Toast.makeText(this, "Error saving track: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addSamplePoint() {
        if (myLocationListener != null && myLocationListener.getLastLocation() != null) {
            // Create sample point at current location
            SamplePoint samplePoint = new SamplePoint(myLocationListener.getLastLocation());
            samplePoints.add(samplePoint);

            // Changed from MarkerOptions to OverlayOptions
            final LatLng position = samplePoint.getPosition();
            final String samplePointId = samplePoint.getId();

            OverlayOptions newMarker = new MarkerOptions()
                    .icon(markerIcon)
                    .position(position)
                    .title(samplePoint.getName())
                    .zIndex(9);

            // Add to map with click listener
            Marker marker = (Marker) mBaiduMap.addOverlay(newMarker);
            marker.setExtraInfo(new Bundle());
            marker.getExtraInfo().putString("sample_point_id", samplePointId);

            // Set click listener for all markers
            if (!markerClickListenerSet) {
                mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Bundle extraInfo = marker.getExtraInfo();
                        if (extraInfo != null && extraInfo.containsKey("sample_point_id")) {
                            String id = extraInfo.getString("sample_point_id");
                            // Open SamplePointActivity with the selected marker's data
                            Intent intent = new Intent(MainActivity.this, SamplePointActivity.class);
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

            Toast.makeText(this, "Sample point added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GpxExporter.exportToGpx(this, myLocationListener.getTrackPoints());
            } else {
                Toast.makeText(this, "Storage permission is required to export GPX", Toast.LENGTH_SHORT).show();
            }
        }
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