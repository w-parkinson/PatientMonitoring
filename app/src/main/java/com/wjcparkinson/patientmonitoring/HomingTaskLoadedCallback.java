package com.wjcparkinson.patientmonitoring;

import android.net.RouteInfo;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Vishal on 10/20/2018.
 */

public interface HomingTaskLoadedCallback {
    void onTaskDone(HomingPathInfo pathInfo);
}
