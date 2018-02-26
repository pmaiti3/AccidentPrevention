package com.android.metacrazie.accidentprevention;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback, com.google.android.gms.location.LocationListener {

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationHelper locationHelper;
    private static String TAG = MainActivity.class.getSimpleName();
    private String latitude;
    private String longitude;
    protected String SENDER_ID = "470083766349";
    private GoogleCloudMessaging gcm = null;
    private NotificationHelper notificationHelper;
    CountDownTimer waitTimer;
    Button btn;
    TextView timer_txt;

    @RequiresApi(api = Build.VERSION_CODES.O)
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
        notificationHelper = new NotificationHelper(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        /*
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation == null) {
            //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            //mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.d("Location: ", String.valueOf(mLastLocation));
        } else {
            //TODO: send data
            handleNewLocation(mLastLocation);
            Log.d(TAG, String.valueOf(mLastLocation));
        }
        */


        startTimer();



        timer_txt = (TextView) findViewById(R.id.time_text);
        timer_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waitTimer.cancel();
                timer_txt.setText("FALSE ALARM!");
            }
        });

        btn = (Button) findViewById(R.id.btn_accident);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                waitTimer.cancel();
                timer_txt.setText("FALSE ALARM!");
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Send data", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //new SendRequest().execute();
                //getData();
                postNotification();
                startTimer();
            }
        });
    }

    String regId = "";
    String msg = "";

    public void startTimer() {
        waitTimer = new CountDownTimer(10000, 300) {
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

            public void onTick(long millisUntilFinished) {
                //called every 300 milliseconds, which could be used to
                //send messages or some other action
                Log.d(TAG, String.valueOf(millisUntilFinished));
                long seconds = millisUntilFinished/1000;
                String display = String.valueOf(seconds) + " s";
                timer_txt.setText(display);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
            }

            public void onFinish() {
                getData();
            }
        }.start();
    }


    @SuppressLint("StaticFieldLeak")
    public void getRegistrationID() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void postNotification() {

        //Intent snoozeIntent = new Intent(this, MyBroadcastReceiver.class);
        //snoozeIntent.setAction(ACTION_SNOOZE);
        //snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
        //PendingIntent snoozePendingIntent =
        //PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);

        Intent intentAction = new Intent(getApplicationContext(), ActionReceiver.class);

        //This is optional if you have more than one buttons and want to differentiate between two
        intentAction.putExtra("action", "actionName");

        PendingIntent pIntentlogin = PendingIntent.getBroadcast(getApplicationContext(), 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationHelper = new NotificationHelper(this);
        Notification.Builder notificationBuilder = null;
        notificationBuilder = notificationHelper.getNotification1(getString(R.string.dialog_title),
                getString(R.string.dialog_message))
                .addAction(R.drawable.ic_cancel_black_24dp, "Turn OFF alert", pIntentlogin);
        notificationBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;

        notificationHelper.notify(0, notificationBuilder);
    }


    public void getData() {

        final String BASE_URL =
                "http://192.168.0.35:9000/secondscreening?";
        final String USER_ID = "userid";
        final String GPS_PARAM = "gps";
        URL url_link;
        String url = "";

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

    public void checkPlayServices() {
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
        handleNewLocation(location);
    }

}
