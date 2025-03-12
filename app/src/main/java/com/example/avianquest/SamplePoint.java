package com.example.avianquest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class SamplePoint {
    private String id;
    private com.baidu.mapapi.model.LatLng position;
    private String name;
    private Date timestamp;
    private String notes;

    public SamplePoint(com.baidu.mapapi.model.LatLng position) {
        this.id = UUID.randomUUID().toString();
        this.position = position;
        this.timestamp = new Date();
        this.name = "Sample " + new SimpleDateFormat("HH:mm:ss", Locale.US).format(timestamp);
    }

    // Getters and setters
    public String getId() { return id; }
    public com.baidu.mapapi.model.LatLng getPosition() { return position; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Date getTimestamp() { return timestamp; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}