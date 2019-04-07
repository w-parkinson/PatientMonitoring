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
 * a URL. Once the data has downloaded, instantiates a HomingPointsParser to parse the data.
 *
 * Adam Harper, s1440298
 */
public class HomingFetchURL extends AsyncTask<String, Void, String> {
    private Context mContext;
    private String directionMode = "driving";
    private final String TAG = "HOMINGFETCHURL";

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
            Log.d(TAG, "Background task data " + data.toString());
        } catch (Exception e) {
            Log.d(TAG, e.toString());
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

            // Create an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connect to url
            urlConnection.connect();

            // Read data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();

            Log.d(TAG, "Downloaded URL: " + data.toString());

        } catch (Exception e) {
            Log.d(TAG, "Exception downloading URL: " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}

