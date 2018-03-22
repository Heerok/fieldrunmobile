package com.humaralabs.fieldrun;


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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.humaralabs.fieldrun.adapter.HyperTaskListAdapter;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.Task;
import com.humaralabs.fieldrun.datastructure.Trip;
import com.humaralabs.fieldrun.server.ServerInterface;
import com.humaralabs.fieldrun.service.EventUpdateService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class HyperLocalTripTaskListActivity extends ActionBarActivity implements ActionBar.TabListener{
    private static final String TAG = "TRIPTASKLIST";
    private ProgressDialog dialog;
    private ArrayList<Task> taskList;
    private Trip trip;
    private String hyper_basket_ref_no="";
    private long tripId;
    private String tripOrigin="";
    private String tripFacility="";
    private String tripExpiryDateTime="";
    private int tripStatus;
    Button button_trip_action;
    DbAdapter db;
    TinyDB tiny;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Intent ii=getIntent();
        hyper_basket_ref_no=ii.getExtras().getString("hyper_basket_ref_no");
        tripId = ii.getExtras().getLong("trip_id");
        tripOrigin = ii.getExtras().getString("origin");
        tripFacility = ii.getExtras().getString("facility");
        tripExpiryDateTime = ii.getExtras().getString("tripExpiryDateTime");

        db=new DbAdapter(HyperLocalTripTaskListActivity.this);
        tiny=new TinyDB(HyperLocalTripTaskListActivity.this);
        taskList=new ArrayList<Task>();

        addTab(actionBar, "PENDING", tripId);
        addTab(actionBar, "DONE", tripId);
        addTab(actionBar, "FAILED", tripId);
        actionBar.setSelectedNavigationItem(0);
        setTripActionButton();
        GPSChecker.GPSCheck(HyperLocalTripTaskListActivity.this, false);
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

        t1.setText(tab + " (" + db.GetTaskForParticulatTripsCount(status, hyper_basket_ref_no, "HyperBasket") + ")");
        //t1.setText(tab);
        t1.setTag(tab);
        t1.setTabListener(this);
        actionBar.addTab(t1);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, "tab : " + tab.getText());
        int status=tab.getPosition();//sttus pending,done,failed
        refreshList(status, hyper_basket_ref_no);
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
        GPSChecker.GPSCheck(HyperLocalTripTaskListActivity.this, false);
        //start service
        CommonFunctions.StartSevice(HyperLocalTripTaskListActivity.this);
        refreshList(Constants.event_status_pending_code, hyper_basket_ref_no);
        refreshView();
        Log.d(TAG, "TTL resumed");
    }


    public void refreshView() {
        //refresh tab
        ActionBar actionBar = getSupportActionBar();
        for (int i=0;i<actionBar.getTabCount();i++) {
            ActionBar.Tab tab = actionBar.getTabAt(i);
            if(i==0)
                tab.setText("pending" + " (" + db.GetTaskForParticulatTripsCount(i,hyper_basket_ref_no,"HyperBasket") + ")");
            else if(i==1)
                tab.setText("done" + " (" + db.GetTaskForParticulatTripsCount(i,hyper_basket_ref_no,"HyperBasketOther") + ")");
            else if(i==2)
                tab.setText("failed" + " (" + db.GetTaskForParticulatTripsCount(i,hyper_basket_ref_no,"HyperBasketOther") + ")");
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
        CommonFunctions.showActiveNotification(HyperLocalTripTaskListActivity.this, TextToDisplay);
    }


    public void AutomaticLogout(){
        tiny.putString("loginToken", "");
        tiny.putString("gcmtoken", "");
        Toast.makeText(HyperLocalTripTaskListActivity.this, "Authentication error! Please Login Again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void refreshList(int taskstatus,String taskref) {
        final Boolean showDeleiveryButtons=db.CheckPickupDoneORNot(tripId,taskref);
        taskList=db.GetTaskForParticulatTrips(taskstatus, taskref,"HyperBasket",showDeleiveryButtons);
        Log.d(TAG, "Loaded " + taskList.size() + " tasks for tripid " + tripId);
        ListView lv_tasks = (ListView) findViewById(R.id.tasklist);

        HyperTaskListAdapter taskListAdapter = new HyperTaskListAdapter(this, taskList, R.layout.activity_task_list_item,taskstatus);
        lv_tasks.setAdapter(taskListAdapter);
        try {
            Collections.sort(taskList);
        }
        catch(Exception e){
            Log.e(TAG, "error in Sorting list", e);
        }
        lv_tasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (tiny == null)
                    tiny = new TinyDB(HyperLocalTripTaskListActivity.this);
                Task task = taskList.get(position);
                int tripstatus = db.getTripStatus(task.tripId);
                if (tripstatus == Constants.trip_status_active_code) {
                    int taskStatus = db.getTaskStatus(task.taskId);
                    String SMSButtons = tiny.getString("SMSButtons1");
                    Intent detailIntent = new Intent(HyperLocalTripTaskListActivity.this, HyperLocalTaskDetailActivity.class);
                    detailIntent.putExtra("codamount", task.codamount);
                    detailIntent.putExtra("amount", task.amount);
                    detailIntent.putExtra("tripId", task.tripId);
                    detailIntent.putExtra("tripExpiryDateTime", tripExpiryDateTime);
                    detailIntent.putExtra("taskId", task.taskId);
                    detailIntent.putExtra("name", task.name);
                    detailIntent.putExtra("ref", task.ref);
                    detailIntent.putExtra("pickups", task.pickups);
                    detailIntent.putExtra("address", task.address);
                    detailIntent.putExtra("zipCode", task.zipCode);
                    detailIntent.putExtra("phone", task.phone);
                    detailIntent.putExtra("taskType", task.taskType);
                    detailIntent.putExtra("pickupQty", task.pickupQty);
                    detailIntent.putExtra("reason", task.reason);
                    detailIntent.putExtra("delieveryDateTime", task.delieveryDateTime);
                    detailIntent.putExtra("comments", task.comments);
                    detailIntent.putExtra("status", task.status);
                    detailIntent.putExtra("tripFacility", tripFacility);
                    detailIntent.putExtra("payment_mode", task.payment_mode);
                    detailIntent.putExtra("consigneeNumber", task.consignee_number);
                    detailIntent.putExtra("consigneeName", task.consignee_name);
                    if (taskStatus == Constants.task_status_start_code || Constants.task_status_doorstep_code == taskStatus) {
                        Log.d("CLICK", "Starting detail");
                        detailIntent.putExtra("showDoneButtonn", "YES");
                    } else if (taskStatus == Constants.task_status_failed_code || taskStatus == Constants.task_status_done_code) {
                        //show toast
                        detailIntent.putExtra("showDoneButtonn", "NO");
                        //Toast.makeText(HyperLocalTripTaskListActivity.this, "This task has been update.", Toast.LENGTH_LONG).show();
                    }
                    else if (taskStatus == Constants.task_status_pending_code &&  !showDeleiveryButtons && task.taskType.equals("HYP-DELIVERY")) {
                        //show toast
                        detailIntent.putExtra("showDoneButtonn", "NO");
                        //Toast.makeText(HyperLocalTripTaskListActivity.this, "Please complete pickup task first.", Toast.LENGTH_LONG).show();
                    }
                    else {
                        detailIntent.putExtra("showDoneButtonn", "NO");
                        //Toast.makeText(HyperLocalTripTaskListActivity.this, "Please start the task first.", Toast.LENGTH_LONG).show();
                    }
                    startActivity(detailIntent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

                } else if (tripstatus == Constants.trip_status_pending_code) {
                    Toast.makeText(HyperLocalTripTaskListActivity.this, "Trip not started yet.", Toast.LENGTH_LONG).show();
                } else if (tripstatus == Constants.trip_status_complete_code) {
                    Toast.makeText(HyperLocalTripTaskListActivity.this, "Trip already completed.", Toast.LENGTH_LONG).show();
            }
            }
        });
    }

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
        button_trip_action = (Button) findViewById(R.id.button_trip_action);
        button_trip_action.setVisibility(View.GONE);
        /*tripStatus=db.getTripStatus(tripId);

        if (tripStatus == Constants.trip_status_pending_code) {//status 0 means pending
            button_trip_action.setVisibility(View.VISIBLE);
            button_trip_action.setText("Start Trip");
        } else if (tripStatus == Constants.trip_status_active_code) {//status 1 means active
            button_trip_action.setText("Trip Complete");
            //checking all task has been done or not if yes then show trip complete button
            int pendingTaskCount = db.GetTaskForParticulatTripsCount(Constants.task_status_pending_code, String.valueOf(tripId),"Task");
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
                tripStatus = db.getTripStatus(tripId);
                new SendTripEventsToServer().execute();
            }
        });*/
    }

    private ProgressDialog Dialog = null;
    private class SendTripEventsToServer extends AsyncTask<Void, Void, Void>
    {
        long activetripid=0;
        int requestId=0;
        String Result="NoEvents";
        protected void onPreExecute()
        {
            Dialog = new ProgressDialog(HyperLocalTripTaskListActivity.this);
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
                            CommonFunctions.sendMessageToActivity(1, HyperLocalTripTaskListActivity.this);
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                            }
                            db.insertUpdatesCount("cccallserverFail");
                        }
                    } catch (Exception e) {
                        if (requestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, requestId);
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, HyperLocalTripTaskListActivity.this);
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
                    CommonFunctions.sendMessageToActivity(1,  HyperLocalTripTaskListActivity.this);
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
                        tiny = new TinyDB(HyperLocalTripTaskListActivity.this);
                    String loginToken = tiny.getString("loginToken");
                    JSONObject jo = db.getPendingEvents(100, loginToken,"ALLTASK");
                    JSONArray eventsUpdateArray=jo.getJSONArray("updates");
                    if(eventsUpdateArray.length()>0) {
                        String iemino = tiny.getString("iemino");
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
                            CommonFunctions.sendMessageToActivity(1, HyperLocalTripTaskListActivity.this);
                        }
                    } catch (Exception e) {
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, HyperLocalTripTaskListActivity.this);
                        e.printStackTrace();
                    }
                } else {
                    CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                    CommonFunctions.sendMessageToActivity(1, HyperLocalTripTaskListActivity.this);
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
    }//end get trips class

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
}
