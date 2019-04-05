package com.wjcparkinson.patientmonitoring;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * HomingPointsParser.java Class that contains all functionality associated with parsing the JSON data
 * downloaded from the Directions API. Implemented as an AsyncTask so that it runs in the background.
 * The output of background task is stored in a HomingPathInfo data object.
 *
 * Adam Harper, s1440298
 */
public class HomingPointsParser extends AsyncTask<String, Integer, HomingPathInfo> {
    HomingTaskLoadedCallback taskCallback;
    String directionMode;

    public HomingPointsParser(Context mContext, String directionMode) {
        this.taskCallback = (HomingTaskLoadedCallback) mContext;
        this.directionMode = directionMode;
    }

    /**
     * Executes in non-UI thread - calls parse on the json data to get the route information, returns
     * the path info.
     */
    @Override
    protected HomingPathInfo doInBackground(String... jsonData) {

        JSONObject jObject;
        HomingPathInfo pathInfo = new HomingPathInfo();

        try {
            jObject = new JSONObject(jsonData[0]);
            pathInfo = parse(jObject);
        } catch (Exception e) {
            Log.d("mylog", e.toString());
            e.printStackTrace();
        }

        return pathInfo;
    }

    /**
     * Calls the taskCallback method of the context activity to indicate completion of task and
     * output the parsed data
     */
    @Override
    protected void onPostExecute(HomingPathInfo result) {
        taskCallback.onTaskDone(result);
    }

    /**
     * Parses data fetched from the google directions api, passed as a JSON object.
     */
    public HomingPathInfo parse(JSONObject jObject) {

        List<HashMap<String, String>> path = new ArrayList<>();
        String distance = "";
        String duration = "";

        JSONObject jRoute;
        JSONObject jLeg;
        JSONArray jSteps;

        try {

            // Get the single leg of the first route
            jRoute = (JSONObject) jObject.getJSONArray("routes").get(0);
            jLeg = (JSONObject) jRoute.getJSONArray("legs").get(0);
            jSteps = jLeg.getJSONArray("steps");

            // traverse the steps of the leg
            for (int k = 0; k < jSteps.length(); k++) {
                String polyline = "";
                polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                List<LatLng> list = decodePoly(polyline);

                // traverse the points in each step, adding to path
                for (int l = 0; l < list.size(); l++) {
                    HashMap<String, String> hm = new HashMap<>();
                    hm.put("lat", Double.toString((list.get(l)).latitude));
                    hm.put("lon", Double.toString((list.get(l)).longitude));
                    path.add(hm);
                }
            }

            // get the distance, duration of the route
            distance = ((JSONObject) jLeg.get("distance")).getString("text");
            duration = ((JSONObject) jLeg.get("duration")).getString("text");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create a new path info to store the path, duration and distance
        HomingPathInfo pathInfo = new HomingPathInfo();
        pathInfo.setPath(path);
        pathInfo.setDuration(duration);
        pathInfo.setDistance(distance);

        return pathInfo;
    }


    /**
     * Method to decode polyline points
     * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

}
