package com.example.avianquest;

import com.baidu.mapapi.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class SamplePoint {
    private String id;
    private LatLng position;
    private String time;
    private String birdSpecies;
    private String gender;
    private int quantity;
    private String habitatType;
    private int distanceToLine;
    private String status;
    private String remarks;
    private String name;

    public SamplePoint(LatLng position) {
        this.id = UUID.randomUUID().toString();
        this.position = position;
        // Format current time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd-HHmm", Locale.getDefault());
        this.time = dateFormat.format(new Date());
        this.name = "Sample #" + this.time;
    }

    public SamplePoint(String id, LatLng position) {
        this.id = id;
        this.position = position;
        // Format current time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd-HHmm", Locale.getDefault());
        this.time = dateFormat.format(new Date());
        this.name = "Sample #" + id;
    }

    // Getters and setters
    public String getId() { return id; }

    public LatLng getPosition() { return position; }
    public void setPosition(LatLng position) { this.position = position; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getBirdSpecies() { return birdSpecies; }
    public void setBirdSpecies(String birdSpecies) { this.birdSpecies = birdSpecies; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getHabitatType() { return habitatType; }
    public void setHabitatType(String habitatType) { this.habitatType = habitatType; }

    public int getDistanceToLine() { return distanceToLine; }
    public void setDistanceToLine(int distanceToLine) { this.distanceToLine = distanceToLine; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}