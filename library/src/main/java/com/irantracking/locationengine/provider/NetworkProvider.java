package com.irantracking.locationengine.provider;

import android.content.Context;
import android.location.LocationManager;

import com.irantracking.locationengine.LocationChangeListener;


public class NetworkProvider extends AbstractDefaultProvider {
    private static final String TAG = "Network";
    private static final String PROVIDER = LocationManager.NETWORK_PROVIDER;

    public NetworkProvider(Context context, long minTimeInterval, LocationChangeListener locationChangeListener) {
        super(PROVIDER, TAG, context, minTimeInterval, locationChangeListener);
    }
}
