package com.humaralabs.fieldrun;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

public class GPSChecker {

    private static final String TAG = "GPSCHECK";
    public static boolean alertVisibleOrnot=false;

    //this method check gps is enabled or not
    public static void GPSCheck(final Context context,Boolean notification) {
        final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );

        boolean enabled = true;
        String message = "Your GPS seems to be disabled, do you want to enable it?";
        if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
            enabled = false;
        }

        if (enabled && !isHighAccuracy(context)) {
            enabled = false;
            message = "Your GPS is not in high accuracy mode, do you want to change it?";
        }

        if (!enabled) {

            if(notification){
                showNotification(context);
            }
            else if(notification==false && alertVisibleOrnot==false) {
                alertVisibleOrnot=true;
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                alertVisibleOrnot = false;
                                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }

    }

    //this method checks high accuracy mode enabled or not
    public static boolean isHighAccuracy(final Context context) {
        int locationMode = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Log.d(TAG, "KITKAT CHECK");
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(),Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                Log.e(TAG, "EROR in gps check settings!", e);
            }

            return (locationMode != Settings.Secure.LOCATION_MODE_OFF && locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY); //check location mode

        }else{
            Log.d(TAG, "OLD CHECK");
            String locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private static int NOTIFICATION_ID = 4155546;
    //this method notify a notification if gps is disabled and when user clicks on this notification then it will take it to gps settings page
    public static void showNotification(final Context context) {
        try {

            String message = "Your GPS seems to be disabled, Please enable Your GPS!";
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

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
            Log.d(TAG, "error in showing notification push");
        }
    }
}
