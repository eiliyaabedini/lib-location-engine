package com.irantracking.locationengine.base;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;


import com.irantracking.locationengine.LocationChangeListener;
import com.irantracking.locationengine.provider.ILocationProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLocationEngine implements LocationChangeListener {
    public static final String TAG = "LocationEngine";

    public static final int SECOND = 1000;


    private Location lastKnownLocation;

    private final List<LocationChangeListener> listeners = new ArrayList<>();
    private List<ILocationProvider> providers;


    public AbstractLocationEngine(Context context) {
        providers = defineProviders(context);
    }

    /**
     * Providers must return in array and each one must be an ILocationProvider
     * @param context
     * @return
     */
    @NonNull
    protected abstract List<ILocationProvider> defineProviders(Context context);

    public Location getLastLocation() {
        Log.i(TAG, "getLastLocation()");

        if (lastKnownLocation == null || useCachedLocationForLastLocation()) {
            for (ILocationProvider provider : providers) {
                Location newLastLocation = provider.getLastLocation();
                if (newLastLocation != null)
                    lastKnownLocation = LocationHelper.giveBestLocation(lastKnownLocation, newLastLocation);
            }
        }


        if (lastKnownLocation == null) return LocationHelper.createMockLocation();
        return lastKnownLocation;
    }

    protected abstract boolean useCachedLocationForLastLocation();

    public Location getLastBestProviderLocation() {
        for (ILocationProvider provider : providers) {
            Location newLastLocation = provider.getLastLocation();
            if (newLastLocation != null)
                lastKnownLocation = LocationHelper.giveBestLocation(lastKnownLocation, newLastLocation);
        }

        if (lastKnownLocation == null) return LocationHelper.createMockLocation();
        return lastKnownLocation;
    }

    @Override
    public synchronized void onLocationChanged(Location location) {
        logLocationDetail("New Location: ", location);

        // Stop requesting for update if there isn't any listener
        Log.d(TAG, "Location listeners: " + listeners.size());
        if (listeners.size() == 0) {
            removeLocationUpdate();
            return;
        }

        // Check for best location from last and new
        Location bestLocation = LocationHelper.giveBestLocation(lastKnownLocation, location);

        if (bestLocation.equals(lastKnownLocation)) {
            logLocationDetail("Last location is better", lastKnownLocation);

        } else {
            lastKnownLocation = bestLocation;

            logLocationDetail("New location is better", lastKnownLocation);

            notifyForNewAccurateLocation(lastKnownLocation);
        }
    }

    /**
     * Notify registered classes for new location update
     *
     * @param location
     */
    private void notifyForNewAccurateLocation(Location location) {

        onNewAccurateLocation(location);

        synchronized (listeners) {
            // Invoke listeners
            for (int i = 0; i < listeners.size(); i++) {
                LocationChangeListener locationChangeListener = listeners.get(i);
                if (locationChangeListener != null)
                    locationChangeListener.onLocationChanged(location);
            }
        }
    }

    protected abstract void onNewAccurateLocation(Location location);

    private void logLocationDetail(String title, Location location) {
        Log.d(TAG, title +
                " ; Provider:" + location.getProvider() +
                " ; Accuracy:" + location.getAccuracy() +
                " ; Time:" + location.getTime() +
                " ; Speed:" + location.getSpeed() +
                " ; Altitude:" + location.getAltitude() +
                " ; Bearing:" + location.getBearing()
        );
    }

    /**
     * Request location updates from providers
     */
    private void requestLocationUpdate() {
        for (ILocationProvider provider : providers) {
            provider.requestLocationUpdate();
        }
    }

    /**
     * Stop providers from getting location
     */
    private void removeLocationUpdate() {
        for (ILocationProvider provider : providers) {
            provider.removeLocationUpdate();
        }
    }

    public void addLocationChangeListener(LocationChangeListener listener) {
        if (!listeners.contains(listener) && listeners.add(listener))
            requestLocationUpdate();
    }

    public void removeLocationChangeListener(LocationChangeListener listener) {
        listeners.remove(listener);
        if (listeners.size() == 0)
            removeLocationUpdate();
    }

}
