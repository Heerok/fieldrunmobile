package com.humaralabs.fieldrun.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.humaralabs.fieldrun.CommonFunctions;
import com.humaralabs.fieldrun.ConnectionChecker;
import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.GPSChecker;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.SplashActivity;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.server.ServerInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;


public class EventUpdateService extends Service implements LocationListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GPSS";
    public static Boolean callingServerApiForEvent=false;
    public static Boolean callingServerApiForTask=false;
    public static int maxUpdateCount=40;
    DbAdapter db;
    TinyDB tiny;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    public static Location mlocation=null;

    private static boolean running = false;


    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public EventUpdateService getService() {
            return EventUpdateService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db=new DbAdapter(EventUpdateService.this);
        tiny=new TinyDB(EventUpdateService.this);
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            Log.d(TAG, "google playsevice not thr");
        }
        createLocationRequest();
        ConnectToGoogleApiClient();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                running = true;
                Log.d(TAG, "Calling timer update");
                Log.d(TAG, "Checking gps is active or not");
                if (db == null)
                    db = new DbAdapter(EventUpdateService.this);
                if (tiny == null)
                    tiny = new TinyDB(EventUpdateService.this);
                final Message msg = new Message();
                new Thread() {
                    public void run() {
                        msg.arg1 = 1;
                        handler.sendMessage(msg);
                    }
                }.start();

                if (!mGoogleApiClient.isConnected()) {
                    ConnectToGoogleApiClient();
                }
            }
        }, 60 * 1000, 60 * 1000);

        //timer for task update
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                running = true;
                Log.d(TAG, "Calling timer update");
                Log.d(TAG, "Checking gps is active or not");
                if(db==null)
                    db=new DbAdapter(EventUpdateService.this);
                if(tiny==null)
                    tiny=new TinyDB(EventUpdateService.this);
                final Message msg = new Message();
                new Thread()
                {
                    public void run()
                    {
                        msg.arg1=2;
                        handler.sendMessage(msg);
                    }
                }.start();

                if(!mGoogleApiClient.isConnected()){
                    ConnectToGoogleApiClient();
                }
            }
        }, 60*1000, 60*1000);
        Log.d(TAG, "service up and running!");
        running = true;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
        if(msg.arg1==1)
        {
            String lastPerNotification=db.GetLastPerNotificationsFromDatabase();
            if(!lastPerNotification.equals("")){
                showNotification(EventUpdateService.this, lastPerNotification);
            }
            //checking any trip is active or not if avtive the go further
            long activetripid=db.getActiveTripId();
           // int activetask=db.GetCountForAllActiveTask();
            if(activetripid!=0) {
                //checking gps is active or not if not then show notification
                GPSChecker.GPSCheck(EventUpdateService.this, true);
                //checking internet is conneccted or not if not then show notification
                Boolean IntenetConected=ConnectionChecker.isConnectingToInternet(EventUpdateService.this,true);
                //here we are checking if already a server call happening so we will not call server again untill previous call complete and if internet connect then call the server
                if(callingServerApiForTask==false && IntenetConected==true) {
                    callingServerApiForTask=true;
                    new SendTaskEventUpdatesToServer().execute();
                }
            }
            else{
                new SendImageUploadEventsToServer().execute();
            }
        }
        else if(msg.arg1==2)
        {
            //checking gps is active or not if not then show notification
            //GPSChecker.GPSCheck(EventUpdateService.this, true);
            //checking internet is conneccted or not if not then show notification
            Boolean IntenetConected=ConnectionChecker.isConnectingToInternet(EventUpdateService.this,false);
            //here we are checking if already a server call happening so we will not call server again untill previous call complete and if internet connect then call the server
            if(callingServerApiForEvent==false && IntenetConected==true) {
                new SendGpsEventsUpdateToServer().execute();
                callingServerApiForEvent=true;
            }
        }
        return false;
        }
    });

    protected void ConnectToGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000 * 10);
        mLocationRequest.setFastestInterval(1000 * 10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        if(mGoogleApiClient.isConnected())
            startLocationUpdates();
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: "+LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
        mlocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "location update");
        if(db==null)
            db=new DbAdapter(EventUpdateService.this);

        if(location!=null) {
            mlocation = location;
            String loginToken=tiny.getString("loginToken");
            if(!loginToken.equals(""))
                db.insertFieldEvent("","","gpsevent", location,"",EventUpdateService.this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        callingServerApiForTask=false;
        callingServerApiForEvent=false;
        return START_STICKY;
    }

    protected void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }
    @Override
    public void onDestroy() {
        try {
            callingServerApiForEvent=false;
            callingServerApiForTask=false;
            running = false;

            Log.d(TAG, "Service::onDestroy, removing gps listener");
            db.insertFieldEvent("","","trackerstop", LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient), "", EventUpdateService.this);
            stopLocationUpdates();
            mGoogleApiClient.disconnect();

        }
        catch(Exception e){
            Log.d(TAG, "error on service destroy .......................");
        }
        try{
          //  Toast.makeText(EventUpdateService.this, "Please", Toast.LENGTH_LONG).show();
            startService(new Intent(this, EventUpdateService.class));
        }
        catch(Exception e){
            Log.d(TAG, "error on service destroy .......................");
        }
    }


    public static boolean isRunning() {
        return running;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        try{
            callingServerApiForEvent=false;
            callingServerApiForTask=false;
            System.out.println("Ontask remove service111");
            // Toast.makeText(EventUpdateService.this, "Please", Toast.LENGTH_LONG).show();
            Intent restartService = new Intent(getApplicationContext(),this.getClass());
            restartService.setPackage(getPackageName());
            startService(restartService);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            return false;
        }
    }

    //Async class for sending trip data to server
    private class SendGpsEventsUpdateToServer extends AsyncTask<Void, Void, Void>
    {
        protected void onPreExecute()
        {
            //do nothing
        }
        protected void onPostExecute(Void unused)
        {
            callingServerApiForEvent=false;
        }
        protected Void doInBackground(Void... par)
        {
            try{
                String url = Constants.ServerApiUrl + "mobile/GpsEventUpdate";
                if(tiny==null)
                    tiny=new TinyDB(EventUpdateService.this);
                String loginToken=tiny.getString("loginToken");
                JSONObject jo = db.getPendingEvents(maxUpdateCount,loginToken,"ALLGPS");
                JSONArray eventsUpdateArray=jo.getJSONArray("updates");
                if(eventsUpdateArray.length()>0) {
                    db.insertUpdatesCount("bgcallserverAttempt");
                    int requestId = Integer.parseInt(jo.getString("reqId"));
                    String iemino = tiny.getString("iemino");
                    String Resultdata = ServerInterface.CallServerApiBgForGPS(jo, url,loginToken,iemino);
                    if(ServerInterface.BGcheckserverforgps && !Resultdata.equals("")) {//server response properly
                        try {
                            maxUpdateCount=40;
                            JSONObject mydata = new JSONObject(Resultdata);
                            Log.d(TAG, "bulk sync response" + mydata.toString());
                            String res = mydata.getString("Result");
                            Boolean status = mydata.getBoolean("status");
                            if (status && res.equals("OK")) {
                                JSONObject record = mydata.getJSONObject("Record");
                                //add new task if assigned i a running trip
                                //AddNewTaskToTrip(record);

                                String respid = record.getString("reqid");
                                //deleting updated record
                                db.deleteUpdatedEvents(Integer.parseInt(respid), "NOTALL");
                                db.insertUpdatesCount("bgcallserverSuccess");
                            } else {
                                if (requestId != 0)
                                    db.updateEventStatus(Constants.event_status_pending_code, requestId);

                                db.insertUpdatesCount("bgcallserverFail");
                            }
                        } catch (Exception e) {
                            if(requestId!=0)
                                db.updateEventStatus(Constants.event_status_pending_code,requestId);

                            db.insertUpdatesCount("bgcallserverFail");
                            e.printStackTrace();
                        }
                    }
                    else {//response blank case
                        if(ServerInterface.BGcheckserverforgps==false) {
                            if (Resultdata.equals("timeout")) {//timeout case
                                maxUpdateCount = (maxUpdateCount / 2);
                                if (maxUpdateCount <= 5)
                                    maxUpdateCount = 5;
                            }
                            else{
                                maxUpdateCount = 40;
                            }
                        }
                        if(requestId!=0)
                            db.updateEventStatus(Constants.event_status_pending_code,requestId);

                        db.insertUpdatesCount("bgcallserverFail");
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            callingServerApiForEvent=false;
            return null;
        }
    }//end


    //Async class for sending task data to server
    private class SendTaskEventUpdatesToServer extends AsyncTask<Void, Void, Void>
    {
        protected void onPreExecute()
        {
            //do nothing
        }

        protected void onPostExecute(Void unused)
        {
            callingServerApiForTask=false;
            new SendImageUploadEventsToServer().execute();
        }

        protected Void doInBackground(Void... par)
        {
            try{
                String url = Constants.ServerApiUrl + "mobile/TaskEventUpdate";
                if(tiny==null)
                    tiny=new TinyDB(EventUpdateService.this);
                String loginToken=tiny.getString("loginToken");
                JSONObject jo = db.getPendingEvents(4,loginToken,"ALLTASK");
                JSONArray eventsUpdateArray=jo.getJSONArray("updates");
                if(eventsUpdateArray.length()>0) {
                    db.insertUpdatesCount("bgcallserverAttempt");
                    int requestId = Integer.parseInt(jo.getString("reqId"));
                    String iemino = tiny.getString("iemino");
                    String Resultdata = ServerInterface.CallServerApiBgForTask(jo, url, loginToken,iemino);
                    if(ServerInterface.BGcheckserverfortask && !Resultdata.equals("")) {//server response properly
                        try {
                            JSONObject mydata = new JSONObject(Resultdata);
                            Log.d(TAG, "bulk sync response" + mydata.toString());
                            String res = mydata.getString("Result");
                            Boolean status = mydata.getBoolean("status");
                            if (status && res.equals("OK")) {
                                JSONObject record = mydata.getJSONObject("Record");
                                //add new task if assigned i a running trip
                                //AddNewTaskToTrip(record);

                                String respid = record.getString("reqid");
                                //deleting updated record
                                db.deleteUpdatedEvents(Integer.parseInt(respid), "NOTALL");
                                db.insertUpdatesCount("bgcallserverSuccess");
                            } else {
                                if (requestId != 0)
                                    db.updateEventStatus(Constants.event_status_pending_code, requestId);

                                db.insertUpdatesCount("bgcallserverFail");
                            }
                        } catch (Exception e) {
                            if(requestId!=0)
                                db.updateEventStatus(Constants.event_status_pending_code,requestId);

                            db.insertUpdatesCount("bgcallserverFail");
                            e.printStackTrace();
                        }

                    }
                    else {//response blank case
                        if(requestId!=0)
                            db.updateEventStatus(Constants.event_status_pending_code,requestId);

                        db.insertUpdatesCount("bgcallserverFail");
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            callingServerApiForTask=false;
            return null;
        }
    }//end

    //Async class for uploading image to server
    private class SendImageUploadEventsToServer extends AsyncTask<Void, Void, Void>
    {
        int ImagerequestId=0;

        String Result="";
        protected void onPreExecute()
        {
            //do nothing
        }
        protected void onPostExecute(Void unused)
        {
            if (ServerInterface.Imagecheckserver && !Result.equals("")) {
                try {
                    JSONObject mydata = new JSONObject(Result);
                    Log.d(TAG, "bulk sync response" + mydata.toString());
                    String res = mydata.getString("Result");
                    if (res.equals("OK")) {
                        String respid = mydata.getString("Record");
                        //deleting updated record
                        db.deleteUpdatedEvents(Integer.parseInt(respid),"NOTALL");
                    } else {
                        if (ImagerequestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, ImagerequestId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (ImagerequestId != 0)
                    db.updateEventStatus(Constants.event_status_pending_code, ImagerequestId);
            }
        }
        protected Void doInBackground(Void... par)
        {
            try{
                String url = Constants.ServerApiUrl + "mobile/task/uploadimage";
                if(tiny==null)
                    tiny=new TinyDB(EventUpdateService.this);
                String loginToken=tiny.getString("loginToken");
                JSONObject jo = db.getPendingImageUploadEvent();
                if (jo.length() > 0) {
                    JSONObject params = new JSONObject();
                    if(jo.getString("filename").contains("SIGN")) {
                        url = Constants.ServerApiUrl + "mobile/task/uploadSignimage";
                        params.put("type", "sign");
                    }
                    else if(jo.getString("filename").contains("IDPROOF")) {
                        url = Constants.ServerApiUrl + "mobile/task/uploadimage";
                        params.put("type", "idproof");
                    }
                    else {
                        url = Constants.ServerApiUrl + "mobile/task/uploadimage";
                        params.put("type", "disposition");
                    }
                    params.put("token", loginToken);
                    params.put("taskId", jo.getLong("taskId"));
                    params.put("reqId", jo.getString("reqId"));
                    ImagerequestId = Integer.parseInt(jo.getString("reqId"));
                    Result = ServerInterface.UploadImageApi(new File(jo.getString("filename")), url, params,loginToken);
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }//end

    //this function is used to add new task in a running trip
    private void AddNewTaskToTrip(JSONObject jsontrip) {
        try {
            //save tasks associated with trip
            Log.d(TAG, "Ashish AddNewTaskToTrip");
            JSONArray taskdata = jsontrip.getJSONArray("tasks");
            for (int j=0;j<taskdata.length();j++) {
                JSONObject jsontask = taskdata.getJSONObject(j);
                Long taskId=jsontask.getLong("id");
                String ref=jsontask.getString("refNum");
                String platformId=jsontask.getString("platformId");
                String itemCategory=jsontask.getString("itemCategory");
                String itemDescription = "";
                if (jsontask.getString("taskDescription") != null && !jsontask.getString("taskDescription").equals("null"))
                    itemDescription= jsontask.getString("taskDescription");

                String optionPhotocount = "";
                if (jsontask.getString("optionalImageCount") != null && !jsontask.getString("optionalImageCount").equals("null"))
                    itemDescription= jsontask.getString("optionalImageCount");

                String mandatoryPhotocount = "";
                if (jsontask.getString("mandatoryImageCount") != null && !jsontask.getString("mandatoryImageCount").equals("null"))
                    itemDescription= jsontask.getString("mandatoryImageCount");
                long pickups=jsontask.getInt("numTasks");
                String name="";
                if (jsontask.getString("name") != null && !jsontask.getString("name").equals("null"))
                    name=jsontask.getString("name");
                String address=jsontask.getString("address");
                String zipcode=jsontask.getString("zipCode");
                String phone="";
                if (jsontask.getString("phone") != null && !jsontask.getString("phone").equals("null"))
                    phone=jsontask.getString("phone");
                String taskType=jsontask.getString("type");
                int pickupQty=0;
                String reason="";

                String delieveryDateTime="";
                try{
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                    String reformattedStr="";
                    try {
                        if(jsontask.getString("completeBy")!=null && !jsontask.getString("completeBy").equals("null") && !jsontask.getString("completeBy").equals(""))
                            reformattedStr = formatter.format(fromUser.parse(jsontask.getString("completeBy")));
                    } catch (Exception e) {
                        Log.e(TAG, "error in parsing date", e);
                    }
                    delieveryDateTime=reformattedStr;//completed by datetime
                }
                catch(Exception e){
                    Log.e(TAG, "error in response", e);
                }
                String comments="";
                String taskstatus=jsontask.getString("status");
                int tstatus=Constants.task_status_pending_code;
                if(taskstatus.equals("CMT")){
                    tstatus=Constants.task_status_done_code;
                }
                else if(taskstatus.equals("CLD")){
                    tstatus=Constants.task_status_failed_code;
                }
                else if(taskstatus.equals("FLD")){
                    tstatus=Constants.task_status_qc_fail_code;
                }
                else if(taskstatus.equals("OFD")){
                    tstatus=Constants.task_status_start_code;
                }
                else if(taskstatus.equals("ARD")){
                    tstatus=Constants.task_status_doorstep_code;
                }
                else{
                    tstatus=Constants.task_status_pending_code;
                }
                String tid=jsontask.getString("tripId");
                long tripid = Long.parseLong(tid);
                String paymentMode="";
                try{
                    paymentMode=jsontask.getString("paymentModeType");
                }
                catch (Exception e){
                    paymentMode="";
                }
                String amount="";
                String consigneeNumber="";
                String consigneeName="";
                String codamount="";
                try{
                    consigneeNumber=jsontask.getString("consigneeNumber");
                    consigneeName=jsontask.optString("contactName");
                    amount=jsontask.getString("amount");
                    codamount=jsontask.getString("codAmount");
                }
                catch (Exception e){
                    consigneeNumber="";
                }

                String BasketId="";
                String taskPinno="";
                if(taskType.contains("OPK")) {
                    try {
                        BasketId = jsontask.getString("superTaskId");
                        String superTaskExpectedQty = jsontask.getString("superTaskExpectedQty");
                        String superTaskStatus = jsontask.getString("superTaskStatus");
                        taskPinno=jsontask.getString("securePin");
                        int ststatus = Constants.basket_status_pending_code;
                        if (superTaskStatus.equals("CMT")) {
                            ststatus = Constants.basket_status_complete_code;
                        } else if (superTaskStatus.equals("CLD")) {
                            ststatus = Constants.basket_status_failed_code;
                        } else {
                            ststatus = Constants.basket_status_pending_code;
                        }

                        if (BasketId != null && !BasketId.equals("")) {
                            db.insertBasketData(Long.valueOf(BasketId), tripid, taskType, name, address, zipcode, Integer.parseInt(superTaskExpectedQty), ststatus);
                        }
                    }
                    catch (Exception e){

                    }
                }

                String res=db.insertTaskData(tripid, taskId, ref,platformId, pickups, name, address, zipcode, phone, taskType,
                        pickupQty, reason, delieveryDateTime, comments, tstatus,paymentMode,
                        BasketId,consigneeNumber,consigneeName,taskPinno, 5,amount,codamount,itemCategory,itemDescription,mandatoryPhotocount,optionPhotocount);
                if(res.equals("NEW")) {
                    db.increaseTripNumTask(tripid);
                    String message = "A new task with shipment no " + ref + " has been assigned to trip number " + tripid;
                    db.insertNewNotification(message);
                    CommonFunctions.showNotification(EventUpdateService.this, message);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Not able to understand server response", e);
        } catch (Exception e) {
            Log.e(TAG, "Not able to understand server response", e);
        }
    }

    private static int NOTIFICATION_ID = 41123427;
    //this method notify a notification if gps is disabled and when user clicks on this notification then it will take it to gps settings page
    public static void showNotification(final Context context, String message) {
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
