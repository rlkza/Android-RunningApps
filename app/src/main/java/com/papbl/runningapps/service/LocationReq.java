package com.papbl.runningapps.service;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by robi on 4/25/2019.
 */

public class LocationReq {
    public static LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
}
