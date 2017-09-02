package com.atena.test.locationservicetest;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by atena on 8/29/2017.
 */

public class LocationService extends Service {
    public static final String BROADCAST_ACTION = "broadcast action";
    Intent intent;
    PendingIntent pendingIntent;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    LocationDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
        db = new LocationDatabase(getApplicationContext());
        setupLocationRequest();
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            db.saveRecord(location.getLatitude(), location.getLongitude(), location.getProvider());
            db.test();
            intent.putExtra("Latitude", location.getLatitude());
            intent.putExtra("Longitude", location.getLongitude());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    };

    ResultCallback resultCallback = new ResultCallback() {
        @Override
        public void onResult(@NonNull Result result) {
            Toast.makeText(getApplicationContext(), "result: " + result.getStatus().toString(), Toast.LENGTH_SHORT).show();
        }
    };

    GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
            LocationServices.GeofencingApi.addGeofences(googleApiClient, createGeofencingRequest(),
                    getGeofencePendingIntent()).setResultCallback(resultCallback);
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    GoogleApiClient.OnConnectionFailedListener failedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(getApplicationContext(), connectionResult.toString(), Toast.LENGTH_LONG).show();
        }
    };

    protected synchronized void setupLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(1000);
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(failedListener)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (pendingIntent != null)
            return pendingIntent;
        Intent intent = new Intent(this, GeofencingIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private List createGeofenceList() {
        List<Geofence> geofenceList = new ArrayList<Geofence>();
        geofenceList.add(new Geofence.Builder()
                .setRequestId("GeofenceLocation")
                .setCircularRegion(
                        35.712142, //Latitude
                        51.4153748, //Longitude
                        100)
                .setLoiteringDelay(30000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_DWELL).build());
        return geofenceList;
    }

    private GeofencingRequest createGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(createGeofenceList());
        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
