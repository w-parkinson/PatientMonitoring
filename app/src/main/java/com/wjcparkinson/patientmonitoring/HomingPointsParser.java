package com.wjcparkinson.patientmonitoring;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Vishal on 10/20/2018.
 */

public class HomingPointsParser extends AsyncTask<String, Integer, HomingRouteInfo> {
    HomingTaskLoadedCallback taskCallback;
    String directionMode = "driving";

    public HomingPointsParser(Context mContext, String directionMode) {
        this.taskCallback = (HomingTaskLoadedCallback) mContext;
        this.directionMode = directionMode;
    }

    // Parsing the data in non-ui thread
    @Override
    protected HomingRouteInfo doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;
        int duration = 0;
        int distance = 0;

        HomingRouteInfo routeInfo = new HomingRouteInfo();

        try {
            jObject = new JSONObject(jsonData[0]);
            Log.d("mylog", jsonData[0].toString());
            HomingDataParser parser = new HomingDataParser();
            Log.d("mylog", parser.toString());

            // Starts parsing data
            routes = parser.parse(jObject);
            duration = parser.getFirstLegDuration(jObject);
            distance = parser.getFirstLegDistance(jObject);
            Log.d("mylog", "Routes " + routes.toString());
            Log.d("mylog", "Duration " + duration);
            Log.d("mylog", "Distance " + distance);

        } catch (Exception e) {
            Log.d("mylog", e.toString());
            e.printStackTrace();
        }

        routeInfo.setRoute(routes);
        routeInfo.setDistance(distance);
        routeInfo.setDuration(duration);
        return routeInfo;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(HomingRouteInfo result) {
        List<List<HashMap<String, String>>> routes = result.getRoute();
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;


        // Traversing through all the routes
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            // Fetching i-th route
            List<HashMap<String, String>> path = routes.get(i);
            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            if (directionMode.equalsIgnoreCase("walking")) {
                lineOptions.width(10);
                lineOptions.color(Color.MAGENTA);
            } else {
                lineOptions.width(20);
                lineOptions.color(Color.BLUE);
            }
            Log.d("mylog", "onPostExecute lineoptions decoded");
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            taskCallback.onTaskDone(lineOptions, result.getDuration(), result.getDistance());
        } else {
            Log.d("mylog", "without Polylines drawn");
        }
    }
}
