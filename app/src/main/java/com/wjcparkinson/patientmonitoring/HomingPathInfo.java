package com.wjcparkinson.patientmonitoring;

import com.google.android.gms.maps.model.LatLng;
import java.util.List;

/**
 * Data class that contains a path's duration, distance, start address, end address and a list of
 * points in the route, as well as getters and setters for each.
 *
 * Adam Harper, s1440298
 */
public class HomingPathInfo {
    private String duration;
    private String distance;
    private String startAddr;
    private String endAddr;
    private List<LatLng> path;

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

    public String getStartAddr() {
        return startAddr;
    }

    public void setStartAddr(String startAddr) {
        this.startAddr = startAddr;
    }

    public String getEndAddr() {
        return endAddr;
    }

    public void setEndAddr(String endAddr) {
        this.endAddr = endAddr;
    }

    public List<LatLng> getPath() {
        return path;
    }

    public void setPath(List<LatLng> path) {
        this.path = path;
    }
}
