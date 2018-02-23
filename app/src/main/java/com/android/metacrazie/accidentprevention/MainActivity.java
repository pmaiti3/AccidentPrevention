package com.android.metacrazie.accidentprevention;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.metacrazie.accidentprevention.Util.LocationHelper;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback, com.google.android.gms.location.LocationListener {

    private static EditText key_text;
    private static EditText data_text;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationHelper locationHelper;
    private static String TAG = MainActivity.class.getSimpleName();
    private String latitude;
    private String longitude;
    protected String SENDER_ID = "470083766349";
    private GoogleCloudMessaging gcm =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        getRegistrationID();



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
             return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation == null) {
            //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.d("Location: ", String.valueOf(mLastLocation));
        } else {
            //TODO: send data
            handleNewLocation(mLastLocation);
            Log.d(TAG, String.valueOf(mLastLocation));
        }

        key_text = (EditText) findViewById(R.id.edittext_key);
        data_text = (EditText) findViewById(R.id.edittext_data);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Send data", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //new SendRequest().execute();
                getData();
            }
        });
    }

        String regId = "";
        String msg = "";

    @SuppressLint("StaticFieldLeak")
    public void getRegistrationID() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object...params) {

                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                    }
                    regId = gcm.register(SENDER_ID);
                    Log.d("in async task", regId);

                    // try
                    msg = "Device registered, registration ID=" + regId;
                    Log.d("REGISTRATION ID", regId);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
        }.execute(null, null, null);
    }


    public void checkPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //Play Services is not installed/enabled
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());
            } else {
                //This device does not support Play Services
            }
        }
    }

    private void handleNewLocation(Location location) {
        Log.d("Location:", location.toString());
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        handleNewLocation(location);
    }

    public void getData(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.0.9:5000/secondscreening?userid=1234&gps=[22.496299,88.371931]";
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

    public class SendRequest extends AsyncTask<String, Void, String> {

        //TODO: get data
        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            Log.d("Background thread", "Send Req");
            try {

                URL url = new URL("http://192.168.0.9:5000/secondscreening?userid=1234&gps=22.496299,88.371931");
                Log.d(TAG, String.valueOf(url));

                JSONObject postDataParams = new JSONObject();

                //TODO: get GPS data

                String key = String.valueOf(key_text.getText());
                String value = String.valueOf(data_text.getText());

                //postDataParams.put("gps", "22.496299,88.371931");
                //postDataParams.put("userid", "1234");

                Log.e("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Log.d("OutputStream", "tag1");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                Log.d("OutputStream", "tag2");

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    Log.d("ResponseCodetag", "HTTP_OK");


                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    Log.d("Error", "false: " + responseCode);
                    return new String("false : " + responseCode);

                }
            } catch (Exception e) {
                Log.d("Exception", "Exception: " + e.getMessage());
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();

        }
    }

    public static String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }


}
