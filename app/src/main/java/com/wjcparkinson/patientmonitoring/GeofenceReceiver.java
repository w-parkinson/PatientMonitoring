//William Parkinson                 s1433610
package com.wjcparkinson.patientmonitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceReceiver extends BroadcastReceiver {

    String tag = "receiver tag";        //Tag used for this class
    String phoneNumber = "07912984316";     //Number of phone to which message should be sent.
    String smsMessageOut = "Attention: A patient has left their designated area";
    String smsMessageIn = "Attention: A patient has entered the designated area";

    //Called when a geofence transition is detected. Used to send a sms message to phone.
    @Override
    public void onReceive(Context context, Intent intent) {

        //Get the geofence transition type.
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        //If the patient leaves the geofence then send a corresponding sms message.
        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            try{
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber,null,smsMessageOut,null,null);
            } catch(Exception e){
                Log.e("tag","Message failed to send");
            }
        }

        //If the patient enters the geofence then send a corresponding sms message.
        if(geofenceTransition== Geofence.GEOFENCE_TRANSITION_ENTER) {
            try{
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber,null,smsMessageIn,null,null);
            } catch(Exception e){
                Log.e("tag","Message failed to send");
            }
        }
    }
}
