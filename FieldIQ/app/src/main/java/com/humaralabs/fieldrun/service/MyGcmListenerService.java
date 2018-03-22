/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.humaralabs.fieldrun.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.SplashActivity;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    TinyDB tinydb;// = new TinyDB(MyGcmListenerService.this);
    DbAdapter db;// = new DbAdapter(MyGcmListenerService.this);
    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String type = data.getString("type");
        //Log.d(TAG, "From: " + from);
        //Log.d(TAG, "Message: " + message);

        if (db == null)
            db = new DbAdapter(MyGcmListenerService.this);

        if (tinydb == null)
            tinydb = new TinyDB(MyGcmListenerService.this);

        db.insertNewNotification(message);

        if(type.equals("Trip") || type.equals("Task")) {
            tinydb.putString("GcmTripMessage", "YES");
            db.insertNewPerNotification(message);
        }

        showNotification(MyGcmListenerService.this, message);
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    /*private void sendNotification(String message) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 *//* ID of notification *//*, notificationBuilder.build());
    }*/

    private static int NOTIFICATION_ID = 41123427;
    //this method notify a notification if gps is disabled and when user clicks on this notification then it will take it to gps settings page
    public static void showNotification(final Context context,String message) {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(context, SplashActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            android.support.v4.app.NotificationCompat.Builder builder = new android.support.v4.app.NotificationCompat.Builder(context);
            Notification n = builder.setContentTitle("FieldRun")
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