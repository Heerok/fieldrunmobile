package com.humaralabs.fieldrun.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.humaralabs.fieldrun.CommonFunctions;
import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.HyperLocalTripTaskListActivity;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.Task;
import com.humaralabs.fieldrun.server.ServerInterface;
import com.humaralabs.fieldrun.service.EventUpdateService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HyperTaskListAdapter extends BaseAdapter {

    private static final String TAG = "TaskList";
    private ArrayList<Task> taskList;
    private LayoutInflater inflater;
    DbAdapter db;
    TinyDB tinydb;
    Context adapterContext;
    private int resource;
    public int taskStatus=0;
    String taskId;
    String tripId;
    String taskref="";
    SimpleDateFormat foramtter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat foramtterTime = new SimpleDateFormat("HH:mm a");
    public HyperTaskListAdapter(Context context, ArrayList<Task> tasks, int resource, int status) {
        if(db==null)
            db = new DbAdapter(context);
        if(tinydb==null)
            tinydb=new TinyDB(context);
        adapterContext=context;
        taskStatus=status;
        this.taskList = tasks;
        this.resource = resource;
        this.inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    private View createViewFromResource(final int position, View convertView,ViewGroup parent) {
        Task task = taskList.get(position);
        View v;
        if (convertView == null) {
            v = inflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }

        RelativeLayout priorityLayout = (RelativeLayout) v.findViewById(R.id.priorityrl);
        RelativeLayout startLayout = (RelativeLayout) v.findViewById(R.id.startLy);
        ImageView imageview=(ImageView) v.findViewById(R.id.pickup);
        TextView numtaskTextView = (TextView) v.findViewById(R.id.numtask);
        TextView taskrefTextView = (TextView) v.findViewById(R.id.ref);
        TextView nameTextView = (TextView) v.findViewById(R.id.name);
        TextView dateTextView = (TextView) v.findViewById(R.id.date);
        TextView zipcodeTextView = (TextView) v.findViewById(R.id.zipcode);
        TextView addressTextView = (TextView) v.findViewById(R.id.address);
        TextView tripstatusTextView = (TextView) v.findViewById(R.id.tripstatus);
        TextView taskstartedstatusTextView = (TextView) v.findViewById(R.id.taskstartedstatus);


        String type = task.taskType;
        String tag = "Pick";
        switch (type) {
            case "HYP-PICKUP":
                tag = "Pick";
                imageview.setImageResource(R.drawable.pickup);
                break;
            case "HYP-DELIVERY":
                tag = "Del";
                imageview.setImageResource(R.drawable.delivery);
                break;
        }

        //setting number of task
        String numTasks=String.valueOf(task.pickups);
        if(numTasks==null || numTasks.equals("") || numTasks.equals("null") || numTasks.equals("NA")) {
            numtaskTextView.setVisibility(View.GONE);
        }
        else {
            numtaskTextView.setVisibility(View.VISIBLE);
            numtaskTextView.setText(numTasks+" "+tag);
        }

        //setting task ref
        taskref=String.valueOf(task.ref);
        if(taskref==null || taskref.equals("") || taskref.equals("null") || taskref.equals("NA")) {
            taskrefTextView.setVisibility(View.GONE);
        }
        else {
            taskrefTextView.setVisibility(View.VISIBLE);
            taskrefTextView.setText(taskref);
        }

        //setting name
        String name=String.valueOf(task.consignee_name);
        if(name==null || name.equals("") || name.equals("null") || name.equals("NA")) {
            nameTextView.setVisibility(View.GONE);
        }
        else {
            nameTextView.setVisibility(View.VISIBLE);
            nameTextView.setText(name);
        }

        //setting date
        String delieveryDateTime=String.valueOf(task.delieveryDateTime);
        if(delieveryDateTime==null || delieveryDateTime.equals("") || delieveryDateTime.equals("null") || delieveryDateTime.equals("NA")) {
            dateTextView.setVisibility(View.GONE);
        }
        else {
            dateTextView.setVisibility(View.VISIBLE);
            String reformattedStr = "";
            try {
                reformattedStr = foramtterTime.format(foramtter.parse(delieveryDateTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
            dateTextView.setText(reformattedStr);
        }

        //setting zipcode
        String zipcode=String.valueOf(task.zipCode);
        if(zipcode==null || zipcode.equals("") || zipcode.equals("null") || zipcode.equals("NA")) {
            zipcodeTextView.setVisibility(View.GONE);
        }
        else {
            zipcodeTextView.setVisibility(View.VISIBLE);
            zipcodeTextView.setText(zipcode);
        }

        //setting address
        String address=String.valueOf(task.address);
        if(address==null || address.equals("") || address.equals("null") || address.equals("NA")) {
            addressTextView.setVisibility(View.GONE);
        }
        else {
            addressTextView.setVisibility(View.VISIBLE);
            addressTextView.setText(address);
        }


        try {
            if(taskStatus==0) {
                if(!taskList.get(position).delieveryDateTime.equals("")) {
                    String dateStart = GetCurrentDAteTime();
                    String dateStop = taskList.get(position).delieveryDateTime;

                    Date d1 = null;
                    Date d2 = null;
                    try {
                        d1 = foramtter.parse(dateStart);
                        d2 = foramtter.parse(dateStop);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // Get msec from each, and subtract.
                    long diff = d2.getTime() - d1.getTime();
                    long diffMinutes = diff / (60 * 1000);
                    long diffHours = diff / (60 * 60 * 1000);
                    if (diffMinutes <= 0) {
                        priorityLayout.setVisibility(View.VISIBLE);
                        priorityLayout.setBackgroundResource(R.drawable.rectagleredbox);
                        tripstatusTextView.setText("High");
                    } else if (diffMinutes > 0 && diffMinutes <= 60) {  //tasks which are due in next one hour
                        priorityLayout.setVisibility(View.VISIBLE);
                        priorityLayout.setBackgroundResource(R.drawable.rectagleredbox);
                        tripstatusTextView.setText("High");
                    } else if (diffMinutes > 60 && diffMinutes <= 120) { //from next 1-2 hours
                        priorityLayout.setVisibility(View.VISIBLE);
                        priorityLayout.setBackgroundResource(R.drawable.rectaglebox);
                        tripstatusTextView.setText("Medium");
                    } else {
                        priorityLayout.setVisibility(View.GONE);
                    }
                }
                else {
                    priorityLayout.setVisibility(View.GONE);
                }
            }
            else {
                priorityLayout.setVisibility(View.GONE);
            }

        }
        catch(Exception e){
            Log.e(TAG, "error in Sorting list", e);
        }

        String taskstatus = taskList.get(position).status.toString();
        String task_type = taskList.get(position).taskType.toString();
        //set the task start button
        if (tinydb == null)
            tinydb = new TinyDB(adapterContext);
        String SMSButtons=tinydb.getString("SMSButtons");
        if (Integer.parseInt(taskstatus)== Constants.task_status_pending_code) {
            startLayout.setVisibility(View.VISIBLE);
        }
        else{//set the hide task start button
            startLayout.setVisibility(View.GONE);
        }

        if (Integer.parseInt(taskstatus)== Constants.task_status_start_code) {
            taskstartedstatusTextView.setVisibility(View.VISIBLE);
        }
        else{
            taskstartedstatusTextView.setVisibility(View.GONE);
        }

        startLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskId = taskList.get(position).taskId.toString();
                tripId = taskList.get(position).tripId.toString();
                String tasktype = taskList.get(position).taskType.toString();
                //new Get_ResponseFromServer().execute();
                //multilayout.setVisibility(View.GONE);

                int tripstatus = db.getTripStatus(taskList.get(position).tripId);
                if(tripstatus==Constants.event_status_pending_code)
                    Toast.makeText(v.getContext(), "Trip not started yet.", Toast.LENGTH_LONG).show();
                else{
                    new AlertDialog.Builder(adapterContext)
                            .setIcon(R.drawable.outerlogo)
                            .setTitle("Start task")
                            .setMessage("Are you sure you want to start this task?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    taskStatus = Constants.task_status_start_code;
                                    StartSendingTripThread();
                                }

                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                    //showdialog(taskId, tripId, tasktype);
            }


        });

        return v;
    }


    TextView tripnoTextView;
    TextView taskidTextView;
    TextView taskTypetextView;
    Button ok,cancle;
    private void showdialog(final String taskId, final String tripId, final String tasktype) {
        final Dialog dialog = new Dialog(adapterContext);
        //hiding default title bar of dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_start_popup);
        dialog.getWindow().getAttributes().width= WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        tripnoTextView=(TextView)dialog.getWindow().findViewById(R.id.tripno);
        taskidTextView =(TextView)dialog.getWindow().findViewById(R.id.taskid);
        taskTypetextView=(TextView)dialog.getWindow().findViewById(R.id.tasktype);
        ok=(Button)dialog.getWindow().findViewById(R.id.task_button_ok);
        cancle=(Button)dialog.getWindow().findViewById(R.id.task_button_can);
        dialog.setCanceledOnTouchOutside(false);


        if(tripId!=null && !tripId.equals(""))
            tripnoTextView.setText("TripId : "+tripId);
        if(taskId!=null && !taskId.equals(""))
            taskidTextView.setText("TaskId : "+taskId);
        if(tasktype!=null&&!tasktype.equals(""))
            taskTypetextView.setText("TaskType : "+tasktype);

        dialog.show(); //to show dialog box
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                taskStatus = Constants.task_status_start_code;
                StartSendingTripThread();
                //new Get_ResponseFromServer().execute();
            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }



    Boolean AuthError=false;
    public void StartSendingTripThread(){
        new Thread()
        {
            public void run()
            {
                AuthError=false;
                Boolean status=SendTripEventToServer();
                if(status){
                    ((HyperLocalTripTaskListActivity) adapterContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                            }
                            if (AuthError) {
                                ((HyperLocalTripTaskListActivity) adapterContext).AutomaticLogout();
                            } else {
                                ((HyperLocalTripTaskListActivity) adapterContext).refreshList(Constants.event_status_pending_code, taskref);
                            }
                        }
                    });
                }
                else{//error case then read from local db
                    ((HyperLocalTripTaskListActivity) adapterContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                            }
                            if (AuthError) {
                                ((HyperLocalTripTaskListActivity) adapterContext).AutomaticLogout();
                            }
                        }
                    });
                }
            }
        }.start();
    }



    private ProgressDialog Dialog = null;
    private Boolean SendTripEventToServer(){
        Boolean CallServerForSendingEventdata=true;
        try {
            ((HyperLocalTripTaskListActivity) adapterContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Dialog = new ProgressDialog(adapterContext);
                    Dialog.setMessage("Please wait...");
                    Dialog.show();
                    Dialog.setCanceledOnTouchOutside(false);
                    Dialog.setCancelable(false);
                }
            });
            String Result="";
            String url = Constants.ServerApiUrl + "mobile/TaskEventUpdate";
            JSONObject params = new JSONObject();
            if (tinydb == null)
                tinydb = new TinyDB(adapterContext);

            Location location= EventUpdateService.mlocation;
            Double latitude = (double) 0, longitude = (double) 0, altitude = (double) 0;
            Float accuracy= (float) 0, bearing= (float) 0, speed= (float) 0;
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                altitude=location.getAltitude();
                accuracy=location.getAccuracy();
                bearing=location.getBearing();
                speed=location.getSpeed();
            }

            JSONObject jo = new JSONObject();
            JSONArray allparams = new JSONArray();
            JSONObject taskUpdateparams = new JSONObject();
            taskUpdateparams.put("taskId", taskId);
            taskUpdateparams.put("status", "OFD");
            taskUpdateparams.put("comments", "");
            taskUpdateparams.put("qty", 0);
            taskUpdateparams.put("reason", "");
            taskUpdateparams.put("rescheduleDateTime", "");
            taskUpdateparams.put("tripId", tripId);
            taskUpdateparams.put("eventType", "taskupdate");
            taskUpdateparams.put("latitude", latitude);
            taskUpdateparams.put("longitude", longitude);
            taskUpdateparams.put("accuracy", accuracy);
            taskUpdateparams.put("altitude", altitude);
            taskUpdateparams.put("bearing", bearing);
            taskUpdateparams.put("speed", speed);
            taskUpdateparams.put("battery", 0);
            taskUpdateparams.put("ts", CommonFunctions.getCurrentTime());
            allparams.put(taskUpdateparams);
            String loginToken = tinydb.getString("loginToken");
            jo.put("token", loginToken);
            jo.put("push", "yes");
            jo.put("updates", allparams);
            jo.put("reqId", taskId);

            JSONArray eventsUpdateArray = jo.getJSONArray("updates");
            if (eventsUpdateArray.length() > 0) {
                String iemino = tinydb.getString("iemino");
                Result = ServerInterface.CallServerApi(jo, url, 10,loginToken,iemino);
                if (ServerInterface.checkserver && !Result.equals("")) {
                    try {
                        JSONObject mydata = new JSONObject(Result);
                        if(mydata.getString("Message")!=null &&  mydata.getString("Message").equals("Auth Error")) {
                            AuthError = true;
                            CallServerForSendingEventdata=false;
                            return false;
                        }
                        String res = mydata.getString("Result");
                        Boolean status = mydata.getBoolean("status");
                        if (status && res.equals("OK")) {
                            db.updateTaskStatus(Constants.task_status_start_code, Long.parseLong(taskId));
                        }else{
                            saveTaskActionEvent("Start",tripId,taskId);
                        }
                        CallServerForSendingEventdata=true;
                    } catch (Exception e) {
                        CallServerForSendingEventdata=true;
                        saveTaskActionEvent("Start",tripId,taskId);
                        e.printStackTrace();
                    }
                } else {
                    saveTaskActionEvent("Start",tripId,taskId);
                    CallServerForSendingEventdata=true;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return CallServerForSendingEventdata;
    }

    private class Get_ResponseFromServer extends AsyncTask<Void, Void, Void>
    {
        String Result="";
        protected void onPreExecute()
        {
            //do nothing
            Dialog = new ProgressDialog(adapterContext);
            Dialog.setMessage("Please wait...");
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
                        String res = mydata.getString("Result");
                        Boolean status = mydata.getBoolean("status");
                        if (status && res.equals("OK")) {
                            db.updateTaskStatus(Constants.task_status_start_code, Long.parseLong(taskId));
                        }else{
                            saveTaskActionEvent("Start",tripId,taskId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    saveTaskActionEvent("Start",tripId,taskId);
                    //CommonFunctions._messageToShow = "Network Error! Refresh or Please try again later!!";
                    //CommonFunctions.sendMessageToActivity(1, MainActivity.mcontext);
                }
                ((HyperLocalTripTaskListActivity)adapterContext).refreshList(Constants.event_status_pending_code,taskref);
            }
        }
        protected Void doInBackground(Void... par) {
            try {
                String url = Constants.ServerApiUrl + "mobile/TaskEventUpdate";
                JSONObject params = new JSONObject();
                //params.put("token", tinydb.getString("loginToken"));
                if (tinydb == null)
                    tinydb = new TinyDB(adapterContext);

                Location location= EventUpdateService.mlocation;
                Double latitude = (double) 0, longitude = (double) 0, altitude = (double) 0;
                Float accuracy= (float) 0, bearing= (float) 0, speed= (float) 0;
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    altitude=location.getAltitude();
                    accuracy=location.getAccuracy();
                    bearing=location.getBearing();
                    speed=location.getSpeed();
                }

                JSONObject jo = new JSONObject();
                JSONArray allparams = new JSONArray();
                JSONObject taskUpdateparams = new JSONObject();
                taskUpdateparams.put("taskId", taskId);
                taskUpdateparams.put("status", "OFD");
                taskUpdateparams.put("comments", "");
                taskUpdateparams.put("qty", 0);
                taskUpdateparams.put("reason", "");
                taskUpdateparams.put("rescheduleDateTime", "");
                taskUpdateparams.put("tripId", tripId);
                taskUpdateparams.put("eventType", "taskupdate");
                taskUpdateparams.put("latitude", latitude);
                taskUpdateparams.put("longitude", longitude);
                taskUpdateparams.put("accuracy", accuracy);
                taskUpdateparams.put("altitude", altitude);
                taskUpdateparams.put("bearing", bearing);
                taskUpdateparams.put("speed", speed);
                taskUpdateparams.put("battery", 0);
                taskUpdateparams.put("ts", CommonFunctions.getCurrentTime());
                allparams.put(taskUpdateparams);
                String loginToken = tinydb.getString("loginToken");
                jo.put("token", loginToken);
                jo.put("push", "yes");
                jo.put("updates", allparams);
                jo.put("reqId", taskId);

                JSONArray eventsUpdateArray = jo.getJSONArray("updates");
                if (eventsUpdateArray.length() > 0) {
                    String iemino = tinydb.getString("iemino");
                    Result = ServerInterface.CallServerApi(jo, url, 10,loginToken,iemino);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }//end get trips class

    public long saveTaskActionEvent(String action_type, String tripId, String taskId) {
        Long res = null;
        try {
            String tstatus = "";
            if (action_type.equals("Start")) {
                tstatus = "OFD";
            }

            //update task table status from pending to complete
            JSONObject taskUpdateparams = new JSONObject();
            taskUpdateparams.put("taskId", taskId);
            taskUpdateparams.put("status", tstatus);
            taskUpdateparams.put("comments", "");
            taskUpdateparams.put("qty", 0);
            taskUpdateparams.put("reason", "");
            taskUpdateparams.put("rescheduleDateTime", "");


            res = db.insertFieldEvent(tripId, "", "taskupdate", EventUpdateService.mlocation, taskUpdateparams.toString(), adapterContext);
            if (res != 0 && action_type.equals("Start")) {
                db.updateTaskStatus(Constants.task_status_start_code, Long.parseLong(taskId));
            }
            /*else if (res != 0 && action_type.equals("Doorstep")) {
                Constants.taskactivestatus = true;
                db.updateTaskStatus(Constants.task_status_doorstep_code, Long.parseLong(taskId));
            }*/

        } catch (Exception e) {
        }
        return res;
    }

    public String GetCurrentDAteTime(){
        Calendar cal1 = Calendar.getInstance(); // creates calendar
        cal1.setTime(new Date()); // sets calendar time/date
        Date b= cal1.getTime();
        String dateStart = foramtter.format(b);
        return dateStart;
    }
}
