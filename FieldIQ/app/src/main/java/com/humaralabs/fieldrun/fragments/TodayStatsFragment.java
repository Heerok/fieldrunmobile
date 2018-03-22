package com.humaralabs.fieldrun.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.MainActivity;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.SplashActivity;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.server.ServerInterface;

import org.json.JSONObject;


public class TodayStatsFragment extends Fragment {

    TextView codamounttext;
    TextView copamounttext;
    TextView distnacetext;

    String Distance="0";
    String CodAmount="0";
    String CopAmount="0";

    TinyDB tinydb;
    DbAdapter db;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_today_stats,container,false);
        db=new DbAdapter(MainActivity.mcontext);
        tinydb=new TinyDB(MainActivity.mcontext);
        codamounttext=(TextView) v.findViewById(R.id.codamounttext);

        copamounttext=(TextView) v.findViewById(R.id.copamounttext);
        distnacetext=(TextView) v.findViewById(R.id.distnacetext);
        StartFethingTodayStatsThread();

        return v;
    }

    Boolean AuthError=false;
    public void StartFethingTodayStatsThread(){
        new Thread()
        {
            public void run()
            {
                AuthError=false;
                Boolean status=FetchTodayStatsFromServer();
                if(status){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                            }
                            codamounttext.setText(CodAmount);
                            copamounttext.setText(CopAmount);
                            distnacetext.setText(Distance);
                        }
                    });
                }
                else{//error case then read from local db
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                            }
                            if(AuthError){
                                AutomaticLogout();
                            }
                            else{
                                Toast.makeText(MainActivity.mcontext, "Something went wrong! Please try again.", Toast.LENGTH_LONG).show();
                            }
                            codamounttext.setText(CodAmount);
                            copamounttext.setText(CopAmount);
                            distnacetext.setText(Distance);
                        }
                    });
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
        getActivity().finish();
    }

    private ProgressDialog Dialog = null;
    private Boolean FetchTodayStatsFromServer(){
        Boolean CallServerForFetchTripdata=false;
        String Result="";
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog = new ProgressDialog(MainActivity.mcontext);
                Dialog.setMessage("Fetching details...");
                Dialog.show();
                Dialog.setCanceledOnTouchOutside(false);
                Dialog.setCancelable(false);
            }
        });
        try{
            String url = Constants.ServerApiUrl + "mobile/getTaskerEodInfo";
            if (tinydb == null)
                tinydb = new TinyDB(MainActivity.mcontext);

            String loginToken = tinydb.getString("loginToken");
            String iemino = tinydb.getString("iemino");
            JSONObject params = new JSONObject();
            params.put("token", loginToken);
            Result = ServerInterface.CallServerApi(params, url,180,loginToken,iemino);
            if(ServerInterface.checkserver && !Result.equals("")) {
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
                        Distance=record.getString("distance")+" KM";
                        CodAmount=record.getString("receivedAmountCod")+" Rs/-";
                        CopAmount=record.getString("receivedAmountCoP")+" Rs/-";
                        CallServerForFetchTripdata=true;
                    } else {
                        CallServerForFetchTripdata=false;
                    }
                } catch (Exception e) {//2(Exception in parse Response)
                    CallServerForFetchTripdata=false;
                    e.printStackTrace();
                }
            }
            else{
                CallServerForFetchTripdata=false;
            }
        }catch(Exception e)
        {
            CallServerForFetchTripdata=false;
            e.printStackTrace();
        }
        return CallServerForFetchTripdata;
    }
}
