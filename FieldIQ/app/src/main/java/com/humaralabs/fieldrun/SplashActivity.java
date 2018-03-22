package com.humaralabs.fieldrun;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.server.ServerInterface;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";
    String EnteredUsername="";
    String EnteredPassword="";
    RelativeLayout rllogin;
    EditText usernameEditText;
    EditText passwordEditText;
    Button loginButton;
    TextView servermsg;
    ImageView _progressBarImageView,logoimage;

    ConnectionChecker concheck;

    boolean isInternet;

    TinyDB tinydb;
    DbAdapter db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        db=new DbAdapter(this);
        tinydb=new TinyDB(this);
        concheck=new ConnectionChecker(this);
        isInternet=concheck.isConnectingToInternet(SplashActivity.this,false);
        rllogin =(RelativeLayout) findViewById(R.id.rllogin);
        _progressBarImageView = (ImageView) findViewById(R.id.image_view);
        servermsg=(TextView) findViewById(R.id.servermsg);
        usernameEditText=(EditText) findViewById(R.id.username);
        passwordEditText=(EditText) findViewById(R.id.password);
        loginButton=(Button) findViewById(R.id.loginbtn);
        logoimage =(ImageView) findViewById(R.id.img);
        TextView appnm=(TextView) findViewById(R.id.appname);
        if(!tinydb.getString("appname").equals("")) {
            appnm.setText(tinydb.getString("appname"));
        }
        if(!tinydb.getString("applogo").equals("")) {
            Picasso.with(SplashActivity.this).load(Constants.ServerUrl + "/" + tinydb.getString("applogo"))
                    .placeholder(R.drawable.outerlogo).into(logoimage);
        }
        else if(Constants.ServerUrl.equals(Constants.taskmasterurl))
        {
            logoimage.setImageResource(R.drawable.taskmaster);
            appnm.setText(tinydb.getString("Task Master"));
        }


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EnteredUsername=usernameEditText.getText().toString();
                EnteredPassword=passwordEditText.getText().toString();
                if(EnteredUsername.equals(""))
                    Toast.makeText(SplashActivity.this, "Please enter your username", Toast.LENGTH_LONG).show();
                else if(EnteredPassword.equals(""))
                    Toast.makeText(SplashActivity.this, "Please enter your password", Toast.LENGTH_LONG).show();
                else{
                    //show animated truck loader
                    _progressBarImageView.setVisibility(View.VISIBLE);
                    _progressBarImageView.setBackgroundResource(R.drawable.splashanim);
                    AnimationDrawable _frameAnimation  = (AnimationDrawable) _progressBarImageView.getBackground();
                    _frameAnimation.start();
                    servermsg.setVisibility(View.VISIBLE);

                    //disable login form element
                    usernameEditText.setEnabled(false);
                    passwordEditText.setEnabled(false);
                    rllogin.setEnabled(false);
                    loginButton.setEnabled(false);

                    //start login thread
                    StartLoginThread("",false);//passing blank bcoz dont have token yet
                }
            }
        });


        String loginToken=tinydb.getString("loginToken");
        if(loginToken.equals("")) {//ask for username and password
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // change UI elements here
                                rllogin.setVisibility(View.VISIBLE);
                                Animation fadeInAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.twin);
                                rllogin.startAnimation(fadeInAnimation);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
        else{
            _progressBarImageView.setVisibility(View.VISIBLE);
            _progressBarImageView.setBackgroundResource(R.drawable.splashanim);
            AnimationDrawable _frameAnimation  = (AnimationDrawable) _progressBarImageView.getBackground();
            _frameAnimation.start();
            servermsg.setVisibility(View.VISIBLE);
            StartLoginThread(loginToken,false);
        }
    }

    public int checkgpsv(){
        int status=0;
        try {
            status=GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        } catch (Exception e) {
            status=0;
            e.printStackTrace();
        }
        if(status==0){
            status=5;
        }
        return status;
    }

    public void StartLoginThread(final String token,final Boolean ReloginStatus){
        if(isInternet) {
        Thread rthread = new Thread() {
            @Override
            public void run() {
                try {
                    LoginUser(token,ReloginStatus);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        rthread.start();
        }
        else
        {
            checkInternetPopup();
        }
    }

    public void LoginUser(String token,Boolean ReloginStatus){

        try {
            String mobileTime=GetCurrentDAteTime();
            String uri;
            String Serverurl=Constants.ServerUrl;
            JSONObject params = new JSONObject();
            if (token.equals("")) {
                uri = Constants.ServerUrl + "/mobileLogin";
                params.put("username", EnteredUsername);
                tinydb.putString("user_name", EnteredUsername);
                params.put("password", EnteredPassword);
                params.put("mobileTime", mobileTime);
                Log.d(TAG, "calling u/p login with " + uri);
            } else {
                params.put("token", token);
                params.put("mobileTime", mobileTime);
                uri = Constants.ServerApiUrl+ "tokenlogin";
                Log.d(TAG, "calling token login with " + uri);
            }

            TelephonyManager tm = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);
            String Iemi = tm.getDeviceId();

            tinydb.putString("iemino", Iemi);

            InstanceID instanceID = InstanceID.getInstance(this);
            String Gcmtoken = null;
            try {
                Gcmtoken = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(!tinydb.getString("gcmtoken").equals(Gcmtoken)) {
                params.put("userGcmType","Google");
                params.put("userGcmId",Gcmtoken);
            }

            params.put("relogin",ReloginStatus);
            params.put("appname", "fieldiq");
            params.put("versionname", BuildConfig.VERSION_NAME);
            params.put("versioncode", BuildConfig.VERSION_CODE);
            params.put("gpsVersion",checkgpsv());

            String Response=ServerInterface.CallServerApi(params,uri,55,token,Iemi);

                if (ServerInterface.checkserver) {
                    if (!Response.equals("")) {
                        Log.d(TAG, "login response");
                        Log.d(TAG, Response.toString());
                        JSONObject data = new JSONObject(Response);
                        int errorCode = Integer.parseInt(data.getString("errorCode"));
                        try {
                            if (data.getBoolean("status")) {
                                if (errorCode == MobileEnum.OLDVERSIONAPK.getCode()) {
                                    Intent i = new Intent(SplashActivity.this, UpdateAppActivity.class);
                                    startActivity(i);
                                } else {
                                    String ServerLoginToken = data.getString("token");
                                    Log.d(TAG, "saving token:" + ServerLoginToken);
                                    tinydb.putString("gcmtoken", Gcmtoken);
                                    tinydb.putString("loginToken", ServerLoginToken);
                                    Constants.loginToken = ServerLoginToken;
                                    processquestionSet(data.getJSONArray("questionSets"));
                                    processDisposition(data.getJSONArray("dispositions"));
                                    processSettings(data.getJSONArray("settings"));
                                    processUserProfile(data.getJSONObject("userAccountDTO"));
                                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            } else {
                                if (errorCode == MobileEnum.OLDVERSIONAPK.getCode()) {
                                    Intent i = new Intent(SplashActivity.this, UpdateAppActivity.class);
                                    startActivity(i);
                                } else if (errorCode == MobileEnum.RELOGIIN.getCode()) {
                                    OpenReloginPopup(data.getString("Message"));
                                } else if(errorCode==MobileEnum.TOKENEXPIRED.getCode())
                                {
                                    InvalidToken();
                                }
                                else{
                                    RrefreshScreen(data.getString("Message"));
                                }
                            }
                        } catch (JSONException e) {
                            RrefreshScreen("Incorrect Response!! Please Try After Some Time.");
                        }
                    } else {
                        RrefreshScreen("Network Error!! Please Try After Some Time.");
                    }
                } else {
                    RrefreshScreen("Poor Internet Connectivity!! Please Check your Internet Connection.");
                }

        } catch (Exception e) {
            RrefreshScreen("Incorrect Response!! Please Try After Some Time.");
        }

    }



    private void OpenReloginPopup(final String messageToShow) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(SplashActivity.this)
                        .setIcon(R.drawable.sign_out)
                        .setTitle("Re-Login")
                        .setMessage(messageToShow + " Are you want to Re-Login?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StartLoginThread("", true);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                rllogin.setVisibility(View.VISIBLE);
                                Animation fadeInAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.twin);
                                rllogin.startAnimation(fadeInAnimation);
                                usernameEditText.setEnabled(true);
                                passwordEditText.setEnabled(true);
                                passwordEditText.setText("");
                                rllogin.setEnabled(true);
                                loginButton.setEnabled(true);
                                _progressBarImageView.setVisibility(View.INVISIBLE);
                                _progressBarImageView.setBackgroundResource(R.drawable.splashanim);
                                AnimationDrawable _frameAnimation = (AnimationDrawable) _progressBarImageView.getBackground();
                                _frameAnimation.stop();
                                servermsg.setVisibility(View.INVISIBLE);
                            }
                        })
                        .show();
            }
        });
    }

    private void InvalidToken() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                                rllogin.setVisibility(View.VISIBLE);
                                Animation fadeInAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.twin);
                                rllogin.startAnimation(fadeInAnimation);
                                usernameEditText.setEnabled(true);
                                passwordEditText.setEnabled(true);
                                passwordEditText.setText("");
                                rllogin.setEnabled(true);
                                loginButton.setEnabled(true);
                                _progressBarImageView.setVisibility(View.INVISIBLE);
                                _progressBarImageView.setBackgroundResource(R.drawable.splashanim);
                                AnimationDrawable _frameAnimation = (AnimationDrawable) _progressBarImageView.getBackground();
                                _frameAnimation.stop();
                                servermsg.setVisibility(View.INVISIBLE);
            }
        });
    }
    private void checkInternetPopup(){

                new AlertDialog.Builder(SplashActivity.this)
                        .setIcon(R.drawable.sign_out)
                        .setTitle("No-Internet")
                        .setMessage(" Do you want connect internet?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               finish();
                            }
                        })
                        .show();

    }


    private void RrefreshScreen(final String messageToShow){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //stop truck animated loader
                _progressBarImageView.setVisibility(View.INVISIBLE);
                _progressBarImageView.setBackgroundResource(R.drawable.splashanim);
                AnimationDrawable _frameAnimation = (AnimationDrawable) _progressBarImageView.getBackground();
                _frameAnimation.stop();
                servermsg.setVisibility(View.INVISIBLE);

                //enable all elemnt of login form
                if(messageToShow.contains("valid")) {//means token finish
                    rllogin.setVisibility(View.VISIBLE);
                    Animation fadeInAnimation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.twin);
                    rllogin.startAnimation(fadeInAnimation);
                    usernameEditText.setEnabled(true);
                    passwordEditText.setEnabled(true);
                    passwordEditText.setText("");
                    rllogin.setEnabled(true);
                    loginButton.setEnabled(true);
                    Toast.makeText(SplashActivity.this, messageToShow, Toast.LENGTH_LONG).show();
                    tinydb.putString("gcmtoken", "");
                }
                else {
                    //display error message
                    Toast.makeText(SplashActivity.this, messageToShow, Toast.LENGTH_LONG).show();
                    finish();
                }
                //CommonFunctions._messageToShow = "Network Error!! Please Try After Aome Time.";
                //CommonFunctions.sendMessageToActivity(1, SplashActivity.this);
            }
        });
    }


    public  String GetCurrentDAteTime(){
        Calendar cal1 = Calendar.getInstance(); // creates calendar
        cal1.setTime(new Date()); // sets calendar time/date
        Date b= cal1.getTime();
        SimpleDateFormat foramtter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String dateStart = foramtter.format(b);
        return dateStart;
    }

    private void processquestionSet(JSONArray questionSets) {
        Log.d(TAG, "questions: " + questionSets.toString());
        try {
            for (int x = 0; x < questionSets.length(); x++) {
                JSONObject ques = questionSets.getJSONObject(x);
                String questionId=ques.getString("id");
                String platformId=ques.getString("platformId");
                String question=ques.getString("question");
                String answerType=ques.getString("answerType");
                String itemCategory=ques.getString("itemCategory");
                String noEffect=ques.getString("noEffect");
                db.insertQuestionsData(questionId,platformId,question,answerType,itemCategory,noEffect);

            }
        } catch (Exception e) {
                       Log.e(TAG, "Not able to understand server response", e);
        }
    }



    private void processDisposition(JSONArray dispdata) {
        Log.d(TAG, "Incoming: " + dispdata.toString());
        try {
            for (int i = 0;i<dispdata.length();i++) {
                JSONObject disp = dispdata.getJSONObject(i);
                String Name=disp.getString("Name");
                String Value=disp.getString("Value");
                String type=disp.getString("type");
                String actiontype=disp.getString("actionType");
                db.insertDispostionData(Name,Value,type,actiontype);
            }
        } catch (Exception e) {
            Log.e(TAG, "Not able to understand server response", e);
        }
    }

    private void processUserProfile(JSONObject jsonpbj){
        try {
            tinydb.putString("firstName",jsonpbj.getString("firstName"));
            tinydb.putString("lastName",jsonpbj.getString("lastName"));
            tinydb.putString("emaiId",jsonpbj.getString("emaiId"));
            tinydb.putString("mobileNumber",jsonpbj.getString("mobileNumber"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void processSettings(JSONArray settingdata) {
        String res="";
        Log.d(TAG, "Incoming: " + settingdata.toString());
        try {
            for (int i = 0;i<settingdata.length();i++) {
                JSONObject set = settingdata.getJSONObject(i);
                String Category=set.getString("Category");
                String value=set.getString("Value");
                tinydb.putString(Category,value);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Not able to understand server response", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
