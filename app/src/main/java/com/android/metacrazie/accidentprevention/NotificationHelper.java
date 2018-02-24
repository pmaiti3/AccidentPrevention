package com.android.metacrazie.accidentprevention;

/**
 * Created by metacrazie on 24/2/18.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Color;
import android.content.Context;
import android.content.ContextWrapper;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

class NotificationHelper extends ContextWrapper {
    private NotificationManager notifManager;
    public static final String CHANNEL_ONE_ID = "com.android.metacrazie.accidentprevention.ONE";
    public static final String CHANNEL_ONE_NAME = "Channel One";
    public static final String CHANNEL_TWO_ID = "com.android.metacrazie.accidentprevention.TWO";
    public static final String CHANNEL_TWO_NAME = "Channel Two";

//Create your notification channels//

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context base) {
        super(base);
        createChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createChannels() {

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, notifManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(notificationChannel);

        NotificationChannel notificationChannel2 = new NotificationChannel(CHANNEL_TWO_ID,
                CHANNEL_TWO_NAME, notifManager.IMPORTANCE_DEFAULT);
        notificationChannel2.enableLights(false);
        notificationChannel2.enableVibration(true);
        notificationChannel2.setLightColor(Color.RED);
        notificationChannel2.setShowBadge(false);
        getManager().createNotificationChannel(notificationChannel2);

    }

//Create the notification that’ll be posted to Channel One//

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification1(String title, String body) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ONE_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_priority_high_black_24dp)
                .setAutoCancel(true);
    }

//Create the notification that’ll be posted to Channel Two//

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification2(String title, String body) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_TWO_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_priority_high_black_24dp)
                .setAutoCancel(true);
    }


    public void notify(int id, Notification.Builder notification) {
        getManager().notify(id, notification.build());
    }

//Send your notifications to the NotificationManager system service//

    private NotificationManager getManager() {
        if (notifManager == null) {
            notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notifManager;
    }
}