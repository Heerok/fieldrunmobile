package com.humaralabs.fieldrun;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.Disposition;
import com.humaralabs.fieldrun.server.ServerInterface;
import com.humaralabs.fieldrun.service.EventUpdateService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class BasketDetailActivity extends ActionBarActivity {

    public static final int SIGNATURE_ACTIVITY = 2;//abir
    static final int REQUEST_IMAGE_CAPTURE = 1;

    TextView txt_server_id;
    TextView txt_seller_name;
    TextView txt_seller_zipcode;
    TextView txt_seller_address;
    TextView txt_eqty;
    TextView txt_tripid;

    RelativeLayout namerl;
    RelativeLayout triprl;
    RelativeLayout zipcoderl;
    RelativeLayout addsressrl;

    DbAdapter db;
    TinyDB tb;
    String basket_server_id = "";
    String basket_eqty = "";
    String basket_seller_name = "";
    String basket_seller_zipcode = "";
    String basket_seller_address = "";
    String basket_trip_id = "";
    String basket_trip_type = "";
    private String tripFacility="";
    private String tripExpiryDateTime="";

   // Button pickOrFail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_detail);

        db=new DbAdapter(this);
        tb=new TinyDB(BasketDetailActivity.this);
        Intent detailIntent = getIntent();
        basket_seller_name = detailIntent.getExtras().get("basket_seller_name").toString();
        basket_server_id = detailIntent.getExtras().get("basket_server_id").toString();
        basket_seller_zipcode = detailIntent.getExtras().get("basket_seller_zipcode").toString();
        basket_seller_address = detailIntent.getExtras().get("basket_seller_address").toString();
        basket_trip_id = detailIntent.getExtras().get("basket_trip_id").toString();
        basket_trip_type = detailIntent.getExtras().get("basket_trip_type").toString();
        basket_eqty=detailIntent.getExtras().get("basket_eqty").toString();
        tripFacility = detailIntent.getExtras().getString("tripFacility");
        tripExpiryDateTime = detailIntent.getExtras().getString("tripExpiryDateTime");

        //pickOrFail=(Button) findViewById(R.id.pickOrFail);

        namerl = (RelativeLayout) findViewById(R.id.namerl);
        zipcoderl = (RelativeLayout) findViewById(R.id.zipcoderl);
        addsressrl = (RelativeLayout) findViewById(R.id.addressrl);
        triprl = (RelativeLayout) findViewById(R.id.triprl);

        txt_server_id = (TextView) findViewById(R.id.basketid);
        txt_seller_name = (TextView) findViewById(R.id.name);
        txt_seller_zipcode = (TextView) findViewById(R.id.zipcode);
        txt_seller_address = (TextView) findViewById(R.id.address);
        txt_tripid=(TextView) findViewById(R.id.tripid);
        txt_eqty = (TextView) findViewById(R.id.eqty);

        //setPickFailButtons();

        //setting task ref
        if (basket_server_id == null || basket_server_id.equals("") || basket_server_id.equals("null") || basket_server_id.equals("NA")) {
            txt_server_id.setVisibility(View.GONE);
        } else {
            txt_server_id.setVisibility(View.VISIBLE);
            txt_server_id.setText("Basket - " + basket_server_id);
        }

        //setting trip id
        if (basket_trip_id == null || basket_trip_id.equals("") || basket_trip_id.equals("null") || basket_trip_id.equals("NA")) {
            triprl.setVisibility(View.GONE);
        } else {
            triprl.setVisibility(View.VISIBLE);
            txt_tripid.setText("Trip No - "+basket_trip_id);
        }
        //setting task ref
        if (basket_seller_name == null || basket_seller_name.equals("") || basket_seller_name.equals("null") || basket_seller_name.equals("NA")) {
            namerl.setVisibility(View.GONE);
        } else {
            namerl.setVisibility(View.VISIBLE);
            txt_seller_name.setText(basket_seller_name);
        }

        //setting basket seller zipcode
        if (basket_seller_zipcode == null || basket_seller_zipcode.equals("") || basket_seller_zipcode.equals("null") || basket_seller_zipcode.equals("NA")) {
            zipcoderl.setVisibility(View.GONE);
        } else {
            zipcoderl.setVisibility(View.VISIBLE);
            txt_seller_zipcode.setText(basket_seller_zipcode);
        }

        //setting seller address
        if (basket_seller_address == null || basket_seller_address.equals("") || basket_seller_address.equals("null") || basket_seller_address.equals("NA")) {
            addsressrl.setVisibility(View.GONE);
        } else {
            addsressrl.setVisibility(View.VISIBLE);
            txt_seller_address.setText(basket_seller_address);
        }

        //setting basket quantity
        if (basket_eqty == null || basket_eqty.equals("") || basket_eqty.equals("null") || basket_eqty.equals("NA")) {
            txt_eqty.setVisibility(View.GONE);
        } else {
            txt_eqty.setVisibility(View.VISIBLE);
            txt_eqty.setText("Qty - "+basket_eqty);
        }

    }

   /* private void setPickFailButtons() {
        int PickedHosCount=db.CheckIsThereAnyPickedHos(Long.valueOf(basket_server_id),Constants.task_status_done_code);
        if(PickedHosCount>0)
        {
            //hide fail button
            pickOrFail.setText("Pick");
            pickOrFail.setVisibility(View.GONE);
            int PendingHosCount=db.CheckIsThereAnyPickedHos(Long.valueOf(basket_server_id),Constants.task_status_pending_code);
            if(PendingHosCount==0)
            {
                pickOrFail.setVisibility(View.VISIBLE);
            }
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        GPSChecker.GPSCheck(BasketDetailActivity.this, false);
        //setPickFailButtons();
    }

    public void click_hos(View view)
    {
        Intent detailIntent=new Intent(BasketDetailActivity.this,HosListActivity.class);
        detailIntent.putExtra("basket_server_id", basket_server_id);
        detailIntent.putExtra("basket_eqty", basket_eqty);
        detailIntent.putExtra("basket_seller_name", basket_seller_name);
        detailIntent.putExtra("basket_seller_zipcode",basket_seller_zipcode);
        detailIntent.putExtra("basket_seller_address",basket_seller_address);
        detailIntent.putExtra("basket_trip_id",basket_trip_id);
        detailIntent.putExtra("basket_trip_type",basket_trip_type);
        detailIntent.putExtra("tripExpiryDateTime", tripExpiryDateTime);
        detailIntent.putExtra("tripFacility", tripFacility);
        startActivity(detailIntent);
    }

    public String basketstatus="";
    /*public void click_pickOrFail(View view) {
        if(pickOrFail.getText().equals("Fail")) {
            showActionDialog("fail");
            basketstatus = "CLD";
        }
        else {
            showActionDialog("success");
            basketstatus = "CMT";
        }
    }*/

    public void click_pick(View view) {
        basketstatus = "CMT";
        showActionDialog("success");
    }

    public void click_Fail(View view) {
        basketstatus = "CLD";
        showActionDialog("fail");
    }

    String reasonText="";
    String pickupqtyText="";
    private String filepath;
    private String signfilepath;
    Spinner dropdown;
    ImageView imageView;
    RelativeLayout signaturelayout;
    Button task_button_ok;
    EditText pickupqty;
    EditText pinno;
    LinearLayout datetimerl;
    EditText rdate;
    EditText rtime;
    EditText  comment_text;
    TextView signclicktextview;
    ImageView signclickedimage;

    Spinner iddropdown;
    EditText amountp;
    EditText Cda_id;

    public void showActionDialog(final String action_type){
        filepath=null;
        signfilepath=null;
        //creating dialog object
        final android.app.Dialog dil = new Dialog(BasketDetailActivity.this);
        //hiding default title bar of dialog
        dil.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dil.setContentView(R.layout.task_action_popu);
        dil.getWindow().getAttributes().width= WindowManager.LayoutParams.MATCH_PARENT;
        dil.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dil.show(); //to show dialog box
        iddropdown= (Spinner)dil.getWindow().findViewById(R.id.ids);
        Cda_id=(EditText)dil.getWindow().findViewById(R.id.cdaid);
        amountp=(EditText)dil.getWindow().findViewById(R.id.amountp);
        pickupqty=(EditText)dil.getWindow().findViewById(R.id.pickupqty);
        pinno=(EditText)dil.getWindow().findViewById(R.id.pinno);
        RatingBar ratingBar=(RatingBar) dil.getWindow().findViewById(R.id.ratingBar);
        ratingBar.setVisibility(View.GONE);
        comment_text=(EditText)dil.getWindow().findViewById(R.id.comment_text);
        comment_text.setVisibility(View.GONE);
        pinno.setVisibility(View.GONE);
        if(action_type.equals("fail")){
            pickupqty.setVisibility(View.GONE);
        }
        else{
            pickupqty.setVisibility(View.VISIBLE);
            int qty=db.getTotalBasketPickedQuantity(Long.valueOf(basket_server_id));
            pickupqty.setText(""+qty);
        }

        amountp.setVisibility(View.GONE);
        Cda_id.setVisibility(View.GONE);
        iddropdown.setVisibility(View.GONE);

        task_button_ok=(Button)dil.getWindow().findViewById(R.id.task_button_ok);
        datetimerl = (LinearLayout)dil.getWindow().findViewById(R.id.datetimerl);
        rdate=(EditText)dil.getWindow().findViewById(R.id.rdate);
        rtime=(EditText)dil.getWindow().findViewById(R.id.rtime);
        imageView = (ImageView)dil.getWindow().findViewById(R.id.imageView);
        dropdown = (Spinner)dil.getWindow().findViewById(R.id.dispositions);
        ArrayList<Disposition> dispositions = null;
        if(action_type.equals("fail")) {
            dispositions = db.getAllDisposition(basket_trip_type, action_type);
            if (dispositions.size() == 0) {
                dropdown.setVisibility(View.GONE);
            } else {
                dropdown.setVisibility(View.VISIBLE);
                String[] items = new String[dispositions.size()];
                int i = 0;
                for (Disposition d : dispositions) {
                    items[i++] = d.value;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
                dropdown.setAdapter(adapter);
            }
        }
        else{
            dropdown.setVisibility(View.GONE);
        }
        GPSChecker.GPSCheck(BasketDetailActivity.this,false);
        signaturelayout =(RelativeLayout) dil.getWindow().findViewById(R.id.signaturelayout);//abir
        if(!action_type.equals("fail")){
            String signaturecaptureType=tb.getString("signaturecapture");
            if(signaturecaptureType.contains(basket_trip_type)) {
                String signaturecapturefacility=tb.getString("signaturecapturefacility");
                String[] signaturecapturefacilitySplit = signaturecapturefacility.split(",");
                Boolean showOrNot=false;
                for (int i = 0; i < signaturecapturefacilitySplit.length; i++) {
                    if (signaturecapturefacilitySplit[i].equals("*") || signaturecapturefacilitySplit[i].equals(tripFacility)) {
                        showOrNot = true;
                        break;
                    } else {
                        showOrNot = false;
                    }
                }
                if(showOrNot)
                    signaturelayout.setVisibility(View.VISIBLE);
                else
                    signaturelayout.setVisibility(View.GONE);
            }
            else
                signaturelayout.setVisibility(View.GONE);

            signaturelayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(BasketDetailActivity.this, CaptureSignature.class);
                    startActivityForResult(intent,SIGNATURE_ACTIVITY);
                }
            });


            signclicktextview =(TextView) dil.getWindow().findViewById(R.id.signclicktextview);//abir
            signclickedimage =(ImageView) dil.getWindow().findViewById(R.id.signclickedimage);//abir
        }
        else{
            signaturelayout.setVisibility(View.GONE);
        }

        final ArrayList<Disposition> finalDispositions = dispositions;
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    boolean cmshowOrNot = false;
                    boolean showOrNot = false;
                    boolean showDatetimeOrNot = false;
                    String disname= finalDispositions.get(position).name;
                    if (disname != null && !disname.isEmpty()) {
                        //Toast.makeText(parent.getContext(),"OnItemSelectedListener : " +  dis.getName(),Toast.LENGTH_SHORT).show();

                        String commentboxString = tb.getString("commentbox");

                        String[] commentboxStringSplit = commentboxString.split(",");
                        for (int i = 0; i < commentboxStringSplit.length; i++) {
                            if (commentboxStringSplit[i].equals(disname)) {
                                cmshowOrNot = true;
                                break;
                            } else {
                                cmshowOrNot = false;
                            }
                        }


                        String imageCaptureTypeString = tb.getString("imagecapture");
                        String[] imageCaptureTypeStringSplit = imageCaptureTypeString.split(",");
                        for (int i = 0; i < imageCaptureTypeStringSplit.length; i++) {
                            if (imageCaptureTypeStringSplit[i].equals(disname)) {
                                showOrNot = true;
                                break;
                            } else {
                                showOrNot = false;
                            }
                        }
                        String rescheduledatetimeTypeString = tb.getString("rescheduledatetime");
                        String[] rescheduledatetimeStringSplit = rescheduledatetimeTypeString.split(",");
                        for (int i = 0; i < rescheduledatetimeStringSplit.length; i++) {
                            if (rescheduledatetimeStringSplit[i].equals(disname)) {
                                showDatetimeOrNot = true;
                                break;
                            } else {
                                showDatetimeOrNot = false;
                            }
                        }
                    }
                    if (cmshowOrNot) {
                        comment_text.setVisibility(View.VISIBLE);
                        // dil.getWindow().findViewById(R.id.task_button_ok).setVisibility(View.GONE);
                    } else {
                        comment_text.setVisibility(View.GONE);
                        //dil.getWindow().findViewById(R.id.task_button_ok).setVisibility(View.VISIBLE);
                    }
                    if (showOrNot) {
                        imageView.setVisibility(View.VISIBLE);
                        // dil.getWindow().findViewById(R.id.task_button_ok).setVisibility(View.GONE);
                    } else {
                        imageView.setVisibility(View.GONE);
                        //dil.getWindow().findViewById(R.id.task_button_ok).setVisibility(View.VISIBLE);
                    }

                    if (showDatetimeOrNot) {
                        datetimerl.setVisibility(View.VISIBLE);
                        // dil.getWindow().findViewById(R.id.task_button_ok).setVisibility(View.GONE);
                    } else {
                        datetimerl.setVisibility(View.GONE);
                        //dil.getWindow().findViewById(R.id.task_button_ok).setVisibility(View.VISIBLE);
                    }
                }
                catch (Exception ex){
                    //Log.e(TAG, "Error in onitem selected!", ex);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        rdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(BasketDetailActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        rdate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                    }
                }, mYear, mMonth, mDay);
                dpd.show();
            }
        });
        rtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                TimePickerDialog tpd = new TimePickerDialog(BasketDetailActivity.this,new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,int minute) {
                        String am_pm = (hourOfDay < 12) ? "AM" : "PM";
                        rtime.setText(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
                tpd.show();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    filepath = photoFile.getPath();
                    //Log.d(TAG, "Saving to :" + filepath);
                } catch (IOException ex) {
                    //Log.e(TAG, "Error in img file creation!", ex);
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
            }
        });

        task_button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickupqtyText="";//pickupqty.getText().toString();
                if(action_type.equals("fail")){
                    pickupqtyText="0";
                }
                else{
                    String noofitemsfetaure=tb.getString("noofitemsfetaure");
                    if(noofitemsfetaure.contains(basket_trip_type))
                        pickupqtyText=pickupqty.getText().toString();
                    else
                        pickupqtyText="1";
                }

                reasonText="";
                String rescheduledDate="";
                String rescheduledTime="";
                try {
                    if(dropdown.getVisibility()==View.VISIBLE)
                        reasonText = dropdown.getSelectedItem().toString();
                    else
                        reasonText = "";
                    if(comment_text.getVisibility()==View.VISIBLE){
                        reasonText=reasonText+" "+ comment_text.getText().toString().trim();
                    }
                }
                catch(Exception e){
                    reasonText ="";
                }

                try {
                    rescheduledDate = rdate.getText().toString();
                    rescheduledTime = rtime.getText().toString();
                }
                catch(Exception e){
                    rescheduledDate="";
                    rescheduledTime="";
                }


                if(pickupqty.getVisibility() == View.VISIBLE && (pickupqtyText.equals("") || pickupqtyText.equals("0"))){
                    Toast.makeText(BasketDetailActivity.this, "Please enter no of items", Toast.LENGTH_LONG).show();
                }
                else if (imageView.getVisibility() == View.VISIBLE && filepath==null) {
                    Toast.makeText(BasketDetailActivity.this, "Please capture image", Toast.LENGTH_LONG).show();
                }
                else if(signaturelayout.getVisibility() == View.VISIBLE && signfilepath==null){
                    Toast.makeText(BasketDetailActivity.this, "Please capture Signature", Toast.LENGTH_LONG).show();
                }
                else{
                    StartUpdateBasketThread();
                    dil.dismiss();
                }
            }
        });
    }


    Boolean AuthError=false;
    public void StartUpdateBasketThread(){
        new Thread()
        {
            public void run()
            {
                AuthError=false;
                Boolean status=SendTripEventsToServer();
                if(status){
                    final Boolean fetchtripstatus=UpdateBasketOnServer();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                                MainActivity.alreadyLoadingAllTripFragment=false;
                            }
                            if (AuthError) {
                                AutomaticLogout();
                            } else if (!fetchtripstatus) {//error case then read from local db
                                CommonFunctions.sendMessageToActivity(1, BasketDetailActivity.this);
                            }
                        }
                    });
                }
                else{//error case then read from local db
                    db.insertUpdatesCount("rccallserverFail");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                                MainActivity.alreadyLoadingAllTripFragment=false;
                            }
                            if(AuthError){
                                AutomaticLogout();
                            }
                            else {
                                CommonFunctions.sendMessageToActivity(1, BasketDetailActivity.this);
                            }
                        }
                    });
                }
            }
        }.start();
    }

    private void AutomaticLogout(){
        tb.putString("loginToken", "");
        tb.putString("gcmtoken", "");
        Toast.makeText(BasketDetailActivity.this, "Authentication error! Please Login Again.", Toast.LENGTH_LONG).show();
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
                Dialog = new ProgressDialog(BasketDetailActivity.this);
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

    private Boolean UpdateBasketOnServer(){
        Boolean CallServerForFetchTripdata=false;
        String Result="";
        try{
            String url = Constants.ServerApiUrl + "mobile/SuperTaskUpdate";
            if (tb == null)
                tb = new TinyDB(BasketDetailActivity.this);
            String loginToken = tb.getString("loginToken");
            JSONObject params = new JSONObject();

            Location location= EventUpdateService.mlocation;
            Double latitude = (double) 0, longitude = (double) 0;
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            params.put("token", loginToken);
            params.put("superTaskId", basket_server_id);
            params.put("status", basketstatus);
            params.put("remarks", reasonText);
            params.put("pickedQty", pickupqtyText);
            params.put("latitude", latitude);
            params.put("longitude", longitude);
            String iemino = tb.getString("iemino");
            Result = ServerInterface.CallServerApi(params, url,55,loginToken,iemino);
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
                        CallServerForFetchTripdata=true;
                        if(basketstatus.equals("CLD"))
                            db.updateBasketStatus(Constants.basket_status_failed_code,Long.parseLong(basket_server_id));
                        else
                            db.updateBasketStatus(Constants.basket_status_complete_code, Long.parseLong(basket_server_id));
                        finish();
                    }
                    else{
                        String msg=mydata.getString("Message");
                        CommonFunctions._messageToShow=msg;//SendTripEventsToServer
                        CallServerForFetchTripdata=false;
                    }
                } catch (Exception e) {
                    CommonFunctions._messageToShow = "Invalid Response! Refresh or Please try again later!!";
                    CallServerForFetchTripdata=false;
                    e.printStackTrace();
                }
            }
            else{
                CommonFunctions._messageToShow = "Network Error! Refresh or Please try again later!!";
                CallServerForFetchTripdata=false;
            }
        }catch(Exception e)
        {
            CommonFunctions._messageToShow = "Invalid Response! Refresh or Please try again later!!";
            CallServerForFetchTripdata=false;
            e.printStackTrace();
        }
        return CallServerForFetchTripdata;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
            //findViewById(R.id.task_button_failed).setVisibility(View.VISIBLE);
        }
        else if (requestCode == SIGNATURE_ACTIVITY && resultCode == RESULT_OK) {//abir
            Bundle bundle = data.getExtras();
            String status  = bundle.getString("status");
            if(status.equalsIgnoreCase("done")){
                signclicktextview.setVisibility(View.GONE);
                signclickedimage.setVisibility(View.VISIBLE);
                if(CaptureSignature.mypath!=null) {
                    setSignaturePic(CaptureSignature.mypath.getPath());
                    Toast toast = Toast.makeText(this, "Signature capture successful!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
        else{
            filepath=null;
            CaptureSignature.mypath=null;
            signfilepath=null;
        }
    }

    private void setSignaturePic(String filename) {//abir
        try {
            // Get the dimensions of the View
            int targetW = 200;//signclickedimage.getWidth();
            int targetH = 120;//signclickedimage.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filename, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap bitmap = BitmapFactory.decodeFile(CaptureSignature.mypath.getPath(), bmOptions);
            Bitmap scaledbitmap = getResizedBitmap(bitmap, 320);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(CaptureSignature.mypath.getPath());
            } catch (FileNotFoundException e) {
               // Log.e(TAG, "ERROR writing to image file!", e);
            }
            scaledbitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
            signclickedimage.setBackgroundDrawable(ob);
            signfilepath=filename;
        }
        catch(Exception e){
            //Log.e(TAG, "ERROR writing to image file!", e);
        }
    }


    private void setPic() {
        try {
            // Get the dimensions of the View
            int targetW = imageView.getWidth();
            int targetH = imageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filepath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap bitmap = BitmapFactory.decodeFile(filepath, bmOptions);
            Bitmap scaledbitmap = getResizedBitmap(bitmap, 320);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(filepath);
            } catch (FileNotFoundException e) {
                //Log.e(TAG, "ERROR writing to image file!", e);
            }
            scaledbitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
            imageView.setBackgroundDrawable(ob);
        }
        catch(Exception e){
            //Log.e(TAG, "ERROR writing to image file!", e);
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TASK_" + timeStamp + "_";

        return File.createTempFile(
                imageFileName,
                ".jpg",
                getExternalFilesDir(null)
        );
    }


    /*private ProgressDialog Dialog = null;
    private class SendTripEventsToServer extends AsyncTask<Void, Void, Void>
    {
        long activetripid=0;
        int requestId=0;
        String Result="NoEvents";
        protected void onPreExecute()
        {
            Dialog = new ProgressDialog(BasketDetailActivity.this);
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
                            CommonFunctions.sendMessageToActivity(1, BasketDetailActivity.this);
                            if (Dialog != null && Dialog.isShowing()) {
                                Dialog.dismiss();
                            }
                        }
                    } catch (Exception e) {
                        if (requestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, requestId);
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, BasketDetailActivity.this);
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
                    CommonFunctions.sendMessageToActivity(1, BasketDetailActivity.this);
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
                        tb = new TinyDB(BasketDetailActivity.this);
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
                            if(basketstatus.equals("CLD"))
                                db.updateBasketStatus(Constants.basket_status_failed_code,Long.parseLong(basket_server_id));
                            else
                                db.updateBasketStatus(Constants.basket_status_complete_code, Long.parseLong(basket_server_id));
                            finish();
                        }
                        else{
                            String msg=jobj.getString("Message");
                            CommonFunctions._messageToShow =msg;//SendTripEventsToServer
                            CommonFunctions.sendMessageToActivity(1, BasketDetailActivity.this);
                        }
                    } catch (Exception e) {
                        CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, BasketDetailActivity.this);
                        e.printStackTrace();
                    }
                } else {
                    CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                    CommonFunctions.sendMessageToActivity(1, BasketDetailActivity.this);
                }
                Dialog.dismiss();
            }
        }
        protected Void doInBackground(Void... par)
        {
            try{
                String url = Constants.ServerApiUrl + "mobile/SuperTaskUpdate";
                if (tb == null)
                    tb = new TinyDB(BasketDetailActivity.this);
                String loginToken = tb.getString("loginToken");
                JSONObject params = new JSONObject();
                params.put("token", loginToken);
                params.put("superTaskId", basket_server_id);
                params.put("status", basketstatus);
                params.put("remarks", reasonText);
                params.put("pickedQty", pickupqtyText);
                Result = ServerInterface.CallServerApi(params, url,55,loginToken);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }*///end get trips class
}