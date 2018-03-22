package com.humaralabs.fieldrun.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.humaralabs.fieldrun.CommonFunctions;
import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.MainActivity;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.server.ServerInterface;

import org.json.JSONException;
import org.json.JSONObject;


public class FeedbackFragment extends Fragment {

    EditText email;
    EditText feedback;
    Button sendEmail;

    String EnteredEmail="";
    String EnteredFeedback="";
    TinyDB tiny;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback,container,false);
        tiny=new TinyDB(MainActivity.mcontext);
        initView(v);
        return v;
    }

    private void initView(View v){

        email=(EditText) v.findViewById(R.id.email);
        email.setText(tiny.getString("emaiId"));
        feedback=(EditText) v.findViewById(R.id.message);

        sendEmail=(Button) v.findViewById(R.id.sendEmail);
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnteredEmail=email.getText().toString();
                EnteredFeedback=feedback.getText().toString();
                if(EnteredEmail.equals(""))
                    Toast.makeText(MainActivity.mcontext, "Please Enter Your Email", Toast.LENGTH_LONG).show();
                else if(EnteredFeedback.equals(""))
                    Toast.makeText(MainActivity.mcontext, "Please Enter Your Query", Toast.LENGTH_LONG).show();
                else
                    new send_feedback().execute();
            }
        });
    }

    private ProgressDialog Dialog = null;
    //Async Class to send user feedback
    private class send_feedback extends AsyncTask<Void, Void, Void>
    {
        String Result="";
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
            if(Dialog != null && Dialog.isShowing())
            {
                Dialog.dismiss();
            }
            try {
                JSONObject data = new JSONObject(Result);
                if(data.getBoolean("status")) {
                    CommonFunctions.sendMessageToActivity(1, MainActivity.mcontext);
                    CommonFunctions._messageToShow="Feedback Successfully Sent!!";
                }
                else{
                    CommonFunctions.sendMessageToActivity(1, MainActivity.mcontext);
                    CommonFunctions._messageToShow="Please Try Again Later!!";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        protected Void doInBackground(Void... params)
        {
            try{
                String url= Constants.ServerApiUrl+"mobile/crewFeedBack";
                JSONObject data = new JSONObject();
                data.put("Token",tiny.getString("loginToken"));
                data.put("EmailId",EnteredEmail);
                data.put("Message",EnteredFeedback);
                String iemino = tiny.getString("iemino");
                Result= ServerInterface.CallServerApi(data, url,55,tiny.getString("loginToken"),iemino);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }//end send_feedback class
}
