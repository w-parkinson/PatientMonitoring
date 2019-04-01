package com.wjcparkinson.patientmonitoring;

import java.util.HashMap;
import java.util.List;

public class BhRouteInfo {
    private int duration;
    private int distance;
    private List<List<HashMap<String, String>>> route;


    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<List<HashMap<String, String>>> getRoute() {
        return route;
    }

    public void setRoute(List<List<HashMap<String, String>>> route) {
        this.route = route;
    }
}
