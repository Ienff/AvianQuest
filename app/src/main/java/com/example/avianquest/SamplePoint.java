package com.example.avianquest;

import com.baidu.mapapi.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class SamplePoint {
    private String id;
    private LatLng position;
    private Date time;
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
        this.time = new Date();
        this.name = "Sample #" + new SimpleDateFormat("MMdd-HHmm", Locale.getDefault()).format(time);
    }

    // Getters and setters
    public String getId() { return id; }

    public LatLng getPosition() { return position; }
    public void setPosition(LatLng position) { this.position = position; }

    public Date getTime() { return time; }
    public void setTime(Date time) { this.time = time; }

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