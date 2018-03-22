package com.humaralabs.fieldrun;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;

import com.humaralabs.fieldrun.service.EventUpdateService;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class CommonFunctions {
    private static final String TAG = "common";
    public static Context con=null;
    public static String _messageToShow="";
    public static int NOTIFICATION_ID=987654327;
    public static void sendMessageToActivity(int msg,Context c)
    {
        con=c;
        Message m = new Message();
        m.what = msg;
        updateHandler.sendMessage(m);
    }

    private static final int _SHOWMESSAGE = 1;
    public static Handler updateHandler = new Handler(){

        // @Override
        public void handleMessage(Message msg) {

            int event = msg.what;
            switch(event){
                case _SHOWMESSAGE:
                {
                    ShowMessageToUser(_messageToShow);
                    break;
                }
            }//end of switch
        }
    }; //end of updateHandler

    //this method is used to show alert message popup
    public static void ShowMessageToUser(String msg)
    {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(con);
        builder1.setTitle("Message");

        builder1.setMessage(msg);

        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        builder1.show();
    }

    //this method is used to get current datetime of system
    public static String GetCurrentDAteTime(){
        Calendar cal1 = Calendar.getInstance(); // creates calendar
        cal1.setTime(new Date()); // sets calendar time/date
        Date b= cal1.getTime();
        SimpleDateFormat foramtter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String dateStart = foramtter.format(b);
        return dateStart;
    }

    //this method is used to get current system date
    public static String GetCurrentDate(){
        Calendar cal1 = Calendar.getInstance(); // creates calendar
        cal1.setTime(new Date()); // sets calendar time/date
        Date b= cal1.getTime();
        SimpleDateFormat foramtter = new SimpleDateFormat("yyyy-MM-dd");
        String dateStart = foramtter.format(b);
        return dateStart;
    }

    //this method is used to get current datetime of system in long format
    public static long getCurrentTime() {
        Time now = new Time();
        now.setToNow();
        return now.toMillis(false);
    }

    //this method is used to show active trip notification on notification bar
    public static  void showActiveNotification(Context c,String TextToDisplay) {

        NotificationManager notificationManager = (NotificationManager) c.getSystemService(c.NOTIFICATION_SERVICE);
        Intent intent = new Intent(c, SplashActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(c,
                NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
        Notification n = builder.setContentIntent(pendingIntent)
                .setContentTitle("FieldRun")
                .setContentIntent(pendingIntent)
                .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(TextToDisplay))
                .setSmallIcon(R.drawable.outerlogo)
                .setContentText(TextToDisplay).setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.outerlogo)).build();
        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFICATION_ID, n);
    }

    //show basic message notification on notification bar
    public static void showNotification(Context c,String message){
        try {
            Random r = new Random();

            NotificationManager notificationManager = (NotificationManager) c.getSystemService(c.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
            Notification n = builder.setContentTitle("FieldRun")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setSmallIcon(R.drawable.outerlogo)
                    .setAutoCancel(true)
                    .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(message))
                    .setSmallIcon(R.drawable.outerlogo)
                    .setContentText(message).setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.outerlogo)).build();
            try {
                n.defaults |= Notification.DEFAULT_VIBRATE;
                n.defaults |= Notification.DEFAULT_SOUND;

                n.defaults |= Notification.FLAG_AUTO_CANCEL;
            } catch (Exception e) {
                Log.d(TAG, "error in showing notification push");
            }
            notificationManager.notify(r.nextInt(), n);
        }
        catch(Exception e){
            Log.d(TAG, "error in showing notification push");
        }
    }

    //this method convert image bitmap to string
    public static String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    //this method convert string to image bitmap
    public static Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    //start event update backround service
    public static void StartSevice(Context context){
        if(EventUpdateService.isRunning()==false) {
            EventUpdateService.callingServerApiForEvent=false;
            EventUpdateService.callingServerApiForTask=false;
            Intent intent = new Intent(context, EventUpdateService.class);
            context.startService(intent);
        }
    }

}
