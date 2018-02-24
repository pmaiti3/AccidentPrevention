package com.android.metacrazie.accidentprevention;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.metacrazie.accidentprevention.R;
import com.google.android.gms.gcm.GcmListenerService;

import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

/**
 * Created by metacrazie on 23/2/18.
 */

public class IntentService extends GcmListenerService {
    private static String TAG = IntentService.class.getSimpleName();
    private static String message;
    private NotificationHelper notificationHelper;
    CountDownTimer waitTimer;
    Timer timer=new Timer();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
        public void onMessageReceived(String from, Bundle data) {
            message = data.getString("message");
            Log.d(TAG, "From: " + from);
            Log.d(TAG, "Message: " + message);
            postNotification();
            startService();
        }
    private void startService()
    {
        timer.scheduleAtFixedRate(new mainTask(), 0, 5000);
        Log.d(TAG, "TIMER STARTED");
    }

    private class mainTask extends TimerTask
    {
        public void run()
        {
            toastHandler.sendEmptyMessage(0);
        }
    }
    private final Handler toastHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
        }
    };
    public void onDestroy()
    {
        super.onDestroy();
        Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
    }


    public void startTimer(){
        waitTimer = new CountDownTimer(10000, 300) {

            public void onTick(long millisUntilFinished) {
                //called every 300 milliseconds, which could be used to
                //send messages or some other action
                Log.d(TAG, String.valueOf(millisUntilFinished));
            }

            public void onFinish() {
                Intent intent = new Intent(IntentService.this, GetLocation.class);
                startActivity(intent);
            }
        }.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void postNotification() {

        //Intent snoozeIntent = new Intent(this, MyBroadcastReceiver.class);
        //snoozeIntent.setAction(ACTION_SNOOZE);
        //snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
        //PendingIntent snoozePendingIntent =
                //PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);

        Intent intentAction = new Intent(getApplicationContext(),ActionReceiver.class);

        //This is optional if you have more than one buttons and want to differentiate between two
        intentAction.putExtra("action","actionName");

        PendingIntent pIntentlogin = PendingIntent.getBroadcast(getApplicationContext(),1,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);

        notificationHelper = new NotificationHelper(this);
        Notification.Builder notificationBuilder = null;
        notificationBuilder = notificationHelper.getNotification1(getString(R.string.dialog_title),
                getString(R.string.dialog_message))
                .setAutoCancel(true)
                .addAction(R.drawable.ic_cancel_black_24dp, "Turn OFF alert", pIntentlogin);
        notificationBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;

        if (notificationBuilder != null) {
            notificationHelper.notify(0, notificationBuilder);
        }
    }

}
