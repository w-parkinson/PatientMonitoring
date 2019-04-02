package com.wjcparkinson.patientmonitoring;
//s1880159 Chao Chen
//Set alarm to be on, off, and delete
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import static android.content.Context.ALARM_SERVICE;
import static com.wjcparkinson.patientmonitoring.AlarmActivity.PREFS;

public class AlarmCustomAdapter extends ArrayAdapter<AlarmDataModel> implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private ArrayList<AlarmDataModel> dataSet;
    Context mContext;

    AlarmManager alarm_manager;
    PendingIntent pending_intent;
    LayoutInflater mInflator;
    Activity mActivity;

    // View lookup cache
    private static class ViewHolder {
        TextView time;
        Button alarm_on, alarm_off, delete_row;
        // initialize alarm manager
    }

    public AlarmCustomAdapter(ArrayList<AlarmDataModel> data, Context context, LayoutInflater mInflator, Activity mActivity) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mInflator = mInflator;
        this.mActivity = mActivity;

    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClick(View v) {

        int position = (Integer) v.getTag();
        Object object = getItem(position);
        AlarmDataModel dataModel = (AlarmDataModel) object;
    }

    private int lastPosition = -1;

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public View getView(final int position, View convertView, ViewGroup parent) {

        alarm_manager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);

        // Get the data item for this position
        final AlarmDataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.time = (TextView) convertView.findViewById(R.id.textView);
            viewHolder.alarm_on = (Button) convertView.findViewById(R.id.alarm_on);
            viewHolder.alarm_off = (Button) convertView.findViewById(R.id.alarm_off);
            viewHolder.delete_row = (Button) convertView.findViewById(R.id.delete);

            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        // create an instance of an calendar
        final Calendar calendar = Calendar.getInstance();

        //create an intent to Alarm receiver class
        final Intent my_intent = new Intent(getContext(), AlarmReceiver.class);

        String abcd = dataModel.getTime();

        int l = abcd.indexOf(':');
        int ll = abcd.lastIndexOf(':');
        int yui;

        for (yui = l + 1; yui < abcd.length(); yui++)
            if (abcd.charAt(yui) == ':')
                break;

        String opqw, button_on_or_off;

        final int hour = Integer.parseInt(abcd.substring(0, l));
        if (ll == l) {
            opqw = abcd.substring(l + 1);
            button_on_or_off = "";
        } else {
            opqw = abcd.substring(l + 1, yui);
            button_on_or_off = abcd.substring(yui + 1);
        }

        Log.d("Tag :: ", opqw);
        final int minute = Integer.parseInt(opqw);

        // convert int to string
        String hour_string = String.valueOf(hour);
        String minute_string = String.valueOf(minute);

        // String button_on_or_off = abcd.substring(ll+1);
        // if(button_on_or_off.equals(minute_string))
        // button_on_or_off = "";

        Log.d("Tag button :: ", button_on_or_off);

        if (hour > 12) {
            hour_string = String.valueOf(hour - 12);
        }
        if (hour == 0) {
            hour_string = "00";
        }
        if (minute < 10) {
            minute_string = "0" + String.valueOf(minute);
        }

        final String hjk = hour_string + ':' + minute_string;

        viewHolder.time.setText(hour_string + ':' + minute_string);

        if (button_on_or_off.equals("ONN")) {
            viewHolder.alarm_on.setVisibility(View.INVISIBLE);
            viewHolder.alarm_off.setVisibility(View.VISIBLE);
        }
        if(button_on_or_off.equals("OFF")) {
            viewHolder.alarm_on.setVisibility(View.VISIBLE);
            viewHolder.alarm_off.setVisibility(View.INVISIBLE);
        }

        viewHolder.alarm_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // to have current time
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat df_check = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate1 = df_check.format(cal.getTime());
                String xy[] = formattedDate1.split(" ");
                String xy1[] = xy[1].split(":");
                final int current_hour_2 = Integer.parseInt(xy1[0]);
                final int current_minutes_2 = Integer.parseInt(xy1[1]);

                Log.d("Tag :: ", current_hour_2 + " " + hour + " :: " + current_minutes_2 + " " + minute);

                if (current_hour_2 < hour || current_minutes_2 <= minute) {

                    Log.d("Tag :: ", "pressed alarm on button successfully !");

                    // setting calendar instance with the hour and minute we picked on time picker
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    // put in extra string in my intent to tell clock you pressed on button
                    my_intent.putExtra("extra", "alarm on");
                    my_intent.putExtra("extra1", "alarm on at time :: " + hjk);

                    // create a pending intent that delays the intnet until the specified calendar time
                    pending_intent = PendingIntent.getBroadcast(getContext(), position, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // set alarm manager
                    alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending_intent);

                    Calendar c1 = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(c1.getTime());
                    String y[] = formattedDate.split(" ");
                    String y1[] = y[1].split(":");
                    int current_hour = Integer.parseInt(y1[0]);
                    int current_minutes = Integer.parseInt(y1[1]);

                    String tyu = dataModel.getTime();
                    String j[] = tyu.split(":");
                    int hour = Integer.parseInt(j[0]);
                    int minute = Integer.parseInt(j[1]);

                    SharedPreferences q1 = getContext().getSharedPreferences(PREFS, 0);
                    String get_string1 = q1.getString("message", "not found");
                    String bc[] = get_string1.split(" ");
                    String put_string1 = "";
                    for (int bcd1 = 0; bcd1 < bc.length; bcd1++)
                        if (bc[bcd1].equals(tyu))
                            put_string1 = put_string1 + hour + ":" + minute + ":" + "ONN ";
                        else
                            put_string1 = put_string1 + bc[bcd1] + " ";

                    SharedPreferences.Editor e123 = q1.edit();
                    e123.putString("message", put_string1);
                    e123.commit();

                    int p = hour * 60 + minute;
                    int m = current_hour * 60 + current_minutes;

                    int diff = p - m;

                    if (diff < 60)
                        Snackbar.make(view, "Alarm set for " + diff + " minutes from now.", Snackbar.LENGTH_LONG)
                                .setAction("No action", null).show();
                    else
                        Snackbar.make(view, "Alarm set for " + (diff / 60) + " hours and " + (diff % 60) + " minutes from now.", Snackbar.LENGTH_LONG)
                                .setAction("No action", null).show();

                    viewHolder.alarm_on.setVisibility(View.INVISIBLE);
                    //viewHolder.alarm_off.setVisibility(View.VISIBLE);
                } else {
                    Log.d("Tag :: ", "pressed alarm on button successfully !");

                    // setting calendar instance with the hour and minute we picked on time picker
                    calendar.set(Calendar.HOUR_OF_DAY, hour + 24);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    // put in extra string in my intent to tell clock you pressed on button
                    my_intent.putExtra("extra", "alarm on");
                    my_intent.putExtra("extra1", "alarm on at time :: " + hjk);

                    // create a pending intent that delays the intnet until the specified calendar time
                    pending_intent = PendingIntent.getBroadcast(getContext(), position, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // set alarm manager
                    alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending_intent);

                    Calendar c1 = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(c1.getTime());
                    String y[] = formattedDate.split(" ");
                    String y1[] = y[1].split(":");
                    int current_hour = Integer.parseInt(y1[0]);
                    int current_minutes = Integer.parseInt(y1[1]);

                    String tyu = dataModel.getTime();
                    String j[] = tyu.split(":");
                    int hour = Integer.parseInt(j[0]);
                    int minute = Integer.parseInt(j[1]);

                    SharedPreferences q1 = getContext().getSharedPreferences(PREFS, 0);
                    String get_string1 = q1.getString("message", "not found");
                    String bc[] = get_string1.split(" ");
                    String put_string1 = "";
                    for (int bcd1 = 0; bcd1 < bc.length; bcd1++)
                        if (bc[bcd1].equals(tyu))
                            put_string1 = put_string1 + hour + ":" + minute + ":" + "ONN ";
                        else
                            put_string1 = put_string1 + bc[bcd1] + " ";

                    SharedPreferences.Editor e123 = q1.edit();
                    e123.putString("message", put_string1);
                    e123.commit();

                    int p = hour * 60 + minute;
                    int m = current_hour * 60 + current_minutes;

                    int diff = p - m + (24 * 60);

                    if (diff < 60)
                        Snackbar.make(view, "Alarm set for " + diff + " minutes from now.", Snackbar.LENGTH_LONG)
                                .setAction("No action", null).show();
                    else
                        Snackbar.make(view, "Alarm set for " + (diff / 60) + " hours and " + (diff % 60) + " minutes from now.", Snackbar.LENGTH_LONG)
                                .setAction("No action", null).show();

                    viewHolder.alarm_on.setVisibility(View.INVISIBLE);
                    //viewHolder.alarm_off.setVisibility(View.VISIBLE);
                }
            }
        });

        viewHolder.alarm_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // put in extra string in my intent to tell clock you pressed off button

                // to have current time
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat df_check = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate1 = df_check.format(cal.getTime());
                String xy[] = formattedDate1.split(" ");
                String xy1[] = xy[1].split(":");
                final int current_hour_2 = Integer.parseInt(xy1[0]);
                final int current_minutes_2 = Integer.parseInt(xy1[1]);

                Log.d("Tag :: ", current_hour_2 + " " + hour + " :: " + current_minutes_2 + " " + minute);

                if (current_hour_2 == hour && (current_minutes_2 == minute || current_minutes_2 == ((minute + 1) % 60))) {

                    final AlertDialog.Builder mBuilder = new AlertDialog.Builder(mActivity);
                    final View mView = mInflator.inflate(R.layout.custom_dialog, null);
                    final TextView question = (TextView) mView.findViewById(R.id.textView);
                    final EditText answer =  mView.findViewById(R.id.edit_text);
                    Button ok = (Button) mView.findViewById(R.id.button);

                    mBuilder.setView(mView);
                    final AlertDialog dialog = mBuilder.create();
                    dialog.show();

                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            viewHolder.alarm_off.setVisibility(View.INVISIBLE);
                            viewHolder.alarm_on.setVisibility(View.VISIBLE);

                            // put in extra string in my intent to tell clock you pressed off button
                            my_intent.putExtra("extra", "alarm off");
                            my_intent.putExtra("extra1", "alarm off at time :: " + hjk);

                            pending_intent = PendingIntent.getBroadcast(getContext(), position, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            // cancel the pending intent
                            alarm_manager.cancel(pending_intent);

                            // stop the ringtone
                            getContext().sendBroadcast(my_intent);

                            dialog.dismiss();

                            String tyu = dataModel.getTime();
                            String lkj[] = tyu.split(":");
                            SharedPreferences q1 = getContext().getSharedPreferences(PREFS, 0);
                            String get_string1 = q1.getString("message", "not found");
                            String bc[] = get_string1.split(" ");
                            String put_string1 = "";
                            for (int bcd1 = 0; bcd1 < bc.length; bcd1++)
                                if (bc[bcd1].equals(tyu))
                                    put_string1 = put_string1 + hour + ":" + minute + " ";
                                else
                                    put_string1 = put_string1 + bc[bcd1] + " ";

                            SharedPreferences.Editor e123 = q1.edit();
                            e123.putString("message", put_string1);
                            e123.commit();
                            Intent intent101 = mActivity.getIntent();
                            mActivity.finish();
                            mActivity.startActivity(intent101);
                        }
                    });
                }
            }
});

                    viewHolder.delete_row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            SharedPreferences ex1 = getContext().getSharedPreferences(PREFS, 0);
                            String c = ex1.getString("message", "empty");

                            String ghi = dataModel.getTime();
                            SharedPreferences.Editor editor12 = ex1.edit();
                            String lm[] = c.split(" ");
                            String fs = "";
                            for (int y5 = 0; y5 < lm.length; y5++) {
                                if (!lm[y5].equals(ghi))
                                    fs = fs + lm[y5] + " ";
                            }
                            editor12.putString("message", fs);
                            editor12.commit();

                            // put in extra string in my intent to tell clock you pressed off button
                            my_intent.putExtra("extra", "alarm off");
                            my_intent.putExtra("extra1", "alarm off at time :: " + hjk);

                            pending_intent = PendingIntent.getBroadcast(getContext(), position, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            // cancel the pending intent
                            alarm_manager.cancel(pending_intent);

                            // stop the ringtone
                            getContext().sendBroadcast(my_intent);

                            dataSet.remove(position);
                            notifyDataSetChanged();
                        }
                    });

        // Return the completed view to render on screen
        return convertView;}
}
