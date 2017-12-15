package za.healthtracking.service;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;


/**
 * Created by hiepmt on 24/07/2017.
 */

public class LocationListenerWrapper implements LocationListener, GpsStatus.Listener {
    public LocationListener mLocationListener;
    private GpsStatus.Listener mListener;

    public LocationListenerWrapper(LocationListener locationListener, GpsStatus.Listener listener) {
        this.mLocationListener = locationListener;
        this.mListener = listener;
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (this.mListener != null) {
            this.mListener.onGpsStatusChanged(event);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (this.mLocationListener != null) {
            this.mLocationListener.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (this.mLocationListener != null) {
            this.mLocationListener.onStatusChanged(provider, status, extras);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (this.mLocationListener != null) {
            this.mLocationListener.onProviderEnabled(provider);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (this.mLocationListener != null) {
            this.mLocationListener.onProviderDisabled(provider);
        }
    }
}
