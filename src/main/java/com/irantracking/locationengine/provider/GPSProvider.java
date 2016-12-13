package com.irantracking.locationengine.provider;

import android.content.Context;
import android.location.LocationManager;

import com.irantracking.locationengine.LocationChangeListener;

public class GPSProvider extends AbstractDefaultProvider {
    private static final String TAG = "GPS";
    private static final String PROVIDER = LocationManager.GPS_PROVIDER;

    public GPSProvider(Context context, long minTimeInterval, LocationChangeListener locationChangeListener) {
        super(PROVIDER, TAG, context, minTimeInterval, locationChangeListener);
    }
}
