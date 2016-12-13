package com.irantracking.locationengine.base;

import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class LocationHelper {
    public static final int ZONE_DISTANCE_FROM_CENTER_OF_TEHRAN = 23785;
    public static final double CENTER_OF_TEHRAN_LATITUDE = 35.7453976;
    public static final double CENTER_OF_TEHRAN_LONGITUDE = 51.3600540;
    private static Location centerOfTehran = getCenterOfTehran();

    /**
     * Check min accuracy and minElapsedTime of location
     *
     * @param location
     * @param minAccuracy
     * @param minElapsedTime
     * @return
     */
    public static boolean isLocationAccurate(Location location, int minAccuracy, long minElapsedTime) {
        long elapsedTimeInSeconds = -1;
        boolean accurate = location != null; // Check not be out of date
        if (location != null) {
            elapsedTimeInSeconds = getElapsedTimeSecond(location);
            accurate = location.hasAccuracy() && location.getAccuracy() < minAccuracy && location.getAccuracy() != 0.0 // Check accuracy
                    && elapsedTimeInSeconds < minElapsedTime;
        }
        if (!accurate)
            Log.i("Location", "Location is not accurate. Detail:"
                    + (location == null ? " location is null." : " accuracy=" + location.getAccuracy() + " elapsedTime=" + elapsedTimeInSeconds + "s"));
        return accurate;
    }

    /**
     * Calculate Location elapsedTime in second
     *
     * @param location
     * @return elapsedTime in second
     */
    public static long getElapsedTimeSecond(Location location) {
        long elapsedTimeInSeconds = -1;
        if (Build.VERSION.SDK_INT > 16) {
            if (location != null) {
                elapsedTimeInSeconds = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / 1000000000;
            }
        }
        return elapsedTimeInSeconds;
    }

    /**
     * Check if elapsedTime of location is below than freshTimeSecond then its fresh location
     *
     * @param location
     * @return
     */
    public static boolean isLocationFresh(Location location) {
        long freshTimeSecond = 60;
        return getElapsedTimeSecond(location) < freshTimeSecond;
    }

    public static Location giveBestLocationOwnMethod(Location lastKnownLocation, Location newLocation, boolean forceAccurateLocation, long minAccuracy, long minValidElapsedTimeSecond) {
        Log.d("BestLocation", "-----------------------------------------");
        Log.d("BestLocation", "giveBestLocation:");
        if (lastKnownLocation == null)  // Check if last is null then we need newLocation
        {
            Log.d("BestLocation", "Returned Location is ---> NewLocation");
            return newLocation;
        }

        Log.d("BestLocation", "LastKnowLocation:        "
                + " Location accuracy:" + lastKnownLocation.getAccuracy()
                + " Location elapsedRealtimeNanos:" + lastKnownLocation.getElapsedRealtimeNanos()
        );
        Log.d("BestLocation", "NewLocation:             "
                + " Location accuracy:" + newLocation.getAccuracy()
                + " Location elapsedRealtimeNanos:" + newLocation.getElapsedRealtimeNanos()
        );


        // Check if newLocation is for newer time
        if (lastKnownLocation.getElapsedRealtimeNanos() < newLocation.getElapsedRealtimeNanos()) {
            Log.d("BestLocation", "New Location is newer");

            // If we don't need to force location then we can send back newLocation
            if (!forceAccurateLocation) {
                Log.d("BestLocation", "Returned Location is ---> NewLocation");
                return newLocation;
            } else {

                // Check for newLocation have minimum valid accuracy
                if (newLocation.getAccuracy() <= minAccuracy) {
                    Log.d("BestLocation", "Have minimum Accuracy : x < " + minAccuracy);

                    // Check for ElapsedTimes Delta
                    long elapsedTimeDeltaSecond = ((newLocation.getElapsedRealtimeNanos() - lastKnownLocation.getElapsedRealtimeNanos()) / 1000000000);

                    Log.d("BestLocation", "Delta with lastLocation:" + elapsedTimeDeltaSecond);

                    // If Delta is longer than minimum elapsed defined so return newLocation else return accurate location
                    if (elapsedTimeDeltaSecond > minValidElapsedTimeSecond) {
                        Log.d("BestLocation", "Delta is more than valid ElapsedTime: " + elapsedTimeDeltaSecond + " > " + minValidElapsedTimeSecond);

                        // Return newer location
                        Log.d("BestLocation", "Returned Location is ---> NewLocation");
                        return newLocation;
                    } else {
                        Log.d("BestLocation", "Delta is less than valid ElapsedTime, more Accurate is important: " + elapsedTimeDeltaSecond + " < " + minValidElapsedTimeSecond);
                        Log.d("BestLocation", "NewLocation Accuracy: " + newLocation.getAccuracy());
                        Log.d("BestLocation", "LastLocation Accuracy: " + lastKnownLocation.getAccuracy());

                        // Return more Accurate location
                        Log.d("BestLocation", "Returned Location is ---> " + (newLocation.getAccuracy() < lastKnownLocation.getAccuracy() ? "NewLocation" : "LastKnownLocation"));
                        return newLocation.getAccuracy() < lastKnownLocation.getAccuracy() ? newLocation : lastKnownLocation;
                    }
                } else {
                    Log.d("BestLocation", "Have minimum Accuracy : x > " + minAccuracy);

                    Log.d("BestLocation", "Returned Location is ---> LastKnownLocation");
                    return lastKnownLocation;
                }
            }
        }

        Log.d("BestLocation", "Returned Location is ---> LastKnownLocation");
        return lastKnownLocation;
    }

    public static int calculateDistance(Location end, Location start) {
        return (int) meterDistanceBetweenPoints(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());
    }

    public static int calculateDistance(LatLng end, LatLng start) {
        return (int) meterDistanceBetweenPoints(start.latitude, start.longitude, end.latitude, end.longitude);
    }

    public static double meterDistanceBetweenPoints(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = (180.f / Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    public static Location getCenterOfTehran() {
        Location centerOfTehran = new Location("");
        centerOfTehran.setLatitude(CENTER_OF_TEHRAN_LATITUDE);
        centerOfTehran.setLongitude(CENTER_OF_TEHRAN_LONGITUDE);
        return centerOfTehran;
    }

    public static boolean isOutOfZone(Location lastLocation) {
        if (lastLocation == null || (lastLocation.getLatitude() == 0 && lastLocation.getLongitude() == 0))
            return false;
        return calculateDistance(centerOfTehran, lastLocation) > ZONE_DISTANCE_FROM_CENTER_OF_TEHRAN;
    }

    public static boolean isValidPoint(Double XPoint, Double YPoint) {
        return XPoint != -1 && XPoint != 0 && YPoint != -1 && YPoint != 0;
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public static Location giveBestLocation(Location lastKnownLocation, Location newLocation) {
        if (isBetterLocation(newLocation, lastKnownLocation))
            return newLocation;
        return lastKnownLocation;
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            Log.d("BetterLocation", "A new location is always better than no location");
            return true;
        }

        if(location==null)
            return false;

        if(location.getAccuracy()>50) {
            // More than 50 meter accuracy is not good for us
            Log.d("BetterLocation", "More than 50 meter accuracy is not good for us");
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            Log.d("BetterLocation", "isSignificantlyNewer:" + timeDelta);
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            Log.d("BetterLocation", "isSignificantlyOlder:" + timeDelta);
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            Log.d("BetterLocation", "isMoreAccurate:" + accuracyDelta);
            return true;
        } else if (isNewer && !isLessAccurate) {
            Log.d("BetterLocation", "isNewer && !isLessAccurate:" + accuracyDelta);
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            Log.d("BetterLocation", "isNewer && !isSignificantlyLessAccurate && isFromSameProvider :" + accuracyDelta);
            return true;
        }

        Log.d("BetterLocation", "End of conditions");
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @NonNull
    public static Location createMockLocation() {
        Log.i(AbstractLocationEngine.TAG, "Location is null !!!");
        Location zeroLocation = new Location("Mock");
        zeroLocation.setLatitude(CENTER_OF_TEHRAN_LATITUDE);
        zeroLocation.setLongitude(CENTER_OF_TEHRAN_LONGITUDE);
        return zeroLocation;
    }
}
