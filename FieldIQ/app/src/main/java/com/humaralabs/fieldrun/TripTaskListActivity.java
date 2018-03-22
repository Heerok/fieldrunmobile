package com.humaralabs.fieldrun;
import android.app.Dialog;
import android.app.ProgressDialog;

import android.content.Intent;
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

import com.humaralabs.fieldrun.adapter.TaskListAdapter;
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

import static com.humaralabs.fieldrun.Constants.task_status_qc_fail_code;

public class TripTaskListActivity extends ActionBarActivity implements ActionBar.TabListener {
    private static final String TAG = "TRIPTASKLIST";
    private ProgressDialog dialog;
    private ArrayList<Task> taskList;
    private Trip trip;
    private String tripId;
    private String tripOrigin = "";
    private String tripFacility = "";
    private String tripExpiryDateTime = "";
    private String tripType = "";
    private int tripStatus;
    Button button_trip_action;
    DbAdapter db;
    TinyDB tiny;
    String captureKM;
    String StartKM = "";
    String EndKM = "";
    private Boolean alreadyCallingThread = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        alreadyCallingThread = false;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Intent ii = getIntent();
        tripId = String.valueOf(ii.getExtras().getLong("trip_id"));
        tripOrigin = ii.getExtras().getString("origin");
        tripFacility = ii.getExtras().getString("facility");
        tripExpiryDateTime = ii.getExtras().getString("tripExpiryDateTime");
        tripType = ii.getExtras().getString("trip_type");

        db = new DbAdapter(TripTaskListActivity.this);
        tiny = new TinyDB(TripTaskListActivity.this);
        taskList = new ArrayList<Task>();

        addTab(actionBar, "PENDING", tripId);
        addTab(actionBar, "DONE", tripId);
        addTab(actionBar, "FAILED", tripId);
        if(tripType.equals("RPK"))
            addTab(actionBar, "QC FAIL", tripId);
        actionBar.setSelectedNavigationItem(0);
        setTripActionButton();
        GPSChecker.GPSCheck(TripTaskListActivity.this, false);
    }


    private void addTab(ActionBar actionBar, String tab, String tripid) {
        ActionBar.Tab t1 = actionBar.newTab();
        int status = 0;
        if (tab.equals("PENDING"))
            status = 0;
        else if (tab.equals("DONE"))
            status = 1;
        else if (tab.equals("FAILED"))
            status = 2;
        else if (tab.equals("QC FAIL"))
            status = 5;
        t1.setText(tab + " (" + db.GetTaskForParticulatTripsCount(status, tripid, "Task") + ")");
        //t1.setText(tab);
        t1.setTag(tab);
        t1.setTabListener(this);
        actionBar.addTab(t1);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, "tab : " + tab.getText());
        int status = tab.getPosition();//sttus pending,done,failed
        refreshList(status==3?5:status, tripId);
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
        GPSChecker.GPSCheck(TripTaskListActivity.this, false);
        //start service
        CommonFunctions.StartSevice(TripTaskListActivity.this);
        refreshList(Constants.event_status_pending_code, tripId);
        refreshView();
        Log.d(TAG, "TTL resumed");
    }


    public void refreshView() {
        //refresh tab
        ActionBar actionBar = getSupportActionBar();
        for (int i = 0; i < actionBar.getTabCount(); i++) {
            ActionBar.Tab tab = actionBar.getTabAt(i);
            if (i == 0)
                tab.setText("pending" + " (" + db.GetTaskForParticulatTripsCount(i, tripId, "Task") + ")");
            else if (i == 1)
                tab.setText("done" + " (" + db.GetTaskForParticulatTripsCount(i, tripId, "Task") + ")");
            else if (i == 2)
                tab.setText("failed" + " (" + db.GetTaskForParticulatTripsCount(i, tripId, "Task") + ")");
            else if (i == 3 && tripType.equals("RPK"))
                tab.setText("qc fail" + " (" + db.GetTaskForParticulatTripsCount(task_status_qc_fail_code, tripId, "Task") + ")");
        }
        //end refresh tab

        //refresh list
        actionBar.setSelectedNavigationItem(0);
        //end refresh list

        //refresh trip action button
        setTripActionButton();
        //end refresh trip action button

        Long activeTripId = db.getActiveTripId();
        String TextToDisplay = "";
        if (activeTripId == 0) {
            Log.d(TAG, "NO TRIPS in db!");
            TextToDisplay = "No Active Trip";
        } else {
            TextToDisplay = "Active Trip Details " + tripId + " " + tripOrigin;
        }
        CommonFunctions.showActiveNotification(TripTaskListActivity.this, TextToDisplay);
    }

    public void refreshList(int taskstatus, String tripId) {
        taskList = db.GetTaskForParticulatTrips(taskstatus, tripId, "Task", true);
        Log.d(TAG, "Loaded " + taskList.size() + " tasks for tripid " + tripId);
        ListView lv_tasks = (ListView) findViewById(R.id.tasklist);

        TaskListAdapter taskListAdapter = new TaskListAdapter(this, taskList, R.layout.activity_task_list_item, taskstatus);

        lv_tasks.setAdapter(taskListAdapter);
        try {
            Collections.sort(taskList);
        } catch (Exception e) {
            Log.e(TAG, "error in Sorting list", e);
        }
        lv_tasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (tiny == null)
                    tiny = new TinyDB(TripTaskListActivity.this);
                    Task task = taskList.get(position);
                    int tripstatus = db.getTripStatus(task.tripId);
                    if (tripstatus == Constants.trip_status_active_code) {
                    int taskStatus = db.getTaskStatus(task.taskId);
                        String SMSButtons = tiny.getString("SMSButtons");
                             if (taskStatus == Constants.task_status_start_code || (taskStatus == Constants.task_status_pending_code && !SMSButtons.contains(task.taskType))) {
                                Log.d("CLICK", "Starting detail");
                                Intent detailIntent = new Intent(TripTaskListActivity.this, TaskDetailActivity.class);
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
                                detailIntent.putExtra("codamount", task.codamount);
                                detailIntent.putExtra("platformId", task.platformId);
                                detailIntent.putExtra("category", task.itemCategory);
                                detailIntent.putExtra("description", task.itemDescription);
                                detailIntent.putExtra("mandatoryPhotocount", task.mandatoryPhotocount);
                                detailIntent.putExtra("optionPhotocount", task.optionPhotocount);
                                detailIntent.putExtra("consigneeNumber", task.consignee_number);
                                startActivity(detailIntent);
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                            } else if (taskStatus == Constants.task_status_failed_code || taskStatus == Constants.task_status_done_code) {
                                //show toast
                                Toast.makeText(TripTaskListActivity.this, "This task has been update.", Toast.LENGTH_LONG).show();}
                             else if (taskStatus == Constants.task_status_qc_fail_code) {
                                 //show toast
                                 Toast.makeText(TripTaskListActivity.this, "This task has been failed in QC.", Toast.LENGTH_LONG).show();}
                         else {
                            Toast.makeText(TripTaskListActivity.this, "Please start the task first.", Toast.LENGTH_LONG).show();
                        }
                } else if (tripstatus == Constants.trip_status_pending_code) {
                    Toast.makeText(TripTaskListActivity.this, "Trip not started yet.", Toast.LENGTH_LONG).show();
                } else if (tripstatus == Constants.trip_status_complete_code) {
                    Toast.makeText(TripTaskListActivity.this, "Trip already completed.", Toast.LENGTH_LONG).show();
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
        tripStatus = db.getTripStatus(Long.valueOf(tripId));
        button_trip_action = (Button) findViewById(R.id.button_trip_action);
        if (tripStatus == Constants.trip_status_pending_code) {//status 0 means pending
            button_trip_action.setVisibility(View.VISIBLE);
            button_trip_action.setText("Start Trip");
        } else if (tripStatus == Constants.trip_status_active_code) {//status 1 means active
            button_trip_action.setText("Trip Complete");
            //checking all task has been done or not if yes then show trip complete button
            int pendingTaskCount = db.GetTaskForParticulatTripsCount(Constants.task_status_pending_code, tripId, "Task");
            if (pendingTaskCount > 0)
                button_trip_action.setVisibility(View.GONE);
            else
                button_trip_action.setVisibility(View.VISIBLE);
        } else if (tripStatus == Constants.trip_status_complete_code) {//status 2 means complete
            button_trip_action.setVisibility(View.GONE);
        }
        button_trip_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyCallingThread == false) {

                    tripStatus = db.getTripStatus(Long.valueOf(tripId));
                    captureKM = tiny.getString("captureKM");
                    if (captureKM.contains(tripType)) {
                        showKmDialog();
                    } else {
                        alreadyCallingThread = true;
                        StartTripActionThread();
                    }

                }
                //new SendTripEventsToServer().execute();
            }
        });
    }

    Button ok;
    EditText edt_km;
    String km;

    public void showKmDialog() {
        //creating dialog object
        final android.app.Dialog dil = new Dialog(TripTaskListActivity.this);
        //hiding default title bar of dialog
        dil.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dil.setContentView(R.layout.capture_km_popup);
        dil.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        dil.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dil.setCanceledOnTouchOutside(false);
        edt_km = (EditText) dil.findViewById(R.id.km);
        ok = (Button) dil.findViewById(R.id.button_ok);
        dil.show(); //to show dialog box
        if (tripStatus == Constants.trip_status_active_code) {
            edt_km.setHint("Enter End KM.");
        }
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                km = edt_km.getText().toString();
                if (edt_km.getHint().toString().equals("Enter Start KM.")) {
                    StartKM = km;
                    if (StartKM.isEmpty()) {
                        Toast.makeText(TripTaskListActivity.this, "Please Enter Start KM.", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        tiny.putString("StartKM", StartKM);
                        StartTripActionThread();
                        dil.dismiss();
                    }
                } else if (edt_km.getHint().toString().equals("Enter End KM.")) {
                    EndKM = km;
                    String startkm = tiny.getString("StartKM");
                    if (EndKM.isEmpty()) {
                        Toast.makeText(TripTaskListActivity.this, "Please Enter End KM.", Toast.LENGTH_LONG).show();
                    } else if (Integer.parseInt(startkm) > Integer.parseInt(EndKM)) {
                        Toast.makeText(TripTaskListActivity.this, "Enter valid End KM.End KM should be greater than start KM.", Toast.LENGTH_LONG).show();
                    } else {
                        StartTripActionThread();
                        dil.dismiss();
                    }
                }
            }
        });

    }

    Boolean AuthError = false;

    public void StartTripActionThread() {
        new Thread() {
            public void run() {
                AuthError = false;
                Boolean status = SendTripEventsToServer();
                if (status) {
                    final Boolean fetchtripstatus = callserverForTripStartOrComplete();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                                alreadyCallingThread = false;
                            }
                            if (AuthError) {
                                AutomaticLogout();
                            } else if (!fetchtripstatus) {//error case then read from local db
                                CommonFunctions.sendMessageToActivity(1, TripTaskListActivity.this);
                            }
                            refreshView();
                        }
                    });
                } else {//error case then read from local db
                    db.insertUpdatesCount("cccallserverFail");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                                alreadyCallingThread = false;
                            }
                            if (AuthError) {
                                AutomaticLogout();
                            } else {
                                CommonFunctions.sendMessageToActivity(1, TripTaskListActivity.this);
                            }
                        }
                    });
                }
            }
        }.start();
    }

    public void AutomaticLogout() {
        tiny.putString("loginToken", "");
        tiny.putString("gcmtoken", "");
        Toast.makeText(TripTaskListActivity.this, "Authentication error! Please Login Again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private ProgressDialog Dialog = null;

    private Boolean SendTripEventsToServer() {
        Boolean CallServerForFetchTripdata = false;
        long activetripid = 0;
        int requestId = 0;
        String Result = "NoEvents";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog = new ProgressDialog(TripTaskListActivity.this);
                Dialog.setMessage("Please wait...");
                Dialog.show();
                Dialog.setCanceledOnTouchOutside(false);
                Dialog.setCancelable(false);
            }
        });
        try {
            activetripid = db.getActiveTripId();
            if (activetripid != 0) {
                String url = Constants.ServerApiUrl + "mobile/TaskEventUpdate";
                if (tiny == null)
                    tiny = new TinyDB(MainActivity.mcontext);
                String loginToken = tiny.getString("loginToken");
                JSONObject jo = db.getPendingEvents(2000, loginToken, "ALLTASK");
                JSONArray eventsUpdateArray = jo.getJSONArray("updates");
                if (eventsUpdateArray.length() > 0) {
                    requestId = Integer.parseInt(jo.getString("reqId"));
                    db.insertUpdatesCount("cccallserverAttempt");
                    String iemino = tiny.getString("iemino");
                    Result = ServerInterface.CallServerApi(jo, url, 180, loginToken, iemino);
                    if (ServerInterface.checkserver && !Result.equals("NoEvents") && !Result.equals("")) {
                        try {
                            JSONObject mydata = new JSONObject(Result);
                            if (mydata.getString("Message") != null && mydata.getString("Message").equals("Auth Error")) {
                                AuthError = true;
                                CallServerForFetchTripdata = false;
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
                                CallServerForFetchTripdata = true;
                            } else {
                                if (requestId != 0)
                                    db.updateEventStatus(Constants.event_status_pending_code, requestId);
                                CommonFunctions._messageToShow = mydata.getString("Message");
                                CallServerForFetchTripdata = false;
                            }
                        } catch (Exception e) {//2(Exception in parse Response)
                            if (requestId != 0)
                                db.updateEventStatus(Constants.event_status_pending_code, requestId);
                            CommonFunctions._messageToShow = "Invalid Response! Refresh or Please try again later!!";
                            CallServerForFetchTripdata = false;
                            e.printStackTrace();
                        }
                    } else {
                        if (requestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, requestId);
                        CommonFunctions._messageToShow = "Network Error! Refresh or Please try again later!!";
                        CallServerForFetchTripdata = false;
                    }
                } else {
                    CallServerForFetchTripdata = true;
                }
            } else {
                db.deleteUpdatedEvents(0, "DATE");
                CallServerForFetchTripdata = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CallServerForFetchTripdata;
    }


    private Boolean callserverForTripStartOrComplete() {
        Boolean CallServerForTripStartOrComplete = false;
        String Result = "";
        if (tripStatus == Constants.trip_status_pending_code)
            Result = StartTrip();
        else if (tripStatus == Constants.trip_status_active_code)
            Result = CompleteTrip();

        if (ServerInterface.checkserver && !Result.equals("")) {
            JSONObject mydata = null;
            try {
                mydata = new JSONObject(Result);
                if (mydata.getString("Message").equals("Auth Error")) {
                    AuthError = true;
                    CallServerForTripStartOrComplete = false;
                    return false;
                }
                String res = mydata.getString("Result");
                Boolean status = mydata.getBoolean("status");
                if (status && res.equals("OK")) {
                    CallServerForTripStartOrComplete = true;
                    if (tripStatus == Constants.trip_status_pending_code)
                        db.updateTripStatus(Constants.trip_status_active_code, Long.valueOf(tripId));
                    else if (tripStatus == Constants.trip_status_active_code) {
                        MainActivity.needToRefresh = true;
                        db.updateTripStatus(Constants.trip_status_complete_code, Long.valueOf(tripId));
                        finish();
                        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                    }
                } else {
                    String msg = mydata.getString("Message");
                    CommonFunctions._messageToShow = msg;
                    CallServerForTripStartOrComplete = false;
                }
            } catch (JSONException e) {
                CallServerForTripStartOrComplete = false;
                CommonFunctions._messageToShow = "Invalid Response! Refresh or Please try again later!!";
                e.printStackTrace();
            }
        } else {
            CommonFunctions._messageToShow = "Connection Error! Please check your internet!!";
            CallServerForTripStartOrComplete = false;
        }
        return CallServerForTripStartOrComplete;
    }

    private String StartTrip() {
        String Response = "";
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
            params.put("startKM", StartKM);
            params.put("reqid", tripId);
            String iemino = tiny.getString("iemino");
            Response = ServerInterface.CallServerApi(params, uri, 55, tiny.getString("loginToken"), iemino);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response;
    }

    private String CompleteTrip() {
        String Response = "";
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
            params.put("endKM", EndKM);
            params.put("reqid", tripId);
            String iemino = tiny.getString("iemino");
            Response = ServerInterface.CallServerApi(params, uri, 55, tiny.getString("loginToken"), iemino);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Response;
    }

/*    private class SendTripEventsToServer extends AsyncTask<Void, Void, Void>
    {
        long activetripid=0;
        int requestId=0;
        String Result="NoEvents";
        protected void onPreExecute()
        {
            Dialog = new ProgressDialog(TripTaskListActivity.this);
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
                            db.deleteUpdatedEvents(Integer.parseInt(respid), "NOTALL");
                            db.insertUpdatesCount("cccallserverSuccess");
                            new callserver_async().execute();
                        } else {
                            if (requestId != 0)
                                db.updateEventStatus(Constants.event_status_pending_code, requestId);
                            CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                            CommonFunctions.sendMessageToActivity(1, TripTaskListActivity.this);
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                            }
                            db.insertUpdatesCount("cccallserverFail");
                        }
                    } catch (Exception e) {
                        if (requestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, requestId);
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, TripTaskListActivity.this);
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
                    CommonFunctions.sendMessageToActivity(1,  TripTaskListActivity.this);
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
                        tiny = new TinyDB(TripTaskListActivity.this);
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
    }*///end get trips class



    /*private class callserver_async extends AsyncTask<Void, Void, Void>
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
                            CommonFunctions.sendMessageToActivity(1, TripTaskListActivity.this);
                        }
                    } catch (Exception e) {
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, TripTaskListActivity.this);
                        e.printStackTrace();
                    }
                } else {
                    CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                    CommonFunctions.sendMessageToActivity(1, TripTaskListActivity.this);
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
