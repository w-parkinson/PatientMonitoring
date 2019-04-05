package com.wjcparkinson.patientmonitoring;

import android.net.RouteInfo;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Interface that must be implemented by
 */

public interface HomingTaskLoadedCallback {
    void onTaskDone(HomingPathInfo pathInfo);
}
