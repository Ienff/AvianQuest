package com.example.avianquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {
    private List<SamplePoint> samplePoints;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView timeView;
        public TextView speciesView;
        public TextView locationView;

        public ViewHolder(View view) {
            super(view);
            timeView = view.findViewById(R.id.timeText);
            speciesView = view.findViewById(R.id.speciesText);
            locationView = view.findViewById(R.id.locationText);
        }
    }

    public RecordsAdapter(List<SamplePoint> samplePoints) {
        this.samplePoints = samplePoints;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SamplePoint point = samplePoints.get(position);
        holder.timeView.setText(point.getTime()); // 直接显示时间字符串
        holder.speciesView.setText(point.getBirdSpecies());
        LatLng pos = point.getPosition();
        holder.locationView.setText(String.format("%.4f, %.4f",
                pos.latitude, pos.longitude));
    }

    @Override
    public int getItemCount() {
        return samplePoints.size();
    }

//    public void updateData(List<SamplePoint> newPoints) {
//        this.samplePoints = newPoints;
//        notifyDataSetChanged();
//    }
    public void updateData(List<SamplePoint> newPoints) {
        // 创建新列表以避免修改原始数据
        List<SamplePoint> sortedPoints = new ArrayList<>(newPoints);
        // 使用 Comparator 按时间字符串进行排序
        Collections.sort(sortedPoints, Comparator.comparing(SamplePoint::getTime));
        this.samplePoints = sortedPoints;
        notifyDataSetChanged();
    }
}