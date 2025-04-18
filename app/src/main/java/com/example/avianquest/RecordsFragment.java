package com.example.avianquest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.model.LatLng;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordsFragment extends Fragment {
    private RecordsAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<SamplePoint> samplePoints = new ArrayList<>();

    private static final int EXPORT_PDF_REQUEST_CODE = 2;

    private void loadRecords() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<SamplePoint> points = new ArrayList<>();

        String[] projection = {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_TIME,
                DatabaseHelper.COLUMN_LATITUDE,
                DatabaseHelper.COLUMN_LONGITUDE,
                DatabaseHelper.COLUMN_BIRD_SPECIES,
                DatabaseHelper.COLUMN_GENDER,
                DatabaseHelper.COLUMN_QUANTITY,
                DatabaseHelper.COLUMN_HABITAT_TYPE,
                DatabaseHelper.COLUMN_DISTANCE,
                DatabaseHelper.COLUMN_STATUS,
                DatabaseHelper.COLUMN_REMARKS
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_SAMPLE_POINTS,
                projection,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_TIME + " DESC"
        );

        while (cursor.moveToNext()) {
            double latitude = cursor.getDouble(2);
            double longitude = cursor.getDouble(3);
            LatLng position = new LatLng(latitude, longitude);

            // 创建样点时直接设置ID
            String id = cursor.getString(0);
            SamplePoint point = new SamplePoint(id, position);

            point.setTime(cursor.getString(1));
            point.setBirdSpecies(cursor.getString(4));
            point.setGender(cursor.getString(5));
            point.setQuantity(cursor.getInt(6));
            point.setHabitatType(cursor.getString(7));
            point.setDistanceToLine(cursor.getInt(8));
            point.setStatus(cursor.getString(9));
            point.setRemarks(cursor.getString(10));
            points.add(point);
        }

        cursor.close();
        db.close();
        samplePoints = points;
        adapter.updateData(points);
    }

    private void initViews(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recordsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecordsAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Initialize export button - changed to FloatingActionButton
        FloatingActionButton exportButton = view.findViewById(R.id.exportButton);
        exportButton.setOnClickListener(v -> exportToPdf());

        // Initialize refresh button
        FloatingActionButton refreshButton = view.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> loadRecords());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);
        dbHelper = new DatabaseHelper(getContext());
        initViews(view);
        loadRecords();
        return view;
    }

    private void exportToPdf() {
        if (getContext() == null) return;

        // Create intent for folder selection
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        // Set default file name
        String fileName = "鸟类样线调查记录_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date()) + ".pdf";
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        // Launch folder picker
        startActivityForResult(intent, EXPORT_PDF_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXPORT_PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    Context context = getContext();
                    if (context == null) return;

                    OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        boolean success = PdfExporter.exportToPdf(outputStream, samplePoints);
                        outputStream.close();

                        String message = success ? "PDF导出成功" : "PDF导出失败";
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "PDF导出失败: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}