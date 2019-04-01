package com.wjcparkinson.patientmonitoring;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Vishal on 10/20/2018.
 */

public interface BhTaskLoadedCallback {
    void onTaskDone(PolylineOptions polylineOptions, int duration, int distance);
}
