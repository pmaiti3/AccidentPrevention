package com.android.metacrazie.accidentprevention;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by metacrazie on 23/2/18.
 */

public class IDListenerService extends GcmListenerService {

    private static String TAG = IDListenerService.class.getSimpleName();
        @Override
        public void onMessageReceived(String from, Bundle data) {
            String message = data.getString("message");
            Log.d(TAG, "From: " + from);
            Log.d(TAG, "Message: " + message);

        }

}
