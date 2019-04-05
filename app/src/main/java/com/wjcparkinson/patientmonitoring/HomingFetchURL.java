package com.wjcparkinson.patientmonitoring;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HomingFetchURL.java An asynchronous task that runs in the background. Requests Directions data from
 * a URL. Once the data has downloaded, instantiates a HomingPointsParser to parse the data. Courtesy:
 * https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
 *
 * Adam Harper, s1440298
 */
public class HomingFetchURL extends AsyncTask<String, Void, String> {
    Context mContext;
    String directionMode = "driving";

    public HomingFetchURL(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Specifies the behaviour of the background task: downloading data from the url passed as
     * strings[0].
     */
    @Override
    protected String doInBackground(String... strings) {
        // For storing data from web service
        String data = "";
        directionMode = strings[1];
        try {
            // Fetching the data from web service
            data = downloadUrl(strings[0]);
            Log.d("mylog", "Background task data " + data.toString());
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    /**
     * Executes on completion of the background task. Instantiates a parser task for interpreting
     * the downloaded data (passes in String s)
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        HomingPointsParser parserTask = new HomingPointsParser(mContext, directionMode);
        // Invokes the thread for parsing the JSON data
        parserTask.execute(s);
    }


    /**
     * Given a URL, downloads the data and returns it as a string
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("mylog", "Downloaded URL: " + data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("mylog", "Exception downloading URL: " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}

