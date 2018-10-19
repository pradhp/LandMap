package com.pearnode.app.placero.provider;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.pearnode.app.placero.custom.LocationPositionReceiver;
import com.pearnode.app.placero.position.Position;

/**
 * Created by USER on 10/17/2017.
 */
public class FusedLocationProvider {

    private final Activity mContext;

    public FusedLocationProvider(Activity context) {
        mContext = context;
    }

    public void getLocation() {

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);

        LocationManager manager = (LocationManager) this.mContext.getSystemService(Context.LOCATION_SERVICE);
        final Position pe = new Position();

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                pe.setLng(location.getLongitude());
                pe.setLat(location.getLatitude());
                ((LocationPositionReceiver) FusedLocationProvider.this.mContext).receivedLocationPostion(pe);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        try {
            manager.requestSingleUpdate(criteria, listener, null);
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }
}
