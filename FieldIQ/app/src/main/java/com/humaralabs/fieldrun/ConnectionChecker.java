package com.humaralabs.fieldrun;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

public class ConnectionChecker {

    private Context _context;

    public ConnectionChecker(Context context){
        this._context = context;
    }

    //this method is checking internet conneted or not
    public static Boolean isConnectingToInternet(final Context context,Boolean ShowNotificationOrnot){
        Boolean activeOrNot=false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected())
        {
            activeOrNot=true;
        }
        else
        {
            activeOrNot=false;
            if(ShowNotificationOrnot)
                showNotification(context);
        }
        return activeOrNot;
    }

    private static int NOTIFICATION_ID = 32455546;
    //this method notify a notification if internet is disabled and when user clicks on this notification then it will take it to internet settings page
    public static void showNotification(final Context context) {
        try {
            String message = "Please Enable Your Internet";
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);

            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            android.support.v4.app.NotificationCompat.Builder builder = new android.support.v4.app.NotificationCompat.Builder(context);
            Notification n = builder.setContentIntent(pendingIntent)
                    .setContentTitle("FieldRun")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(message))
                    .setSmallIcon(R.drawable.outerlogo)
                    .setContentText(message).setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.outerlogo)).build();
            // n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
            n.defaults |= Notification.DEFAULT_VIBRATE;
            n.defaults |= Notification.DEFAULT_SOUND;
            notificationManager.notify(NOTIFICATION_ID, n);
        } catch (Exception e) {
            //Log.d(TAG, "error in showing notification push");
        }
    }
}