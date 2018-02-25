package com.android.metacrazie.accidentprevention;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.metacrazie.accidentprevention.Util.LocationHelper;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by metacrazie on 24/2/18.
 */

public class ActionReceiver extends BroadcastReceiver {
    private static String TAG = ActionReceiver.class.getSimpleName();
    private String latitude;
    private String longitude;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationHelper locationHelper;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();
        mLastLocation = GetLocation.getLocation();
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        Log.d(TAG, String.valueOf(location));
        String action=intent.getStringExtra("action");
        getData();
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
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
            RequestQueue queue = Volley.newRequestQueue(mContext);
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
}