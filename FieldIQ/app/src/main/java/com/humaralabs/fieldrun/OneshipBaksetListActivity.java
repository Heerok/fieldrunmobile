package com.humaralabs.fieldrun;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.humaralabs.fieldrun.adapter.BasketListAdapter;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.Basket;
import com.humaralabs.fieldrun.datastructure.Trip;
import com.humaralabs.fieldrun.server.ServerInterface;
import com.humaralabs.fieldrun.service.EventUpdateService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OneshipBaksetListActivity extends ActionBarActivity implements ActionBar.TabListener{
    private static final String TAG = "TRIPBASKETLIST";
    private ProgressDialog dialog;
    private ArrayList<Basket> basketList;
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
        setContentView(R.layout.activity_basket_list);
        alreadyCallingThread=false;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Intent ii=getIntent();
        tripId = ii.getExtras().getLong("trip_id");
        tripOrigin = ii.getExtras().getString("origin");
        tripFacility = ii.getExtras().getString("facility");
        tripExpiryDateTime = ii.getExtras().getString("tripExpiryDateTime");

        db=new DbAdapter(OneshipBaksetListActivity.this);
        tiny=new TinyDB(OneshipBaksetListActivity.this);
        basketList=new ArrayList<Basket>();

        addTab(actionBar, "PENDING", tripId);
        addTab(actionBar, "DONE", tripId);
        addTab(actionBar, "FAILED", tripId);
        actionBar.setSelectedNavigationItem(0);
        setTripActionButton();
        GPSChecker.GPSCheck(OneshipBaksetListActivity.this, false);
    }


    private void addTab(ActionBar actionBar, String tab,Long tripid) {
       ActionBar.Tab t1 = actionBar.newTab();
        int status=0;
        if(tab.equals("PENDING"))
            status=0;
        else if(tab.equals("DONE"))
            status=1;
        else if(tab.equals("FAILED"))
            status=2;

        t1.setText(tab + " (" + db.GetBasketForParticulatTripsCount(status, tripid,"Particuular") + ")");
        //t1.setText(tab);
        t1.setTag(tab);
        t1.setTabListener(this);
        actionBar.addTab(t1);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, "tab : " + tab.getText());
        int status=tab.getPosition();//sttus pending,done,failed
        refreshList(status, tripId);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        GPSChecker.GPSCheck(OneshipBaksetListActivity.this, false);
        //start service
        CommonFunctions.StartSevice(OneshipBaksetListActivity.this);
        refreshList(Constants.event_status_pending_code, tripId);
        refreshView();
        Log.d(TAG, "TTL resumed");
    }


    public void refreshView() {
        //refresh tab
        ActionBar actionBar = getSupportActionBar();
        for (int i=0;i<actionBar.getTabCount();i++) {
            ActionBar.Tab tab = actionBar.getTabAt(i);
            if(i==0)
                tab.setText("pending" + " (" + db.GetBasketForParticulatTripsCount(i, tripId,"Particuular") + ")");
            else if(i==1)
                tab.setText("done" + " (" + db.GetBasketForParticulatTripsCount(i, tripId,"Particuular") + ")");
            else if(i==2)
                tab.setText("failed" + " (" + db.GetBasketForParticulatTripsCount(i, tripId,"Particuular") + ")");
        }
        //end refresh tab

        //refresh list
        actionBar.setSelectedNavigationItem(0);
        //end refresh list

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
        CommonFunctions.showActiveNotification(OneshipBaksetListActivity.this, TextToDisplay);
    }
    Basket basket;
    public void refreshList(int taskstatus,Long tripId) {
        basketList=db.GetBasketForParticulatTrips(taskstatus, tripId);
        Log.d(TAG, "Loaded " + basketList.size() + " tasks for tripid " + tripId);
        ListView lv_basket = (ListView) findViewById(R.id.basketlist);

        BasketListAdapter basketListAdapter = new BasketListAdapter(this, basketList, R.layout.activity_basket_list_item,taskstatus);

        lv_basket.setAdapter(basketListAdapter);

        lv_basket.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (tiny == null)
                    tiny = new TinyDB(OneshipBaksetListActivity.this);
                basket = basketList.get(position);
                int tripstatus = db.getTripStatus(basket.basket_trip_id);
                if (tripstatus == Constants.trip_status_active_code) {
                    int basketStatus = basket.basket_status;
                    if (basketStatus == Constants.basket_status_pending_code) {
                        Log.d("CLICK", "Starting detail");
                        Intent detailIntent = new Intent(OneshipBaksetListActivity.this, BasketDetailActivity.class);
                        detailIntent.putExtra("basket_server_id", basket.basket_server_id);
                        detailIntent.putExtra("basket_eqty", basket.basket_eqty);
                        detailIntent.putExtra("basket_seller_name", basket.basket_seller_name);
                        detailIntent.putExtra("basket_seller_zipcode", basket.basket_seller_zipcode);
                        detailIntent.putExtra("basket_seller_address", basket.basket_seller_address);
                        detailIntent.putExtra("basket_trip_id", basket.basket_trip_id);
                        detailIntent.putExtra("basket_trip_type", basket.basket_trip_type);
                        detailIntent.putExtra("tripExpiryDateTime", tripExpiryDateTime);
                        detailIntent.putExtra("tripFacility", tripFacility);
                        startActivity(detailIntent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                    } else if (basketStatus == Constants.basket_status_complete_code) {
                        //show quantity popup
                        //showdialog(basket.basket_server_id);
                        Toast.makeText(OneshipBaksetListActivity.this, "This basket has been update.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(OneshipBaksetListActivity.this, "This basket has been update.", Toast.LENGTH_LONG).show();
                    }
                } else if (tripstatus == Constants.basket_status_pending_code) {
                    Toast.makeText(OneshipBaksetListActivity.this, "Trip not started yet.", Toast.LENGTH_LONG).show();
                } else if (tripstatus == Constants.basket_status_complete_code) {
                    Toast.makeText(OneshipBaksetListActivity.this, "Trip already completed.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    Long gbasket_server_id;
    int  basketPickedQty=0;
    private void showdialog(Long basket_server_id) {
        gbasket_server_id=basket_server_id;
        final android.app.Dialog dialog = new Dialog(OneshipBaksetListActivity.this);
        //hiding default title bar of dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.basket_update_popup);
        dialog.getWindow().getAttributes().width= WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        final EditText basketPickedQtyText=(EditText)dialog.getWindow().findViewById(R.id.basketpickupqty);
        int qty=db.getTotalBasketPickedQuantity(Long.valueOf(basket_server_id));
        basketPickedQtyText.setText(""+qty);

        Button basket_button_ok=(Button)dialog.getWindow().findViewById(R.id.basket_button_ok);
        dialog.setCanceledOnTouchOutside(false);

        dialog.show(); //to show dialog box
        basket_button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            basketPickedQty=Integer.parseInt(basketPickedQtyText.getText().toString());
            new updateBaksetQty_async().execute();
            dialog.dismiss();
            }
        });
    }

    private class updateBaksetQty_async extends AsyncTask<Void, Void, Void>
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
                            CommonFunctions._messageToShow = "Quntity Updated!";
                            CommonFunctions.sendMessageToActivity(1, OneshipBaksetListActivity.this);
                            Dialog.dismiss();
                        }
                    } catch (Exception e) {
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, OneshipBaksetListActivity.this);
                        e.printStackTrace();
                    }
                } else {
                    CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                    CommonFunctions.sendMessageToActivity(1, OneshipBaksetListActivity.this);
                }

            }
        }
        protected Void doInBackground(Void... par)
        {
            try{
                String url = Constants.ServerApiUrl + "mobile/SuperTaskUpdate";
                if (tiny == null)
                    tiny = new TinyDB(OneshipBaksetListActivity.this);
                String loginToken = tiny.getString("loginToken");
                JSONObject params = new JSONObject();
                params.put("token", loginToken);
                params.put("superTaskId", gbasket_server_id);
                params.put("status", "CMT");
                params.put("remarks", "");
                params.put("pickedQty", basketPickedQty);
                String iemino = tiny.getString("iemino");
                Result = ServerInterface.CallServerApi(params, url,55,loginToken,iemino);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }//end get trips class

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
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
            int pendingTaskCount = db.GetBasketForParticulatTripsCount(Constants.task_status_pending_code, tripId,"Particuular");
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
                                CommonFunctions.sendMessageToActivity(1, OneshipBaksetListActivity.this);
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
                                CommonFunctions.sendMessageToActivity(1, OneshipBaksetListActivity.this);
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
        Toast.makeText(OneshipBaksetListActivity.this, "Authentication error! Please Login Again.", Toast.LENGTH_LONG).show();
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
                Dialog = new ProgressDialog(OneshipBaksetListActivity.this);
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
            Response=ServerInterface.CallServerApi(params, uri, 55, tiny.getString("loginToken"),iemino);
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


    /*private ProgressDialog Dialog = null;
    private class SendTripEventsToServer extends AsyncTask<Void, Void, Void>
    {
        long activetripid=0;
        int requestId=0;
        String Result="NoEvents";
        protected void onPreExecute()
        {
            Dialog = new ProgressDialog(OneshipBaksetListActivity.this);
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
                            CommonFunctions.sendMessageToActivity(1, OneshipBaksetListActivity.this);
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                            }
                            db.insertUpdatesCount("cccallserverFail");
                        }
                    } catch (Exception e) {
                        if (requestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, requestId);
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, OneshipBaksetListActivity.this);
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
                    CommonFunctions.sendMessageToActivity(1,  OneshipBaksetListActivity.this);
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
                        tiny = new TinyDB(OneshipBaksetListActivity.this);
                    String loginToken = tiny.getString("loginToken");
                    JSONObject jo = db.getPendingEvents(100, loginToken,"ALLTASK");
                    JSONArray eventsUpdateArray=jo.getJSONArray("updates");
                    if(eventsUpdateArray.length()>0) {
                        requestId=Integer.parseInt(jo.getString("reqId"));
                        db.insertUpdatesCount("cccallserverAttempt");
                        Result = ServerInterface.CallServerApi(jo, url,180,loginToken);
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
                            CommonFunctions.sendMessageToActivity(1, OneshipBaksetListActivity.this);
                        }
                    } catch (Exception e) {
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, OneshipBaksetListActivity.this);
                        e.printStackTrace();
                    }
                } else {
                    CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                    CommonFunctions.sendMessageToActivity(1, OneshipBaksetListActivity.this);
                }
                Dialog.dismiss();
                refreshView();
                if (tripStatus == Constants.trip_status_pending_code) {
                    //call server trip list api
                    new Get_AllTripsFromServer().execute();
                }

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



    //Async class for getting trips data from server
    /*private class Get_AllTripsFromServer extends AsyncTask<Void, Void, Void>
    {
        String Result="";
        protected void onPreExecute()
        {
            Dialog = new ProgressDialog(OneshipBaksetListActivity.this);
            Dialog.setMessage("Refreshing Trip...");
            Dialog.show();
            Dialog.setCanceledOnTouchOutside(false);
            Dialog.setCancelable(false);
        }
        protected void onPostExecute(Void unused)
        {
            if(Dialog != null && Dialog.isShowing())
            {
                Dialog.dismiss();
                if (ServerInterface.checkserver) {
                    try {
                        JSONObject mydata = new JSONObject(Result);
                        JSONArray a=mydata.getJSONArray("Records");
                        processResponse(a);
                        refreshView();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {//4 list error
                    CommonFunctions._messageToShow = "Network Error! Refresh or Please try again later!!";
                    CommonFunctions.sendMessageToActivity(1, MainActivity.mcontext);
                }
            }
        }
        protected Void doInBackground(Void... par)
        {
            try{
                String url = Constants.ServerApiUrl + "mobile/trip/list";
                JSONObject params = new JSONObject();
                params.put("token", tiny.getString("loginToken"));
                Result=ServerInterface.CallServerApi(params, url,55,tiny.getString("loginToken"));
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }//end get trips class

    //proccess tripdata response
    public void processResponse(JSONArray tripdata) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        Log.d(TAG, "loaded trips: " + tripdata.length());
        Log.d(TAG, "loaded trips: " + tripdata.toString());

        //delete previous data saved in local database
        db.deletePreviousData();

        try {
            for (int i=0;i<tripdata.length();i++) {
                JSONObject jsontrip = tripdata.getJSONObject(i);
                Long tripId= Long.valueOf(jsontrip.getString("id"));
                String tripDate = jsontrip.getString("tripDate");
                int numTasks=jsontrip.getInt("numTasks");
                String zipCode=jsontrip.getString("zipCode");
                String origin=jsontrip.getString("origin");
                Boolean tripstarted=jsontrip.getBoolean("started");
                Boolean tripcompleted=jsontrip.getBoolean("completed");
                String expiryDate = jsontrip.getString("expiryDate");
                if(expiryDate==null)
                    expiryDate="";

                String facilityCode=jsontrip.getString("facility");
                String tripType=jsontrip.getString("tripType");
                int status=0;
                if(tripstarted)//if trip started
                    status=1;
                if(tripcompleted)
                    status=2;//if trip completed

                db.insertTripData(tripId, tripDate, expiryDate, numTasks, zipCode, origin, status, facilityCode, tripType);
                //save tasks associated with trip
                JSONArray taskdata = jsontrip.getJSONArray("tasks");
                for (int j=0;j<taskdata.length();j++) {
                    JSONObject jsontask = taskdata.getJSONObject(j);
                    Long taskId = jsontask.getLong("id");
                    String ref = jsontask.getString("refNum");
                    long pickups = jsontask.getInt("numTasks");
                    String name = "";
                    if (jsontask.getString("name") != null && !jsontask.getString("name").equals("null"))
                        name = jsontask.getString("name");
                    String address = jsontask.getString("address");
                    String zipcode = jsontask.getString("zipCode");
                    String phone = "";
                    if (jsontask.getString("phone") != null && !jsontask.getString("phone").equals("null"))
                        phone = jsontask.getString("phone");
                    String taskType = jsontask.getString("type");
                    int pickupQty = 0;
                    String reason = "";
                    String delieveryDateTime = "";
                    try {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                        String reformattedStr = "";
                        try {
                            if (jsontask.getString("completeBy") != null && !jsontask.getString("completeBy").equals("null") && !jsontask.getString("completeBy").equals(""))
                                reformattedStr = formatter.format(fromUser.parse(jsontask.getString("completeBy")));
                        } catch (Exception e) {
                            Log.e(TAG, "error in parsing date", e);
                        }
                        delieveryDateTime = reformattedStr;//completed by datetime
                    } catch (Exception e) {
                        Log.e(TAG, "error in response", e);
                    }

                    String comments = "";
                    String taskstatus = jsontask.getString("status");
                    int tstatus = Constants.task_status_pending_code;
                    if (taskstatus.equals("CMT")) {
                        tstatus = Constants.task_status_done_code;
                    } else if (taskstatus.equals("CLD")) {
                        tstatus = Constants.task_status_failed_code;
                    } else if (taskstatus.equals("OFD")) {
                        tstatus = Constants.task_status_start_code;
                    } else if (taskstatus.equals("ARD")) {
                        tstatus = Constants.task_status_doorstep_code;
                    } else {
                        tstatus = Constants.task_status_pending_code;
                    }

                    String paymentMode = "";
                    String BasketId = "";
                    try {
                        paymentMode = jsontask.getString("paymentModeType");
                    } catch (Exception e) {
                        paymentMode = "";
                    }

                    String consigneeNumber = "";
                    try {
                        consigneeNumber = jsontask.getString("consigneeNumber");
                    } catch (Exception e) {
                        consigneeNumber = "";
                    }
                    String taskPinno="";
                    if(tripType.contains("OPK")) {
                        BasketId=jsontask.getString("superTaskId");
                        String superTaskExpectedQty =jsontask.getString("superTaskExpectedQty");
                        String superTaskStatus =jsontask.getString("superTaskStatus");
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
                            db.insertBasketData(Long.valueOf(BasketId), tripId, tripType, name, address, zipcode,
                                    Integer.parseInt(superTaskExpectedQty), ststatus);
                        }
                    }
                    String amount="";
                    db.insertTaskData(tripId, taskId, ref, pickups, name, address, zipcode, phone, taskType,
                            pickupQty, reason, delieveryDateTime,
                            comments, tstatus, paymentMode, BasketId, consigneeNumber,taskPinno,5,amount);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Not able to understand server response", e);
        }
    }*/

}
