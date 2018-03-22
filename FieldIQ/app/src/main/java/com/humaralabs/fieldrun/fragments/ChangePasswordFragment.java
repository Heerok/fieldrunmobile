package com.humaralabs.fieldrun.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.MainActivity;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.SplashActivity;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.server.ServerInterface;

import org.json.JSONObject;


public class ChangePasswordFragment extends Fragment {

    EditText newPw,confirmPw;
    Button changeButton;
    TinyDB tinydb;
    String newPwtext="";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tinydb=new TinyDB(MainActivity.mcontext);
        View v = inflater.inflate(R.layout.fragment_change_pw,container,false);
        newPw=(EditText) v.findViewById(R.id.newpw);
        confirmPw=(EditText) v.findViewById(R.id.confirmpassword);
        changeButton =(Button) v.findViewById(R.id.changebtn);

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPwtext=newPw.getText().toString();
                String confirmPwtext=confirmPw.getText().toString();

                if(!isEitherStringORNumber(newPwtext)){
                    if(newPwtext.equals(confirmPwtext)){
                        StartChangePasswordThread();
                    }
                    else{
                        Toast.makeText(MainActivity.mcontext, "Password not match!", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(MainActivity.mcontext, "Password should be alphanumeric!", Toast.LENGTH_LONG).show();
                }
            }
        });
        return v;
    }

    public static boolean isEitherStringORNumber(String value) {
        return value.matches("^[A-z]+$") || value.matches("^[0-9]*$");
    }
    Boolean AuthError=false;

    public void StartChangePasswordThread(){
        new Thread()
        {
            public void run()
            {
                AuthError=false;
                Boolean status=ChangePasswordRequest();
                if(status){
                    if(getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (Dialog != null && Dialog.isShowing()) {
                                    Dialog.dismiss();
                                }
                                AutomaticLogout();
                            }
                        });
                    }
                }
                else{//error case then read from local db
                    if(getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (Dialog != null && Dialog.isShowing()) {
                                    Dialog.dismiss();
                                }
                                if (AuthError) {
                                    AutomaticLogout();
                                }
                                else{
                                    Toast.makeText(MainActivity.mcontext, "Please try again later!", Toast.LENGTH_LONG).show();
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
        //Toast.makeText(MainActivity.mcontext, "Authentication error! Please Login Again.", Toast.LENGTH_LONG).show();
        Intent i = new Intent(MainActivity.mcontext, SplashActivity.class);
        startActivity(i);
        if(getActivity()!=null) {
            getActivity().finish();
        }
    }

    private ProgressDialog Dialog = null;
    private Boolean ChangePasswordRequest(){
        Boolean CallServer=false;
        String Result="";
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Dialog = new ProgressDialog(MainActivity.mcontext);
                    Dialog.setMessage(" Please wait...");
                    Dialog.show();
                    Dialog.setCanceledOnTouchOutside(false);
                    Dialog.setCancelable(false);
                }
            });
        }
        try{
            String url = Constants.ServerApiUrl + "resetPassword";
            if (tinydb == null)
                tinydb = new TinyDB(MainActivity.mcontext);
            String loginToken = tinydb.getString("loginToken");
            JSONObject jo = new JSONObject();
            jo.put("token",loginToken);
            jo.put("password",newPwtext);
            String iemino = tinydb.getString("iemino");
            Result = ServerInterface.CallServerApi(jo, url,180,loginToken,iemino);
            if(ServerInterface.checkserver && !Result.equals("")) {
                try {
                    JSONObject mydata = new JSONObject(Result);
                    if(mydata.getString("Message")!=null &&  mydata.getString("Message").equals("Auth Error")) {
                        AuthError = true;
                        CallServer=false;
                        return false;
                    }
                    Boolean status = mydata.getBoolean("status");
                    if(status){
                        CallServer=true;
                    }
                    else{
                        CallServer=false;
                    }
                } catch (Exception e) {//2(Exception in parse Response)
                    CallServer=false;
                    e.printStackTrace();
                }
            }
            else{
                CallServer=false;
            }
        }catch(Exception e)
        {
            CallServer=false;
            e.printStackTrace();
        }
        return CallServer;
    }



}
