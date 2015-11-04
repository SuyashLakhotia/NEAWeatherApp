package com.suyashlakhotia.neaweatherapp;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class NotificationService extends Service {
    public Context context = this;
    private Handler handler = new Handler();

    private String notif_title = "App Installed", notif_text = "Thank you for installing our app.";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags,int startid) {
        // if conditions for NOT notifying the user:
        new Thread() {
            public void run() {
                final JSONObject NEA_PSI = RemoteFetch_NEA.fetchNEAData("psi_update");

                if (NEA_PSI == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "Error in retrieving data.", Toast.LENGTH_LONG);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            setNotification(NEA_PSI);
                        }
                    });
                }
            }
        }.start();

        // Below is the code for notifying the user:
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(notif_title)
                        .setContentText(notif_text)
                        .setDefaults(Notification.DEFAULT_ALL);
        // Creates an explicit intent for the Recent Alerts Screen
        Intent resultIntent = new Intent(context, RecentAlertsScreenActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(RecentAlertsScreenActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1, mBuilder.build());

        return START_STICKY;
    }

    private void setNotification(JSONObject NEA_PSI) {

        RecentAlertsDB alertsDB = new RecentAlertsDB(this);
        //Generating Random Data to insert to database
        RecentAlerts recentAlerts = new RecentAlerts();
        int PSIVal = -1;

        try {
            PSIVal = NEA_PSI.getJSONObject("channel").getJSONObject("item").getJSONArray("region").getJSONObject(1).getJSONObject("record").getJSONArray("reading").getJSONObject(1).getInt("value");
        } catch (JSONException e) {
            Log.e("NEAWeatherApp", "Error getting PSI data in NotificationService.");
        }

        if (PSIVal < 101) {
            notif_title = "PSI Levels Normal";
            notif_text = "Nothing to worry about.";
        } else {
            notif_title = "PSI Levels High";
            notif_text = "You're recommended to wear a mask or get indoors.";
            recentAlerts.description = notif_text;
            recentAlerts.title = notif_title;
            alertsDB.insert(recentAlerts);
        }

    }
}
