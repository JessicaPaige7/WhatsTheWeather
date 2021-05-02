package com.j_nel.whatstheweather;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

class LocationFinder extends Service implements LocationListener {
    //Variables
    double dbLatitude, dbLongitude;
    Location location;
    private final Context context;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canUserGetLocation = false;
    protected LocationManager locationManager;
    private static final long MINIMUM_DISTANCE_FOR_UPDATES = 10;
    private static final long MINIMUM_TIME = 1000 * 60 * 1;

    //Constructor
    public LocationFinder(Context Context) {
        context = Context;
        getUserLocation();
    }

    public Location getUserLocation() {
        //Retrieve user's location by check if their services are enabled.
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled && isNetworkEnabled) {
                this.canUserGetLocation = true;
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MINIMUM_TIME, MINIMUM_DISTANCE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            dbLatitude = location.getLatitude();
                            dbLongitude = location.getLongitude();
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                        }
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME, MINIMUM_DISTANCE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                dbLatitude = location.getLatitude();
                                dbLongitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception E) {
            E.printStackTrace();
        }
        return location;
    }

    public double getUserLatitude() {
        if (location != null) {
            dbLatitude = location.getLatitude();
        }
        return dbLatitude;
    }

    public double getUserLongitude() {
        if (location != null) {
            dbLongitude = location.getLongitude();
        }
        return dbLongitude;
    }

    public boolean canUserGetLocation() {
        return this.canUserGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Location settings");
        alertDialog.setMessage("Mobile location settings are not enabled. Would you like to go to the settings menu?");
        alertDialog.setPositiveButton("Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
        });
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location Location) {
    }

    @Override
    public void onProviderDisabled(String Provider) {
    }

    @Override
    public void onProviderEnabled(String Provider) {
    }

    @Override
    public void onStatusChanged(String Provider, int Status, Bundle Extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}