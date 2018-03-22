package com.humaralabs.fieldrun;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.humaralabs.fieldrun.adapter.HosListAdapter;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.Task;
import com.humaralabs.fieldrun.server.ServerInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;


public class HosListActivity extends ActionBarActivity implements ActionBar.TabListener {
    ListView hos_list;
    private ArrayList<Task> hosList;
    DbAdapter db;
    TinyDB tb;
    String basket_server_id;
    String basket_eqty = "";
    String basket_seller_name = "";
    String basket_seller_zipcode = "";
    String basket_seller_address = "";
    Long basket_trip_id;
    String basket_trip_type = "";
    private String tripFacility="";
    private String tripExpiryDateTime="";

    private Boolean alreadyCallingThread=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hos_list);
        alreadyCallingThread=false;
        hos_list=(ListView)findViewById(R.id.hoslist);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Intent detailIntent = getIntent();

        db=new DbAdapter(HosListActivity.this);
        tb=new TinyDB(HosListActivity.this);
        hosList=new ArrayList<Task>();
        basket_seller_name = detailIntent.getExtras().get("basket_seller_name").toString();
        basket_server_id = detailIntent.getExtras().get("basket_server_id").toString();
        basket_seller_zipcode = detailIntent.getExtras().get("basket_seller_zipcode").toString();
        basket_seller_address = detailIntent.getExtras().get("basket_seller_address").toString();
        basket_trip_id = Long.valueOf(detailIntent.getExtras().get("basket_trip_id").toString());
        basket_trip_type = detailIntent.getExtras().get("basket_trip_type").toString();
        basket_eqty=detailIntent.getExtras().get("basket_eqty").toString();
        tripFacility = detailIntent.getExtras().getString("tripFacility");
        tripExpiryDateTime = detailIntent.getExtras().getString("tripExpiryDateTime");


        addTab(actionBar, "PENDING",basket_server_id);
        addTab(actionBar, "DONE", basket_server_id);
        addTab(actionBar, "FAILED", basket_server_id);
        actionBar.setSelectedNavigationItem(0);
        GPSChecker.GPSCheck(HosListActivity.this, false);

    }

    private void addTab(ActionBar actionBar, String tab,String basketid) {
        ActionBar.Tab t1 = actionBar.newTab();
        int status=0;
        if(tab.equals("PENDING"))
            status=0;
        else if(tab.equals("DONE"))
            status=1;
        else if(tab.equals("FAILED"))
            status=2;

        t1.setText(tab + " (" + db.GetTaskForParticulatTripsCount(status, basketid,"Basket") + ")");
        //t1.setText(tab);
        t1.setTag(tab);
        t1.setTabListener(this);
        actionBar.addTab(t1);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        int status=tab.getPosition();//sttus pending,done,failed
        refreshList(status, basket_server_id);
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
        GPSChecker.GPSCheck(HosListActivity.this, false);
        //start service
        CommonFunctions.StartSevice(HosListActivity.this);
        refreshList(Constants.event_status_pending_code, basket_server_id);
        refreshView();
    }


    public void refreshView() {
        //refresh tab
        ActionBar actionBar = getSupportActionBar();
        for (int i=0;i<actionBar.getTabCount();i++) {
            ActionBar.Tab tab = actionBar.getTabAt(i);
            if(i==0)
                tab.setText("pending" + " (" + db.GetTaskForParticulatTripsCount(i, basket_server_id,"Basket") + ")");
            else if(i==1)
                tab.setText("done" + " (" + db.GetTaskForParticulatTripsCount(i, basket_server_id,"Basket") + ")");
            else if(i==2)
                tab.setText("failed" + " (" + db.GetTaskForParticulatTripsCount(i, basket_server_id,"Basket") + ")");
        }
        //end refresh tab

        //refresh list
        actionBar.setSelectedNavigationItem(0);
        //end refresh list
    }

    public void refreshList(int taskstatus,String basketid) {
        hosList=db.GetTaskForParticulatTrips(taskstatus,basketid,"Basket",true);
        //Log.d(TAG, "Loaded " + taskList.size() + " tasks for tripid " + tripId);
        ListView lv_tasks = (ListView) findViewById(R.id.hoslist);

        HosListAdapter hosListAdapter = new HosListAdapter(this, hosList, R.layout.activity_hos_list_item,taskstatus);

        lv_tasks.setAdapter(hosListAdapter);
        try {
            Collections.sort(hosList);
        }
        catch(Exception e){
            //Log.e(TAG, "error in Sorting list", e);
        }
        lv_tasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = hosList.get(position);
                int tripstatus = db.getTripStatus(task.tripId);
                if (tripstatus == Constants.trip_status_active_code) {
                    int taskStatus = db.getTaskStatus(task.taskId);
                    if (taskStatus == Constants.task_status_pending_code) {
                        //Log.d("CLICK", "Starting detail");
                        Intent detailIntent = new Intent(HosListActivity.this, SubTaskDetailActivity.class);
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
                        detailIntent.putExtra("pinno", task.pinno);
                        startActivity(detailIntent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                    } else if (taskStatus == Constants.task_status_failed_code || taskStatus == Constants.task_status_done_code) {
                        //show toast
                        Toast.makeText(HosListActivity.this, "This task has been update.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(HosListActivity.this, "Please start the task first.", Toast.LENGTH_LONG).show();
                    }
                } else if (tripstatus == Constants.trip_status_pending_code) {
                    Toast.makeText(HosListActivity.this, "Trip not started yet.", Toast.LENGTH_LONG).show();
                } else if (tripstatus == Constants.trip_status_complete_code) {
                    Toast.makeText(HosListActivity.this, "Trip already completed.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
        }
        else if(id==R.id.action_refresh)
        {
            if(alreadyCallingThread==false) {
                alreadyCallingThread = true;
                StartTripActionThread();
            }
            //new SendTripEventsToServer().execute();
            //Toast.makeText(HosListActivity.this,"refresh",Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
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
                    final Boolean fetchtripstatus=Get_AllHosFromServer();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                                alreadyCallingThread=false;
                            }
                            if (AuthError) {
                                AutomaticLogout();
                            } else if (!fetchtripstatus) {
                                CommonFunctions.sendMessageToActivity(1, HosListActivity.this);
                            }
                            refreshList(Constants.event_status_pending_code, basket_server_id);
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
                                CommonFunctions.sendMessageToActivity(1, HosListActivity.this);
                            }
                        }
                    });
                }
            }
        }.start();
    }



    public void AutomaticLogout(){
        tb.putString("loginToken", "");
        tb.putString("gcmtoken", "");
        Toast.makeText(HosListActivity.this, "Authentication error! Please Login Again.", Toast.LENGTH_LONG).show();
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
                Dialog = new ProgressDialog(HosListActivity.this);
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
                if (tb == null)
                    tb = new TinyDB(MainActivity.mcontext);
                String loginToken = tb.getString("loginToken");
                JSONObject jo = db.getPendingEvents(2000, loginToken,"ALLTASK");
                JSONArray eventsUpdateArray=jo.getJSONArray("updates");
                if(eventsUpdateArray.length()>0) {
                    requestId=Integer.parseInt(jo.getString("reqId"));
                    db.insertUpdatesCount("cccallserverAttempt");
                    String iemino = tb.getString("iemino");
                    Result = ServerInterface.CallServerApi(jo, url,180,loginToken,iemino);
                    if(ServerInterface.checkserver && !Result.equals("NoEvents") && !Result.equals("")) {
                        try {
                            JSONObject mydata = new JSONObject(Result);
                            if(mydata.getString("Message")!=null &&  mydata.getString("Message").equals("Auth Error")) {
                                AuthError = true;
                                CallServerForFetchTripdata=false;
                                return false;
                            }
                            //Log.d(TAG, "bulk sync response" + mydata.toString());
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


    private Boolean Get_AllHosFromServer(){
        Boolean CallServerForFetchTripdata=false;
        String Result="";
        try{
            String url = Constants.ServerApiUrl + "mobile/superTask/list/expectedLoad";
            String loginToken = tb.getString("loginToken");
            JSONObject params = new JSONObject();
            params.put("token",loginToken);
            params.put("id",basket_server_id);
            String iemino = tb.getString("iemino");
            Result = ServerInterface.CallServerApi(params, url, 10, loginToken,iemino);
            if (ServerInterface.checkserver  && !Result.equals("")) {
                try {
                    JSONObject mydata = new JSONObject(Result);
                    if(mydata.getString("Message").equals("Auth Error")) {
                        AuthError = true;
                        CallServerForFetchTripdata=false;
                        return false;
                    }

                    String res = mydata.getString("Result");
                    Boolean status = mydata.getBoolean("status");
                    if (status && res.equals("OK")) {
                        JSONObject record=mydata.getJSONObject("Record");
                        JSONArray taskdata=record.getJSONArray("tasks");
                        processResponse(taskdata);
                        CallServerForFetchTripdata=true;
                    }
                    else{
                        CallServerForFetchTripdata=false;
                    }

                } catch (Exception e) {
                    CallServerForFetchTripdata=false;
                    e.printStackTrace();
                }
            }
        }catch(Exception e)
        {
            CallServerForFetchTripdata=false;
            e.printStackTrace();
        }
        return CallServerForFetchTripdata;
    }



    /*private ProgressDialog Dialog = null;
    private class SendTripEventsToServer extends AsyncTask<Void, Void, Void>
    {
        long activetripid=0;
        int requestId=0;
        String Result="NoEvents";
        protected void onPreExecute()
        {
            Dialog = new ProgressDialog(HosListActivity.this);
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
                        //Log.d(TAG, "bulk sync response" + mydata.toString());
                        String res1 = mydata.getString("status");
                        String res = mydata.getString("Result");
                        Boolean status=mydata.getBoolean("status");
                        if (status && res.equals("OK")) {
                            JSONObject record = mydata.getJSONObject("Record");
                            String respid = record.getString("reqid");
                            //deleting updated record
                            db.deleteUpdatedEvents(Integer.parseInt(respid), "NOTALL");
                            new callserver_async().execute();
                        } else {
                            if (requestId != 0)
                                db.updateEventStatus(Constants.event_status_pending_code, requestId);
                            CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                            CommonFunctions.sendMessageToActivity(1, HosListActivity.this);
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                            }
                        }
                    } catch (Exception e) {
                        if (requestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, requestId);
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, HosListActivity.this);
                        if (Dialog != null && Dialog.isShowing()) {
                            Dialog.dismiss();
                        }
                        e.printStackTrace();
                    }
                }
                else {
                    if(requestId!=0)
                        db.updateEventStatus(Constants.event_status_pending_code,requestId);
                    CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                    CommonFunctions.sendMessageToActivity(1, HosListActivity.this);
                    if(Dialog != null && Dialog.isShowing()) {
                        Dialog.dismiss();
                    }
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
                    if (tb == null)
                        tb = new TinyDB(HosListActivity.this);
                    String loginToken = tb.getString("loginToken");
                    JSONObject jo = db.getPendingEvents(100, loginToken,"ALLTASK");
                    JSONArray eventsUpdateArray=jo.getJSONArray("updates");
                    if(eventsUpdateArray.length()>0) {
                        requestId=Integer.parseInt(jo.getString("reqId"));
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


    //Async class for getting response data from server on start
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
                try {
                    Dialog.dismiss();
                    if (ServerInterface.checkserver) {
                        JSONObject mydata = new JSONObject(Result);
                        JSONObject record=mydata.getJSONObject("Record");
                        JSONArray taskdata=record.getJSONArray("tasks");
                        processResponse(taskdata);
                        refreshList(Constants.event_status_pending_code, basket_server_id);
                        refreshView();
                    } else {

                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        protected Void doInBackground(Void... par) {
            try {
                String url = Constants.ServerApiUrl + "mobile/superTask/list/expectedLoad";
                String loginToken = tb.getString("loginToken");
                JSONObject params = new JSONObject();
                params.put("token",loginToken);
                params.put("id",basket_server_id);
                Result = ServerInterface.CallServerApi(params, url, 10, loginToken);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }*/

    //proccess taskdata response
    public void processResponse(JSONArray taskdata) {
        try {
            //save tasks
            for (int j=0;j<taskdata.length();j++) {
                JSONObject jsontask = taskdata.getJSONObject(j);
                Long taskId = jsontask.getLong("id");
                String ref = jsontask.getString("refNum");
                String platformId = jsontask.getString("platformId");
                String itemCategory = jsontask.getString("itemCategory");
                String itemDescription = "";
                if (jsontask.getString("taskDescription") != null && !jsontask.getString("taskDescription").equals("null"))
                    itemDescription= jsontask.getString("taskDescription");

                String optionPhotocount = "";
                if (jsontask.getString("optionalImageCount") != null && !jsontask.getString("optionalImageCount").equals("null"))
                    itemDescription= jsontask.getString("optionalImageCount");

                String mandatoryPhotocount = "";
                if (jsontask.getString("mandatoryImageCount") != null && !jsontask.getString("mandatoryImageCount").equals("null"))
                    itemDescription= jsontask.getString("mandatoryImageCount");

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
                        //Log.e(TAG, "error in parsing date", e);
                    }
                    delieveryDateTime = reformattedStr;//completed by datetime
                } catch (Exception e) {
                    //Log.e(TAG, "error in response", e);
                }

                String comments = "";
                String taskstatus = jsontask.getString("status");
                int tstatus = Constants.task_status_pending_code;
                if (taskstatus.equals("CMT")) {
                    tstatus = Constants.task_status_done_code;
                } else if (taskstatus.equals("CLD")) {
                    tstatus = Constants.task_status_failed_code;
                }
                 else if (taskstatus.equals("FLD")) {
                tstatus = Constants.task_status_qc_fail_code;
                 }
                else if (taskstatus.equals("OFD")) {
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
                String consigneeName = "";
                try {
                    consigneeNumber = jsontask.getString("consigneeNumber");
                    consigneeName = jsontask.optString("contactName");
                } catch (Exception e) {
                    consigneeNumber = "";
                }
                String taskPinno="";
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
                    db.insertBasketData(Long.valueOf(BasketId), basket_trip_id, basket_trip_type, name, address, zipcode, Integer.parseInt(superTaskExpectedQty), ststatus);
                }
                String amount="";
                String codamount="";
                db.insertTaskData(basket_trip_id, taskId, ref,platformId, pickups, name, address, zipcode, phone, taskType,
                        pickupQty, reason, delieveryDateTime, comments, tstatus, paymentMode, BasketId,
                        consigneeNumber,consigneeName,taskPinno,5,amount,codamount,itemCategory,itemDescription,mandatoryPhotocount,optionPhotocount);
            }
        } catch (JSONException e) {
            //Log.e(TAG, "Not able to understand server response", e);
        }
    }
}