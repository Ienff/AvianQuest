package com.example.avianquest;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class RecordsFragment extends Fragment {
    private RecordsAdapter adapter;
    private DatabaseHelper dbHelper;

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
        adapter.updateData(points);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recordsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecordsAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        FloatingActionButton refreshButton = view.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> loadRecords());

        dbHelper = new DatabaseHelper(getContext());
        loadRecords();

        return view;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}