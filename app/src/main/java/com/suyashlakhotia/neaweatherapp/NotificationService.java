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
    public int onStartCommand(Intent intent, int flags, int startid) {
        // Fetch PSI Data from NEA:
        new Thread() {
            public void run() {
                final JSONObject NEA_PSI = RemoteFetch_NEA.fetchNEAData("psi_update");

                if (NEA_PSI == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Log.e("NotificationService", "onStartCommand(): Error retrieving data.");
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

        // Notify User via Android Notifications:
        if (notif_title == "PSI Levels High") {
            NotificationCompat.Builder mBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(notif_title)
                            .setContentText(notif_text)
                            .setDefaults(Notification.DEFAULT_ALL);
            Intent resultIntent = new Intent(context, RecentAlertsScreenActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(RecentAlertsScreenActivity.class);
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
        }

        return START_STICKY;
    }

    private void setNotification(JSONObject NEA_PSI) {
        RecentAlertsDB alertsDB = new RecentAlertsDB(this);
        RecentAlerts recentAlerts = new RecentAlerts();
        int PSIVal = -1;

        try {
            PSIVal = NEA_PSI.getJSONObject("channel").getJSONObject("item").getJSONArray("region").getJSONObject(1).getJSONObject("record").getJSONArray("reading").getJSONObject(1).getInt("value");
        } catch (JSONException e) {
            Log.e("NotificationService", "setNotification(): One or more fields not found in the JSON data.");
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
