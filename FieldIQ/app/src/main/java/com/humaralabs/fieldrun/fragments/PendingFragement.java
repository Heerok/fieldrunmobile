package com.humaralabs.fieldrun.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.humaralabs.fieldrun.ConnectionChecker;
import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.MainActivity;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.server.ServerInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;


public class PendingFragement extends android.support.v4.app.Fragment {
    private static final String TAG = "GPSS";
    Button btn_event;
    Button btn_signature;
    TextView pending_event_no;
    TextView Signature_no;
    int event_count;
    int signature_count;
    public static int maxUpdateCount=80;
    DbAdapter db;
    TinyDB tb;

    private ProgressDialog prgDialog;
    // Progress Dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_pendingevent, container, false);

        tb=new TinyDB(MainActivity.mcontext);
        db=new DbAdapter(MainActivity.mcontext);

         btn_event=(Button)v.findViewById(R.id.btn_pendingevent);
         btn_signature=(Button)v.findViewById(R.id.btn_signature);

         pending_event_no=(TextView)v.findViewById(R.id.pendingeventtext);
         Signature_no=(TextView)v.findViewById(R.id.signaturetext);

        refresh_event();

        btn_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        if (ConnectionChecker.isConnectingToInternet(MainActivity.mcontext,false))
                            new SendGpsEventsUpdateToServer().execute();

                        else
                            Toast.makeText(MainActivity.mcontext, "Please Connect internet", Toast.LENGTH_SHORT).show();

            }
        });
        btn_signature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(signature_count!=0)
                {*/
                if (ConnectionChecker.isConnectingToInternet(MainActivity.mcontext,false))
                    new SendImageUploadEventsToServer().execute();

                else
                {
                    Toast.makeText(MainActivity.mcontext,"Please Connect internet",Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    private void refresh_event() {
        event_count=db.getPendingEventsCount("event");
        signature_count=db.getPendingEventsCount("image");
        //if(event_count==0)
        pending_event_no.setText("" + event_count);
        Signature_no.setText("" + signature_count);

    }

    // Show Dialog Box with Progress bar
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                prgDialog = new ProgressDialog(MainActivity.mcontext);
                prgDialog.setMessage("Uploading Image Event. Please wait...");
                prgDialog.setIndeterminate(false);
                prgDialog.setMax(signature_count);
                prgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                prgDialog.setCancelable(false);
                prgDialog.show();
                return prgDialog;
            default:
                return null;
        }
    }
    //Async class for sending trip data to server
    private ProgressDialog Dialog = null;
    private class SendGpsEventsUpdateToServer extends AsyncTask<Void, Void, Void>
    {
        protected void onPreExecute()
        {
            Dialog = new ProgressDialog(MainActivity.mcontext);
            Dialog.setMessage("Please wait...");
            Dialog.show();
            Dialog.setCanceledOnTouchOutside(false);
            Dialog.setCancelable(false);
        }
        protected void onPostExecute(Void unused)
        {
            refresh_event();
            if(Dialog!=null)
                Dialog.dismiss();
        }
        protected Void doInBackground(Void... par)
        {
            try{
                String url = Constants.ServerApiUrl + "mobile/GpsEventUpdate";
                if(tb==null)
                    tb=new TinyDB(MainActivity.mcontext);
                String loginToken=tb.getString("loginToken");
                JSONObject jo = db.getPendingEvents(200,loginToken,"ALLGPS");
                JSONArray eventsUpdateArray=jo.getJSONArray("updates");
                if(eventsUpdateArray.length()>0) {
                    //db.insertUpdatesCount("bgcallserverAttempt");
                    int requestId = Integer.parseInt(jo.getString("reqId"));
                    String iemino = tb.getString("iemino");
                    String Resultdata = ServerInterface.CallServerApi(jo, url, 180, loginToken,iemino);
                    if(ServerInterface.checkserver) {
                        if (!Resultdata.equals("")) {//server response properly
                            try {
                                JSONObject mydata = new JSONObject(Resultdata);
                                Log.d(TAG, "bulk sync response" + mydata.toString());
                                String res = mydata.getString("Result");
                                Boolean status = mydata.getBoolean("status");
                                if (status && res.equals("OK")) {
                                    JSONObject record = mydata.getJSONObject("Record");
                                    //add new task if assigned i a running trip
                                    String respid = record.getString("reqid");
                                    //deleting updated record
                                    db.deleteUpdatedEvents(Integer.parseInt(respid), "NOTALL");
                                } else {
                                    if (requestId != 0)
                                        db.updateEventStatus(Constants.event_status_pending_code, requestId);

                                }
                            } catch (Exception e) {
                                if(requestId!=0)
                                    db.updateEventStatus(Constants.event_status_pending_code,requestId);
                                e.printStackTrace();
                            }
                        }
                    }
                    else {//response blank case
                        if(requestId!=0)
                            db.updateEventStatus(Constants.event_status_pending_code,requestId);
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }//end



    //Async class for uploading image to server
    private class SendImageUploadEventsToServer extends AsyncTask<String, String, String>
    {
        int ImagerequestId=0;

        String Result="";
        protected void onPreExecute()
        {
            // Shows Progress Bar Dialog and then call doInBackground method
            onCreateDialog(progress_bar_type);
        }
        protected void onPostExecute(String result)
        {
            refresh_event();
            prgDialog.setProgress(0);
            prgDialog.dismiss();
        }
        protected String doInBackground(String... par)
        {
            try{
                String url;// = Constants.ServerApiUrl + "mobile/task/uploadimage";
                if(tb==null)
                    tb=new TinyDB(MainActivity.mcontext);
                String loginToken=tb.getString("loginToken");
                for(int i=1;i<=signature_count;i++)
                { // Publish the progress which triggers onProgressUpdate method
                    JSONObject jo = db.getPendingImageUploadEvent();
                    if (jo.length() > 0)
                    {
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
                            /*if(jo.getString("filename").contains("SIGN"))
                                url=Constants.ServerApiUrl + "mobile/task/uploadSignimage";
                            else
                                url=Constants.ServerApiUrl + "mobile/task/uploadimage";
                            */
                            params.put("token", loginToken);
                            params.put("taskId", jo.getLong("taskId"));
                            params.put("reqId", jo.getString("reqId"));
                            ImagerequestId = Integer.parseInt(jo.getString("reqId"));
                            String Result = ServerInterface.UploadImageApi(new File(jo.getString("filename")), url, params,loginToken);
                            if (ServerInterface.Imagecheckserver && !Result.equals("")) {
                                try {
                                    JSONObject mydata = new JSONObject(Result);
                                    Log.d(TAG, "bulk sync response" + mydata.toString());
                                    String res = mydata.getString("Result");
                                    if (res.equals("OK")) {
                                        String respid = mydata.getString("Record");
                                        //deleting updated record
                                        db.deleteUpdatedEvents(Integer.parseInt(respid),"NOTALL");
                                        //setting progress dialog
                                        publishProgress(""+i);
                                    } else {
                                        if (ImagerequestId != 0)
                                            db.updateEventStatus(Constants.event_status_pending_code, ImagerequestId);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else
                            {
                                if (ImagerequestId != 0)
                                    db.updateEventStatus(Constants.event_status_pending_code, ImagerequestId);
                            }
                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
        // While Downloading Music File
        protected void onProgressUpdate(String... progress) {
            // Set progress percentage
            prgDialog.setProgress(Integer.parseInt(progress[0]));
        }

    }//end
}
