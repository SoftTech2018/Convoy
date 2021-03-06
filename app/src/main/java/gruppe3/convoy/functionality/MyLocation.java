package gruppe3.convoy.functionality;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

/*
 Klassen er udviklet af Jon Tvermose Nielsen
 */
public class MyLocation implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation, mLastLocation;
    private static final long INTERVAL = 1000 * 10; // Update interval i ms = 10 sek
    private static final long FASTEST_INTERVAL = 1000 * 5; // Hurtigste update interval i ms
    private String mLastUpdateTime;
    private boolean mRequestingLocationUpdates = false;
    public static boolean POSUPDATED = false;
    private final String TAG = "STEDBESTEMMELSE";

    public MyLocation(){
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "**** MyLocation.onConnected()");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation==null){
            mLastLocation = new Location("");// TO DO - hvis der ikke kan findes en sidst kendt lokation
            mLastLocation.setLatitude(55);
            mLastLocation.setLongitude(12);
        }

        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if(mGoogleApiClient.isConnected()){
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.d(TAG, "Stedbestemmelse startet");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Lokation opdateret: " + location.getLatitude() + " : " + location.getLongitude());
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        if(!POSUPDATED){
            POSUPDATED = true;
            SingleTon.fetchData();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if(!SingleTon.powerSaving){
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } else {
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
    }

    /**
     * Denne metoder starter stedbestemmelse i appen
     */
    public void startLocationService(Context context){
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    public void onResume() {
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    public void stopLocationUpdates() {
        Log.d(TAG, "Stedbestemmelse stoppet");
        mRequestingLocationUpdates = false;
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public Location getLocation(){
        if(mCurrentLocation==null){
            if(mLastLocation==null){
                mLastLocation = new Location("");
                // TO DO - hvis der ikke kan findes en sidst kendt lokation
                mLastLocation.setLatitude(55);
                mLastLocation.setLongitude(12);
            }
            return mLastLocation;
        } else {
            return mCurrentLocation;
        }
    }
}
