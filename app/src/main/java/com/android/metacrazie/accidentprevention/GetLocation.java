package com.android.metacrazie.accidentprevention;

import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.metacrazie.accidentprevention.Util.LocationHelper;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by metacrazie on 24/2/18.
 */

public class GetLocation extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback, com.google.android.gms.location.LocationListener {

    private static String TAG = GetLocation.class.getSimpleName();
    private String latitude;
    private String longitude;
    private static Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        locationHelper = new LocationHelper(this);
        locationHelper.checkpermission();
        getData();
    }

    public void getData() {

        final String BASE_URL =
                "http://192.168.0.5:5000/secondscreening?";
        final String USER_ID = "userid";
        final String GPS_PARAM = "gps";
        URL url_link;
        String url = "";

        latitude = "22.496299";
        longitude = "88.371931";
        RequestQueue queue = Volley.newRequestQueue(this);
        //String url ="http://192.168.0.9:5000/secondscreening?userid=1234&gps=[22.496299,88.371931]";
        String gps = "[" + latitude + "," + longitude + "]";
        Log.d("GET DATA", gps);

        try {
            Uri builtUri = Uri.parse(BASE_URL)
                    .buildUpon()
                    .appendQueryParameter(USER_ID, "1234")
                    .appendQueryParameter(GPS_PARAM, gps)
                    .build();

            url_link = new URL(builtUri.toString());
            url = builtUri.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.d("URL", url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(TAG, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, String.valueOf(error));
            }
        });
        queue.add(stringRequest);
    }

    public static Location getLocation(){

        return mLastLocation;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        locationHelper.checkPlayServices();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            mLastLocation = location;
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            Log.d(TAG, String.valueOf(location));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        locationHelper.connectApiClient();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("Connection failed:", " ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, String.valueOf(location));
        mLastLocation = location;
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        Log.d("Location:", location.toString());
        mLastLocation = location;
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
    }


}
