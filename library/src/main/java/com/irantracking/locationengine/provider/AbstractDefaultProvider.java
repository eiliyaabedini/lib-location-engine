package com.irantracking.locationengine.provider;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.irantracking.locationengine.LocationChangeListener;
import com.irantracking.locationengine.base.LocationHelper;

public class AbstractDefaultProvider implements LocationListener, ILocationProvider {
    private String TAG;
    private String PROVIDER;

    private Location lastKnownLocation;
    private boolean lastlyRequested;

    private LocationManager locationManager;
    private boolean isProviderEnabled;

    private final Context context;
    private long minTimeInterval;
    private LocationChangeListener locationChangeListener;


    public AbstractDefaultProvider(String provider, String tag, Context context, long minTimeInterval, LocationChangeListener locationChangeListener) {
        this.TAG = "Location-" + tag;
        this.PROVIDER = provider;
        this.context = context;

        this.minTimeInterval = minTimeInterval;
        this.locationChangeListener = locationChangeListener;
        connectToLocationManager(context);

    }

    private void connectToLocationManager(Context context) {
        Log.d(TAG, "connectToLocationManager");

        if (context == null)
            return;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // getting Provider status
        isProviderEnabled = locationManager.isProviderEnabled(PROVIDER);

        if (isProviderEnabled) {
            if (lastKnownLocation == null) {
                Log.d("Provider Enabled", "Provider Enabled");
                if (locationManager != null) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        lastKnownLocation = locationManager.getLastKnownLocation(PROVIDER);
                }
            }
        }
    }


    @Override
    public Location getLastLocation() {
        Log.d(TAG, "getLastLocation()");

        Location managerLastKnownLocation = null;
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                managerLastKnownLocation = locationManager.getLastKnownLocation(PROVIDER);
        }


        lastKnownLocation = LocationHelper.giveBestLocation(lastKnownLocation, managerLastKnownLocation);

        return lastKnownLocation;
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged() :" + location);

        lastKnownLocation = location;

        locationChangeListener.onLocationChanged(lastKnownLocation);
    }

    /**
     * At least one update will happen
     */
    @Override
    public void requestLocationUpdate() {
        Log.d(TAG, "requestLocationUpdate()");

        if (!lastlyRequested && locationManager != null) {
            Log.d(TAG, "makeRequest()");
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(
                        PROVIDER,
                        minTimeInterval,
                        0f, this);
                lastlyRequested = true;
            }
        }
    }

    @Override
    public void removeLocationUpdate() {
        Log.d(TAG, "removeLocationUpdate()");

        if (lastlyRequested && locationManager != null) {
            Log.d(TAG, "removeUpdates()");
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(this);
                lastlyRequested = false;
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        isProviderEnabled = true;
    }

    @Override
    public void onProviderDisabled(String s) {
        isProviderEnabled = false;
    }
}
