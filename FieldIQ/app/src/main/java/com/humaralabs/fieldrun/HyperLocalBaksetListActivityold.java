package com.humaralabs.fieldrun;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.HyperBasket;
import com.humaralabs.fieldrun.datastructure.Trip;
import com.humaralabs.fieldrun.server.ServerInterface;
import com.humaralabs.fieldrun.service.EventUpdateService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HyperLocalBaksetListActivityold extends AppCompatActivity{
    private static final String TAG = "TRIPBASKETLIST";
    private ProgressDialog dialog;
    private ArrayList<HyperBasket> hyperbasketList;
    private Trip trip;
    private long tripId;
    private String tripOrigin="";
    private String tripFacility="";
    private String tripExpiryDateTime="";
    private int tripStatus;
    Button button_trip_action;
    DbAdapter db;
    TinyDB tiny;

    private Boolean alreadyCallingThread=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hyper_basket_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        if (toolbar != null) {
            toolbar.setTitle("Tasks");
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.needToRefresh=true;
                    finish();
                }
            });
        }
        alreadyCallingThread=false;
        Intent ii=getIntent();
        tripId = ii.getExtras().getLong("trip_id");
        tripOrigin = ii.getExtras().getString("origin");
        tripFacility = ii.getExtras().getString("facility");
        tripExpiryDateTime = ii.getExtras().getString("tripExpiryDateTime");

        db=new DbAdapter(HyperLocalBaksetListActivityold.this);
        tiny=new TinyDB(HyperLocalBaksetListActivityold.this);
        hyperbasketList=new ArrayList<HyperBasket>();


        setTripActionButton();
        GPSChecker.GPSCheck(HyperLocalBaksetListActivityold.this, false);
    }





    @Override
    protected void onResume() {
        super.onResume();
        GPSChecker.GPSCheck(HyperLocalBaksetListActivityold.this, false);
        //start service
        CommonFunctions.StartSevice(HyperLocalBaksetListActivityold.this);
        refreshList(Constants.event_status_pending_code, tripId);
        refreshView();
        Log.d(TAG, "TTL resumed");
    }


    public void refreshView() {
        //refresh tab

        //refresh trip action button
        setTripActionButton();
        //end refresh trip action button

        Long activeTripId=db.getActiveTripId();
        String TextToDisplay="";
        if(activeTripId==0){
            Log.d(TAG, "NO TRIPS in db!");
            TextToDisplay="No Active Trip";
        }
        else{
            TextToDisplay = "Active Trip Details " + tripId + " " + tripOrigin;
        }
        CommonFunctions.showActiveNotification(HyperLocalBaksetListActivityold.this, TextToDisplay);
    }

    HyperBasket hyperBasket;
    public void refreshList(int taskstatus, final Long tripId) {
        hyperbasketList=db.GetHyperBasketForParticulatTrips(taskstatus, tripId);
        //Log.d(TAG, "Loaded " + hyperbasketList.size() + " tasks for tripid " + tripId);
        ListView lv_basket = (ListView) findViewById(R.id.basketlist);

        //HyperBasketListAdapter basketListAdapter = new HyperBasketListAdapter(this, hyperbasketList, R.layout.activity_hyper_basket_list_item,tripOrigin,tripFacility,tripExpiryDateTime);

        //lv_basket.setAdapter(basketListAdapter);

        /*lv_basket.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (tiny == null)
                    tiny = new TinyDB(HyperLocalBaksetListActivity.this);
                hyperBasket = hyperbasketList.get(position);
                int tripstatus = db.getTripStatus(hyperBasket.hyper_basket_trip_id);
                if (tripstatus == Constants.trip_status_active_code) {
                        Intent detailIntent = new Intent(HyperLocalBaksetListActivity.this, HyperLocalTripTaskListActivity.class);
                        detailIntent.putExtra("hyper_basket_ref_no", hyperBasket.hyper_basket_ref_no);
                        detailIntent.putExtra("trip_id", tripId);
                        detailIntent.putExtra("origin", tripOrigin);
                        detailIntent.putExtra("facility", tripFacility);
                        detailIntent.putExtra("tripExpiryDateTime", tripExpiryDateTime);
                        startActivity(detailIntent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                } else if (tripstatus == Constants.basket_status_pending_code) {
                    Toast.makeText(HyperLocalBaksetListActivity.this, "Trip not started yet.", Toast.LENGTH_LONG).show();
                } else if (tripstatus == Constants.basket_status_complete_code) {
                    Toast.makeText(HyperLocalBaksetListActivity.this, "Trip already completed.", Toast.LENGTH_LONG).show();
                }
            }
        });*/
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            //MainActivity.needToRefresh=true;
            finish();
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
        }

        return super.onOptionsItemSelected(item);
    }

    public void setTripActionButton() {
        tripStatus=db.getTripStatus(tripId);
        button_trip_action = (Button) findViewById(R.id.button_trip_action);
        if (tripStatus == Constants.trip_status_pending_code) {//status 0 means pending
            button_trip_action.setVisibility(View.VISIBLE);
            button_trip_action.setText("Start Trip");
        } else if (tripStatus == Constants.trip_status_active_code) {//status 1 means active
            button_trip_action.setText("Trip Complete");
            //checking all task has been done or not if yes then show trip complete button
            int pendingTaskCount = db.GetTaskForParticulatTripsCount(Constants.task_status_pending_code, String.valueOf(tripId), "Task");
            if (pendingTaskCount > 0)
                button_trip_action.setVisibility(View.GONE);
            else
                button_trip_action.setVisibility(View.VISIBLE);
        }
        else if (tripStatus == Constants.trip_status_complete_code) {//status 2 means complete
            button_trip_action.setVisibility(View.GONE);
        }
        button_trip_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(alreadyCallingThread==false) {
                    alreadyCallingThread = true;
                    tripStatus = db.getTripStatus(tripId);
                    StartTripActionThread();
                }
                //new SendTripEventsToServer().execute();
            }
        });
    }



    Boolean AuthError=false;
    public void StartTripActionThread(){
        new Thread()
        {
            public void run()
            {
                AuthError=false;
                Boolean status=SendTripEventsToServer();
                if(status){
                    final Boolean fetchtripstatus=callserverForTripStartOrComplete();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                                alreadyCallingThread=false;
                            }
                            if (AuthError) {
                                AutomaticLogout();
                            } else if (!fetchtripstatus) {//error case then read from local db
                                CommonFunctions.sendMessageToActivity(1, HyperLocalBaksetListActivityold.this);
                            }
                            refreshView();
                        }
                    });
                }
                else{//error case then read from local db
                    db.insertUpdatesCount("cccallserverFail");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                                alreadyCallingThread=false;
                            }
                            if (AuthError) {
                                AutomaticLogout();
                            }
                            else{
                                CommonFunctions.sendMessageToActivity(1, HyperLocalBaksetListActivityold.this);
                            }
                        }
                    });
                }
            }
        }.start();
    }

    public void AutomaticLogout(){
        tiny.putString("loginToken", "");
        tiny.putString("gcmtoken", "");
        Toast.makeText(HyperLocalBaksetListActivityold.this, "Authentication error! Please Login Again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private ProgressDialog Dialog = null;
    private Boolean SendTripEventsToServer(){
        Boolean CallServerForFetchTripdata=false;
        long activetripid=0;
        int requestId=0;
        String Result="NoEvents";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog = new ProgressDialog(HyperLocalBaksetListActivityold.this);
                Dialog.setMessage("Please wait...");
                Dialog.show();
                Dialog.setCanceledOnTouchOutside(false);
                Dialog.setCancelable(false);
            }
        });
        try{
            activetripid=db.getActiveTripId();
            if(activetripid!=0) {
                String url = Constants.ServerApiUrl + "mobile/TaskEventUpdate";
                if (tiny == null)
                    tiny = new TinyDB(MainActivity.mcontext);
                String loginToken = tiny.getString("loginToken");
                JSONObject jo = db.getPendingEvents(2000, loginToken,"ALLTASK");
                JSONArray eventsUpdateArray=jo.getJSONArray("updates");
                if(eventsUpdateArray.length()>0) {
                    requestId=Integer.parseInt(jo.getString("reqId"));
                    db.insertUpdatesCount("cccallserverAttempt");
                    String iemino = tiny.getString("iemino");
                    Result = ServerInterface.CallServerApi(jo, url,180,loginToken,iemino);
                    if(ServerInterface.checkserver && !Result.equals("NoEvents") && !Result.equals("")) {
                        try {
                            JSONObject mydata = new JSONObject(Result);
                            if(mydata.getString("Message")!=null &&  mydata.getString("Message").equals("Auth Error")) {
                                AuthError = true;
                                CallServerForFetchTripdata=false;
                                return false;
                            }
                            Log.d(TAG, "bulk sync response" + mydata.toString());
                            String res = mydata.getString("Result");
                            Boolean status = mydata.getBoolean("status");
                            if (status && res.equals("OK")) {
                                JSONObject record = mydata.getJSONObject("Record");
                                String respid = record.getString("reqid");
                                //deleting updated record
                                db.deleteUpdatedEvents(Integer.parseInt(respid), "NOTALL");
                                db.insertUpdatesCount("cccallserverSuccess");
                                CallServerForFetchTripdata=true;
                            } else {
                                if (requestId != 0)
                                    db.updateEventStatus(Constants.event_status_pending_code, requestId);
                                CommonFunctions._messageToShow = mydata.getString("Message");
                                CallServerForFetchTripdata=false;
                            }
                        } catch (Exception e) {//2(Exception in parse Response)
                            if (requestId != 0)
                                db.updateEventStatus(Constants.event_status_pending_code, requestId);
                            CommonFunctions._messageToShow = "Invalid Response! Refresh or Please try again later!!";
                            CallServerForFetchTripdata=false;
                            e.printStackTrace();
                        }
                    }
                    else {
                        if (requestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, requestId);
                        CommonFunctions._messageToShow = "Network Error! Refresh or Please try again later!!";
                        CallServerForFetchTripdata=false;
                    }
                }
                else{
                    CallServerForFetchTripdata=true;
                }
            }
            else{
                db.deleteUpdatedEvents(0, "DATE");
                CallServerForFetchTripdata=true;
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return CallServerForFetchTripdata;
    }


    private Boolean callserverForTripStartOrComplete() {
        Boolean CallServerForTripStartOrComplete=false;
        String Result = "";
        if (tripStatus == Constants.trip_status_pending_code)
            Result = StartTrip();
        else if (tripStatus == Constants.trip_status_active_code)
            Result = CompleteTrip();

        if (ServerInterface.checkserver && !Result.equals("")) {
            JSONObject mydata = null;
            try {
                mydata = new JSONObject(Result);
                if(mydata.getString("Message").equals("Auth Error")) {
                    AuthError = true;
                    CallServerForTripStartOrComplete=false;
                    return false;
                }
                String res = mydata.getString("Result");
                Boolean status = mydata.getBoolean("status");
                if (status && res.equals("OK")) {
                    CallServerForTripStartOrComplete=true;
                    if (tripStatus == Constants.trip_status_pending_code)
                        db.updateTripStatus(Constants.trip_status_active_code, tripId);
                    else if (tripStatus == Constants.trip_status_active_code) {
                        MainActivity.needToRefresh=true;
                        db.updateTripStatus(Constants.trip_status_complete_code, tripId);
                        finish();
                        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                    }
                }
                else{
                    String msg=mydata.getString("Message");
                    CommonFunctions._messageToShow =msg;
                    CallServerForTripStartOrComplete=false;
                }
            } catch (JSONException e) {
                CallServerForTripStartOrComplete=false;
                CommonFunctions._messageToShow = "Invalid Response! Refresh or Please try again later!!";
                e.printStackTrace();
            }
        }
        else{
            CommonFunctions._messageToShow = "Connection Error! Please check your internet!!";
            CallServerForTripStartOrComplete=false;
        }
        return CallServerForTripStartOrComplete;
    }

    private String StartTrip(){
        String Response="";
        String uri = Constants.ServerApiUrl + "mobile/trip/active";

        JSONObject params = new JSONObject();
        Double latitude = null, longitude = null;
        try {
            if (EventUpdateService.mlocation != null) {
                latitude = EventUpdateService.mlocation.getLatitude();
                longitude = EventUpdateService.mlocation.getLongitude();
            }
            params.put("latitude", latitude);
            params.put("longitude", longitude);
            params.put("token", tiny.getString("loginToken"));
            params.put("tripId", tripId);
            params.put("ts", CommonFunctions.getCurrentTime());
            params.put("eventType", "tripstart");
            params.put("reqid", tripId);
            String iemino = tiny.getString("iemino");
            Response=ServerInterface.CallServerApi(params,uri,55,tiny.getString("loginToken"),iemino);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response;
    }

    private String CompleteTrip(){
        String Response="";
        String uri = Constants.ServerApiUrl + "mobile/trip/complete";

        JSONObject params = new JSONObject();
        Double latitude = null, longitude = null;
        try {
            if (EventUpdateService.mlocation != null) {
                latitude = EventUpdateService.mlocation.getLatitude();
                longitude = EventUpdateService.mlocation.getLongitude();
            }
            params.put("latitude", latitude);
            params.put("longitude", longitude);
            params.put("token", tiny.getString("loginToken"));
            params.put("tripId", tripId);
            params.put("ts", CommonFunctions.getCurrentTime());
            params.put("eventType", "tripcomplete");
            params.put("reqid", tripId);
            String iemino = tiny.getString("iemino");
            Response=ServerInterface.CallServerApi(params,uri,55,tiny.getString("loginToken"),iemino);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Response;
    }

    @Override
    public void onBackPressed() {
        MainActivity.needToRefresh=true;
        finish();
    }



    /*private ProgressDialog Dialog = null;
    private class SendTripEventsToServer extends AsyncTask<Void, Void, Void>
    {
        long activetripid=0;
        int requestId=0;
        String Result="NoEvents";
        protected void onPreExecute()
        {
            Dialog = new ProgressDialog(HyperLocalBaksetListActivity.this);
            Dialog.setMessage("Please wait...");
            Dialog.show();
            Dialog.setCanceledOnTouchOutside(false);
            Dialog.setCancelable(false);
        }
        protected void onPostExecute(Void unused)
        {
            if(activetripid!=0) {

                if(Result.equals("NoEvents")){
                    new callserver_async().execute();
                }
                else if(ServerInterface.checkserver && !Result.equals("")) {
                    try {
                        JSONObject mydata = new JSONObject(Result);
                        Log.d(TAG, "bulk sync response" + mydata.toString());
                        String res1 = mydata.getString("status");
                        String res = mydata.getString("Result");
                        Boolean status=mydata.getBoolean("status");
                        if (status && res.equals("OK")) {
                            JSONObject record = mydata.getJSONObject("Record");
                            String respid = record.getString("reqid");
                            //deleting updated record
                            db.deleteUpdatedEvents(Integer.parseInt(respid), "ALL");
                            db.insertUpdatesCount("cccallserverSuccess");
                            new callserver_async().execute();
                        } else {
                            if (requestId != 0)
                                db.updateEventStatus(Constants.event_status_pending_code, requestId);
                            CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                            CommonFunctions.sendMessageToActivity(1, HyperLocalBaksetListActivity.this);
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                            }
                            db.insertUpdatesCount("cccallserverFail");
                        }
                    } catch (Exception e) {
                        if (requestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, requestId);
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, HyperLocalBaksetListActivity.this);
                        if (Dialog != null && Dialog.isShowing()) {
                            Dialog.dismiss();
                        }
                        db.insertUpdatesCount("cccallserverFail");
                        e.printStackTrace();
                    }
                }
                else {
                    if(requestId!=0)
                        db.updateEventStatus(Constants.event_status_pending_code,requestId);
                    CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                    CommonFunctions.sendMessageToActivity(1,  HyperLocalBaksetListActivity.this);
                    if(Dialog != null && Dialog.isShowing()) {
                        Dialog.dismiss();
                    }
                    db.insertUpdatesCount("cccallserverFail");
                }
            }
            else{
                new callserver_async().execute();
            }
        }
        protected Void doInBackground(Void... par)
        {
            try{
                activetripid=db.getActiveTripId();
                if(activetripid!=0) {
                    String url = Constants.ServerApiUrl + "mobile/TaskEventUpdate";
                    if (tiny == null)
                        tiny = new TinyDB(HyperLocalBaksetListActivity.this);
                    String loginToken = tiny.getString("loginToken");
                    String iemino = tiny.getString("iemino");
                    JSONObject jo = db.getPendingEvents(100, loginToken,"ALLTASK");
                    JSONArray eventsUpdateArray=jo.getJSONArray("updates");
                    if(eventsUpdateArray.length()>0) {
                        requestId=Integer.parseInt(jo.getString("reqId"));
                        db.insertUpdatesCount("cccallserverAttempt");
                        Result = ServerInterface.CallServerApi(jo, url,180,loginToken,iemino);
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }//end get trips class

    private class callserver_async extends AsyncTask<Void, Void, Void>
    {
        String Result="";
        protected void onPreExecute()
        {
        }
        protected void onPostExecute(Void unused)
        {
            if(Dialog != null && Dialog.isShowing())
            {
                if (ServerInterface.checkserver) {
                    try {
                        JSONObject jobj=new JSONObject(Result);
                        Boolean res=jobj.getBoolean("status");
                        if(res) {
                            if (tripStatus == Constants.trip_status_pending_code)
                                db.updateTripStatus(Constants.trip_status_active_code, tripId);
                            else if (tripStatus == Constants.trip_status_active_code) {
                                db.updateTripStatus(Constants.trip_status_complete_code, tripId);
                                finish();
                                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                            }
                        }
                        else{
                            String msg=jobj.getString("Message");
                            CommonFunctions._messageToShow =msg;
                            CommonFunctions.sendMessageToActivity(1, HyperLocalBaksetListActivity.this);
                        }
                    } catch (Exception e) {
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, HyperLocalBaksetListActivity.this);
                        e.printStackTrace();
                    }
                } else {
                    CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                    CommonFunctions.sendMessageToActivity(1, HyperLocalBaksetListActivity.this);
                }
                Dialog.dismiss();
                refreshView();
            }
        }
        protected Void doInBackground(Void... par)
        {
            try{
                if (tripStatus == Constants.trip_status_pending_code)
                    Result=StartTrip();
                else if (tripStatus == Constants.trip_status_active_code)
                    Result=CompleteTrip();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }*///end get trips class
}
