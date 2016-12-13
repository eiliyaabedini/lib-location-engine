package com.irantracking.locationengine.provider;

import android.location.Location;

public interface ILocationProvider {
    Location getLastLocation();

    void requestLocationUpdate();

    void removeLocationUpdate();
}
