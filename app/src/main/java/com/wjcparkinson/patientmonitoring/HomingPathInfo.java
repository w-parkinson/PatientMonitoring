package com.wjcparkinson.patientmonitoring;

import java.util.HashMap;
import java.util.List;

public class HomingPathInfo {
    private String duration;
    private String distance;
    private List<HashMap<String, String>> path;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public List<HashMap<String, String>> getPath() {
        return path;
    }

    public void setPath(List<HashMap<String, String>> path) {
        this.path = path;
    }
}
