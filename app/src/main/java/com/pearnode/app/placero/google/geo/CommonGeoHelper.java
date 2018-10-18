package com.pearnode.app.placero.google.geo;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;

import java.util.List;
import java.util.Locale;

import com.pearnode.app.placero.area.model.Address;

/**
 * Created by USER on 11/8/2017.
 */
public class CommonGeoHelper {

    public static final CommonGeoHelper INSTANCE = new CommonGeoHelper();

    private CommonGeoHelper() {
    }

    public Address getAddressByGeoLocation(Context context, Double lat, Double lon) {
        Address areaAddress = new Address();
        try {
            Location areaLocation = new Location("");
            areaLocation.setLatitude(lat);
            areaLocation.setLongitude(lon);

            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
            List<android.location.Address> addresses = geocoder.getFromLocation(areaLocation.getLatitude(), areaLocation.getLongitude(), 1);
            for (int i = 0; i < addresses.size(); i++) {
                android.location.Address address = addresses.get(i);
                areaAddress.setAdminArea(address.getAdminArea());
                areaAddress.setSubAdminArea(address.getSubAdminArea());
                areaAddress.setCountry(address.getCountryName());
                areaAddress.setFeatureName(address.getFeatureName());
                areaAddress.setLocality(address.getLocality());
                areaAddress.setSubLocality(address.getSubLocality());
                areaAddress.setPostalCode(address.getPostalCode());
                areaAddress.setPremises(address.getPremises());
                areaAddress.setThoroughFare(address.getThoroughfare());
                areaAddress.setSubThoroughFare(address.getSubThoroughfare());
                break;
            }
        } catch (Exception e) {
            // Do nothing if fails.
        }
        return areaAddress;
    }
}
