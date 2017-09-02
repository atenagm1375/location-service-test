package com.atena.test.locationservicetest;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by atena on 9/2/2017.
 */

public class GeofencingIntentService extends IntentService {
    public static String ACTION;
    public GeofencingIntentService() {
        super("GeofenceIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            Toast.makeText(getApplicationContext(), "Geofence error code: " + event.getErrorCode(), Toast.LENGTH_SHORT).show();
            return;
        }
        if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_DWELL)
            sendNotification();
    }

    private void sendNotification() {
        Uri notifUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Geofence alaram")
                .setContentText("GEOFENCE_TRANSITION_DWELL")
                .setSound(notifUri).setLights(Color.BLUE, 500, 500);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notifBuilder.build());
    }
}
