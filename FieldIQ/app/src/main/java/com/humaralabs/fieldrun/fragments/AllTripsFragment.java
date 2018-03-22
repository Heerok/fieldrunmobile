package com.humaralabs.fieldrun.fragments;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.humaralabs.fieldrun.CommonFunctions;
import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.HyperLocalBaksetListActivity;
import com.humaralabs.fieldrun.MainActivity;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.SplashActivity;
import com.humaralabs.fieldrun.TripTaskListActivity;
import com.humaralabs.fieldrun.adapter.CustomAllTripsAdapter;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.Trip;
import com.humaralabs.fieldrun.server.ServerInterface;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Admin on 04-06-2015.
 */
public class AllTripsFragment extends Fragment {
    private static final String TAG ="AllTrips";
    TinyDB tinydb;
    DbAdapter db;
    ArrayList<Trip> AllTripList;
    ListView lview;
    TextView notrips;
    ImageView im;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_all_trips,container,false);
        im=(ImageView) v.findViewById(R.id.appbanner);
        tinydb=new TinyDB(MainActivity.mcontext);
        db=new DbAdapter(MainActivity.mcontext);

        AllTripList=new ArrayList<Trip>();
        lview=(ListView) v.findViewById(R.id.listView1);
        notrips=(TextView) v.findViewById(R.id.notrips);

        db.deleteUpdateCount();

        if(!tinydb.getString("appbanner").equals("")) {
            Picasso.with(MainActivity.mcontext).load(Constants.ServerUrl + "/" + tinydb.getString("appbanner")).into(target);
        }
        if(MainActivity.syncOrNot){
            StartFethingTripThread();
        }
        else {
            //checking trip avialble for today in sqllite database
            int TripCount = db.GetTodayTripsCountFromDataBase("All");
            if (TripCount == 0) {//if not availble then get all trips for today from server
                StartFethingTripThread();
                //new SendTripEventsToServer().execute();
            } else {//if availble then get all trips for today from database
                ArrayList<Trip> triplist = db.GetTripsFromDatabase("All");
                SetTripDataInList(triplist);
            }
        }
        return v;
    }

    private CompanyLogoTarget target = new CompanyLogoTarget();
    private class CompanyLogoTarget implements Target {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            //imageLoadered = true;
            int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                im.setBackgroundDrawable(new BitmapDrawable(bitmap));
            } else {
                im.setBackground(new BitmapDrawable(getResources(), bitmap));
            }
            //im.setImageBitmap(bitmap);
            //bitmapOfImage = bitmap;
            Log.e("App","Success to load company logo in onBitmapLoaded method");
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            //imageLoadered = false;
            //imgLogo.setBackgroundResource(R.drawable.black_border);
            //imgLogo.setImageResource(R.drawable.building);
            Log.e("App","Failed to load company logo in onBitmapFailed method");
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            //imgLogo.setImageResource(R.drawable.loading);
            Log.e("App","Prepare to load company logo in onPrepareLoad method");
        }

    }

    Boolean AuthError=false;
    public void StartFethingTripThread(){
        new Thread()
        {
            public void run()
            {
                AuthError=false;
                Boolean status=SendTripEventsToServer();
                if(status){
                    final Boolean fetchtripstatus=Get_AllTripsFromServer();
                    MainActivity.alreadyLoadingAllTripFragment = false;
                    if(getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (Dialog != null && Dialog.isShowing()) {
                                    Dialog.dismiss();
                                }
                                if (AuthError) {
                                    AutomaticLogout();
                                } else if (!fetchtripstatus) {//error case then read from local db
                                    ArrayList<Trip> triplist = db.GetTripsFromDatabase("All");
                                    SetTripDataInList(triplist);
                                }
                            }
                        });
                    }
                }
                else{//error case then read from local db
                    MainActivity.alreadyLoadingAllTripFragment = false;
                    db.insertUpdatesCount("rccallserverFail");
                    if(getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (Dialog != null && Dialog.isShowing()) {
                                    Dialog.dismiss();
                                }
                                if (AuthError) {
                                    AutomaticLogout();
                                } else {
                                    ArrayList<Trip> triplist = db.GetTripsFromDatabase("All");
                                    SetTripDataInList(triplist);
                                }
                            }
                        });
                    }
                }
            }
        }.start();
    }

    private void AutomaticLogout(){
        tinydb.putString("loginToken", "");
        tinydb.putString("gcmtoken", "");
        Toast.makeText(MainActivity.mcontext, "Authentication error! Please Login Again.", Toast.LENGTH_LONG).show();
        Intent i = new Intent(MainActivity.mcontext, SplashActivity.class);
        startActivity(i);
        if(getActivity()!=null) {
            getActivity().finish();
        }
    }

    private ProgressDialog Dialog = null;
    private Boolean SendTripEventsToServer(){
        Boolean CallServerForFetchTripdata=false;
        long activetripid=0;
        int requestId=0;
        String Result="NoEvents";
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Dialog = new ProgressDialog(MainActivity.mcontext);
                    Dialog.setMessage("Refreshing All Trips!! Please wait...");
                    Dialog.show();
                    Dialog.setCanceledOnTouchOutside(false);
                    Dialog.setCancelable(false);
                }
            });
        }
        try{
            activetripid=db.getActiveTripId();
            if(activetripid!=0) {
                String url = Constants.ServerApiUrl + "mobile/TaskEventUpdate";
                if (tinydb == null)
                    tinydb = new TinyDB(MainActivity.mcontext);
                String loginToken = tinydb.getString("loginToken");
                JSONObject jo = db.getPendingEvents(2000, loginToken,"ALLTASK");
                JSONArray eventsUpdateArray=jo.getJSONArray("updates");
                if(eventsUpdateArray.length()>0) {
                    requestId=Integer.parseInt(jo.getString("reqId"));
                    db.insertUpdatesCount("rccallserverAttempt");
                    String iemino = tinydb.getString("iemino");
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
                                db.insertUpdatesCount("rccallserverSuccess");
                                CallServerForFetchTripdata=true;
                            } else {
                                if (requestId != 0)
                                    db.updateEventStatus(Constants.event_status_pending_code, requestId);
                                //CommonFunctions._messageToShow = mydata.getString("Message");
                                CallServerForFetchTripdata=false;
                            }
                        } catch (Exception e) {//2(Exception in parse Response)
                            if (requestId != 0)
                                db.updateEventStatus(Constants.event_status_pending_code, requestId);
                            //CommonFunctions._messageToShow = "Invalid Response! Refresh or Please try again later!!";
                            CallServerForFetchTripdata=false;
                            e.printStackTrace();
                        }
                    }
                    else {
                        if (requestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, requestId);
                        //CommonFunctions._messageToShow = "Network Error! Refresh or Please try again later!!";
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

    private Boolean Get_AllTripsFromServer(){
        Boolean CallServerForFetchTripdata=false;
        String Result="";
        try{
            String url = Constants.ServerApiUrl + "mobile/trip/list";
            JSONObject params = new JSONObject();
            params.put("token", tinydb.getString("loginToken"));
            String iemino = tinydb.getString("iemino");
            Result=ServerInterface.CallServerApi(params, url,55,tinydb.getString("loginToken"),iemino);
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
                       JSONArray a=mydata.getJSONArray("Records");
                        processResponse(a);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SetTripDataInList(AllTripList);
                                }
                            });
                        }
                        CallServerForFetchTripdata=true;
                        if (Dialog != null && Dialog.isShowing()) {
                            Dialog.dismiss();
                        }
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
            e.printStackTrace();
        }
        return CallServerForFetchTripdata;
    }

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
                Long tripId = Long.valueOf(jsontrip.getString("id"));
                String tripDate = jsontrip.getString("tripDate");
                int numTasks = jsontrip.getInt("numTasks");
                String zipCode=jsontrip.getString("zipCode");
                String origin = jsontrip.getString("origin");
                Boolean tripstarted=jsontrip.getBoolean("started");
                Boolean tripcompleted = jsontrip.getBoolean("completed");
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

                db.insertTripData(tripId,tripDate,expiryDate,numTasks,zipCode,origin,status,facilityCode,tripType);
                Trip tripobj=new Trip(tripId,tripDate,numTasks,zipCode,origin,status,expiryDate,facilityCode,tripType);
                AllTripList.add(tripobj);
                //save tasks associated with trip
                JSONArray taskdata = jsontrip.getJSONArray("tasks");
                for (int j=0;j<taskdata.length();j++) {
                    JSONObject jsontask = taskdata.getJSONObject(j);
                    Long taskId = jsontask.getLong("id");
                    String ref = jsontask.getString("refNum");

                    String platformId ;
                    String itemCategory="";
                    String itemDescription = "";
                    String mandatoryPhotocount = "";
                    if (jsontask.getString("mandatoryImageCount") != null && !jsontask.getString("mandatoryImageCount").equals("null"))
                        mandatoryPhotocount= jsontask.getString("mandatoryImageCount");
                    String optionPhotocount = "";
                    if (jsontask.getString("optionalImageCount") != null && !jsontask.getString("optionalImageCount").equals("null"))
                        optionPhotocount= jsontask.getString("optionalImageCount");

                   if (jsontask.getString("taskDescription") != null && !jsontask.getString("taskDescription").equals("null"))
                        itemDescription= jsontask.getString("taskDescription");
                    if (jsontask.getString("platformId") != null && !jsontask.getString("platformId").equals("null"))
                        platformId= jsontask.getString("platformId");
                    else
                        platformId ="All";

                    if (jsontask.getString("itemCategory") != null && !jsontask.getString("itemCategory").equals("null"))
                        itemCategory= jsontask.getString("itemCategory");


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
                    if (jsontask.getString("reason") != null && !jsontask.getString("reason").equals("null"))
                        reason = jsontask.getString("reason");
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
                    } else if (taskstatus.equals("FLD")) {
                        tstatus = Constants.task_status_qc_fail_code;
                    } else if (taskstatus.equals("OFD") || taskstatus.equals("RCH")) {
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
                    String amount="";
                    String codamount="";
                    try {
                        consigneeNumber = jsontask.getString("consigneeNumber");
                        consigneeName = jsontask.getString("contactName");
                        amount=jsontask.getString("amount");
                        codamount=jsontask.getString("codAmount");
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
                        if (BasketId != null && BasketId != "null" && !BasketId.equals("")) {
                            db.insertBasketData(Long.valueOf(BasketId), tripId, tripType, name, address, zipcode, Integer.parseInt(superTaskExpectedQty), ststatus);
                        }
                    }
                    else if(tripType.contains("HYP")){
                        db.insertHyperBasketData(ref, tripId);
                    }

                    db.insertTaskData(tripId, taskId, ref,platformId, pickups, name, address, zipcode, phone, taskType,
                            pickupQty, reason, delieveryDateTime, comments, tstatus, paymentMode, BasketId,
                            consigneeNumber,consigneeName,taskPinno,5,amount,codamount,itemCategory,itemDescription,mandatoryPhotocount,optionPhotocount);
                }

                try {
                    JSONArray SuperTaskdata = jsontrip.getJSONArray("superTasks");
                    if(SuperTaskdata!=null && !SuperTaskdata.equals("null")) {
                        for (int j = 0; j < SuperTaskdata.length(); j++) {
                            JSONObject jsonsupertask = SuperTaskdata.getJSONObject(j);
                            String bid = jsonsupertask.getString("id");
                            Long tid = jsonsupertask.getLong("tripId");
                            String ttype = jsonsupertask.getString("type");
                            String cname = jsonsupertask.getString("customerName");
                            String add = jsonsupertask.getString("address");
                            String zip = jsonsupertask.getString("zipCode");
                            String qty = jsonsupertask.getString("qty");
                            int sstatus = 0;
                            String superTaskStatus = jsonsupertask.getString("status");
                            if (superTaskStatus.equals("CMT")) {
                                sstatus = Constants.basket_status_complete_code;
                            } else if (superTaskStatus.equals("CLD")) {
                                sstatus = Constants.basket_status_failed_code;
                            } else {
                                sstatus = Constants.basket_status_pending_code;
                            }
                            db.insertBasketData(Long.valueOf(bid), tid, ttype, cname, add, zip, Integer.parseInt(qty), sstatus);
                        }
                    }
                }
                catch (Exception e){

                }

                tinydb.putString("GcmTripMessage","NO");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Not able to understand server response", e);
        }
        catch (Exception e) {
            Log.e(TAG, "Not able to understand server response", e);
        }
    }

    //seting trip data in a listview
    private void SetTripDataInList(final ArrayList<Trip> triplist){
        if(triplist.size()>0) {
            notrips.setVisibility(View.GONE);
            CustomAllTripsAdapter adapter = new CustomAllTripsAdapter(MainActivity.mcontext, triplist);
            lview.setAdapter(adapter);
            lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Long activeTripId=db.getActiveTripId();
                    if(activeTripId==0){
                        if (triplist.get(position).status == Constants.trip_status_complete_code)
                            Toast.makeText(MainActivity.mcontext, "Trip " + triplist.get(position).tripId + " has been completed", Toast.LENGTH_LONG).show();
                        else if (triplist.get(position).numTasks != 0)
                            OpenTaskListActivity(triplist,position);
                        else if (triplist.get(position).trip_type.contains("OPK"))
                            OpenTaskListActivity(triplist,position);
                        else if (triplist.get(position).numTasks == 0)
                            Toast.makeText(MainActivity.mcontext, "No task Assigned For " + triplist.get(position).tripId + " Trip", Toast.LENGTH_LONG).show();
                    }
                    else{
                        long selectedTripId=triplist.get(position).tripId;
                        if(activeTripId==selectedTripId)
                            OpenTaskListActivity(triplist,position);
                        else
                            Toast.makeText(MainActivity.mcontext, "Please Complete Currently Active Trip", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else{
            notrips.setVisibility(View.VISIBLE);
        }
        Long activeTripId = db.getActiveTripId();
        String TextToDisplay="";
        if(activeTripId==0){
            Log.d(TAG, "NO TRIPS in db!");
            TextToDisplay="No Active Trip";
        }
        else{
            for(int i=0; i<triplist.size(); i++) {
                if(triplist.get(i).status==Constants.trip_status_active_code) {
                    TextToDisplay = "Active Trip Details " + triplist.get(i).tripId + " " + triplist.get(i).origin;
                    break;
                }
            }
        }
        CommonFunctions.showActiveNotification(MainActivity.mcontext, TextToDisplay);
    }

    //open task activity for selected trip
    private void OpenTaskListActivity(ArrayList<Trip> triplist,int position){
        Intent i;
        //if(triplist.get(position).trip_type.contains("OPK"))
           // i = new Intent(MainActivity.mcontext, OneshipBaksetListActivity.class);
        if(triplist.get(position).trip_type.contains("HYP"))
            i = new Intent(MainActivity.mcontext, HyperLocalBaksetListActivity.class);
        else
            i = new Intent(MainActivity.mcontext, TripTaskListActivity.class);

        i.putExtra("trip_id", triplist.get(position).tripId);
        i.putExtra("origin", triplist.get(position).origin);
        i.putExtra("facility", triplist.get(position).trip_facility);
        i.putExtra("tripExpiryDateTime", triplist.get(position).tripExpiryDateTime);
        i.putExtra("trip_type", triplist.get(position).trip_type);
        startActivity(i);
        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }



    //Async class to send field events data to server
    /*private class SendTripEventsToServer extends AsyncTask<Void, Void, Void>
    {
        long activetripid=0;
        int requestId=0;
        String Result="NoEvents";
        protected void onPreExecute()
        {
            Dialog = new ProgressDialog(MainActivity.mcontext);
            Dialog.setMessage("Refreshing All Trips!! Please wait...");
            Dialog.show();
            Dialog.setCanceledOnTouchOutside(false);
            Dialog.setCancelable(false);
        }
        protected void onPostExecute(Void unused)
        {
            if(activetripid!=0) {
                if(Result.equals("NoEvents")){
                    new Get_AllTripsFromServer().execute();
                }
                else if(ServerInterface.checkserver && !Result.equals("")) {
                    try {
                        JSONObject mydata = new JSONObject(Result);
                        Log.d(TAG, "bulk sync response" + mydata.toString());
                        String res = mydata.getString("Result");
                        Boolean status = mydata.getBoolean("status");
                        if (status && res.equals("OK")) {
                            JSONObject record = mydata.getJSONObject("Record");
                            String respid = record.getString("reqid");
                            //deleting updated record
                            db.deleteUpdatedEvents(Integer.parseInt(respid), "NOTALL");
                            db.insertUpdatesCount("rccallserverSuccess");
                            new Get_AllTripsFromServer().execute();
                        } else {
                            if (requestId != 0)
                                db.updateEventStatus(Constants.event_status_pending_code, requestId);
                            CommonFunctions._messageToShow = "Network Error! Refresh or Please try again later!!";
                            CommonFunctions.sendMessageToActivity(1, MainActivity.mcontext);
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                            }
                            db.insertUpdatesCount("rccallserverFail");
                        }
                    } catch (Exception e) {//2(Exception in parse Response)
                        if (requestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, requestId);
                        CommonFunctions._messageToShow = "Network Error! Refresh or Please try again later!!";
                        CommonFunctions.sendMessageToActivity(1, MainActivity.mcontext);
                        if (Dialog != null && Dialog.isShowing()) {
                            Dialog.dismiss();
                        }
                        db.insertUpdatesCount("rccallserverFail");
                        e.printStackTrace();
                    }
                }
                else {
                    if(requestId!=0)//3 call server exception server not responding
                        db.updateEventStatus(Constants.event_status_pending_code,requestId);
                    CommonFunctions._messageToShow = "Network Error! Refresh or Please try again later!!";
                    CommonFunctions.sendMessageToActivity(1, MainActivity.mcontext);
                    if(Dialog != null && Dialog.isShowing()) {
                        Dialog.dismiss();
                    }
                    db.insertUpdatesCount("rccallserverFail");
                }
            }
            else{
                db.deleteUpdatedEvents(0,"DATE");
                new Get_AllTripsFromServer().execute();
            }
        }
        protected Void doInBackground(Void... par)
        {
            try{
                activetripid=db.getActiveTripId();
                if(activetripid!=0) {
                    String url = Constants.ServerApiUrl + "mobile/TaskEventUpdate";
                    if (tinydb == null)
                        tinydb = new TinyDB(MainActivity.mcontext);
                    String loginToken = tinydb.getString("loginToken");
                    JSONObject jo = db.getPendingEvents(2000, loginToken,"ALLTASK");
                    JSONArray eventsUpdateArray=jo.getJSONArray("updates");
                    if(eventsUpdateArray.length()>0) {
                        requestId=Integer.parseInt(jo.getString("reqId"));
                        db.insertUpdatesCount("rccallserverAttempt");
                        String iemino = tinydb.getString("iemino");
                        Result = ServerInterface.CallServerApi(jo, url,180,loginToken,iemino);
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }//end

    //Async class for getting trips data from server
    private class Get_AllTripsFromServer extends AsyncTask<Void, Void, Void>
    {
        String Result="";
        protected void onPreExecute()
        {
            //do nothing
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
                        SetTripDataInList(AllTripList);
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
                params.put("token", tinydb.getString("loginToken"));
                String iemino = tinydb.getString("iemino");
                Result=ServerInterface.CallServerApi(params, url,55,tinydb.getString("loginToken"),iemino);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }*///end get trips class

}
