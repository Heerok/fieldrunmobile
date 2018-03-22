package com.humaralabs.fieldrun;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
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
import com.humaralabs.fieldrun.service.EventUpdateService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SubTaskDetailActivity extends ActionBarActivity {

    private static final String TAG = "TDA";
    public static final int SIGNATURE_ACTIVITY = 2;//abir
    private String filepath;
    private String signfilepath;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Button picked;
    TextView signclicktextview;
    ImageView signclickedimage;
    ImageView call_custmer;
    public static String orderid;
    String tripId = "";
    String tripExpiryDateTime = "";
    String taskId = "";
    String ref = "";
    String name = "";
    String pickups = "";
    String address = "";
    String zipCode = "";
    String phone = "";
    String taskType = "";
    String taskfacility = "";
    //String pickupQty = "";
    String reason = "";
    String delieveryDateTime = "";
    String comments = "";
    String status = "";
    String click_taskstatus;
    String payment_mode="";
    String consigneeNumber;
    String savedPinno="";
    DbAdapter db;
    TinyDB tb;
    Toolbar toolbar;
    TelephonyManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_task_detail);
        CaptureSignature.mypath=null;
        Log.d("CLICK", "Loading task");
        db=new DbAdapter(this);
        tb=new TinyDB(SubTaskDetailActivity.this);

        Intent detailIntent = getIntent();
        tripId = detailIntent.getExtras().get("tripId").toString();
        tripExpiryDateTime= detailIntent.getExtras().get("tripExpiryDateTime").toString();
        taskId = detailIntent.getExtras().get("taskId").toString();
        ref = detailIntent.getExtras().get("ref").toString();
        orderid=ref;
        name = detailIntent.getExtras().get("name").toString();
        pickups = detailIntent.getExtras().get("pickups").toString();
        address = detailIntent.getExtras().get("address").toString();
        zipCode = detailIntent.getExtras().get("zipCode").toString();
        phone = detailIntent.getExtras().get("phone").toString();
        taskType = detailIntent.getExtras().get("taskType").toString();
        taskfacility= detailIntent.getExtras().get("tripFacility").toString();
       // pickupQty = detailIntent.getExtras().get("pickupQty").toString();
        reason = detailIntent.getExtras().get("reason").toString();
        delieveryDateTime = detailIntent.getExtras().get("delieveryDateTime").toString();
        comments = detailIntent.getExtras().get("comments").toString();
        payment_mode = detailIntent.getExtras().get("payment_mode").toString();
        status = detailIntent.getExtras().get("status").toString();
        consigneeNumber=detailIntent.getExtras().get("consigneeNumber").toString();
        savedPinno=detailIntent.getExtras().get("pinno").toString();

        RelativeLayout triprl = (RelativeLayout) findViewById(R.id.triprl);
        RelativeLayout namerl = (RelativeLayout) findViewById(R.id.namerl);
        RelativeLayout mobilerl = (RelativeLayout) findViewById(R.id.mobilerl);
        RelativeLayout zipcoderl = (RelativeLayout) findViewById(R.id.zipcoderl);
        RelativeLayout addressrl = (RelativeLayout) findViewById(R.id.addressrl);

        TextView taskrefText = (TextView) findViewById(R.id.hosid);
        TextView qtyText = (TextView) findViewById(R.id.qty);
        TextView tasktypeText = (TextView) findViewById(R.id.tasktype);
        TextView tripidText = (TextView) findViewById(R.id.tripid);
        TextView nameText = (TextView) findViewById(R.id.name);
        TextView mobileText = (TextView) findViewById(R.id.mobile);
        TextView zipcodeText = (TextView) findViewById(R.id.zipcode);
        TextView addressText = (TextView) findViewById(R.id.address);

        picked=(Button)findViewById(R.id.task_button_picked);

        picked.setVisibility(View.VISIBLE);
        findViewById(R.id.task_button_faild).setVisibility(View.VISIBLE);
        //setting task ref
        if (ref == null || ref.equals("") || ref.equals("null") || ref.equals("NA")) {
            taskrefText.setVisibility(View.GONE);
        } else {
            taskrefText.setVisibility(View.VISIBLE);
            taskrefText.setText("Hos ref - " + ref);
        }

        //setting task type
        if (pickups == null || pickups.equals("") || pickups.equals("null") || pickups.equals("NA")) {
            qtyText.setVisibility(View.GONE);
        } else {
            qtyText.setVisibility(View.VISIBLE);
            qtyText.setText("Qty - "+pickups);
        }

        //setting tripid
        if (tripId == null || tripId.equals("") || tripId.equals("null") || tripId.equals("NA")) {
            triprl.setVisibility(View.GONE);
        } else {
            triprl.setVisibility(View.VISIBLE);
            tripidText.setText("Trip no - "+tripId);
        }

        //setting seller name
        if (name == null || name.equals("") || name.equals("null") || name.equals("NA")) {
            namerl.setVisibility(View.GONE);
        } else {
            namerl.setVisibility(View.VISIBLE);
            nameText.setText("Seller name - "+name);
        }

        //setting seller mobile
        if (phone == null || phone.equals("") || phone.equals("null") || phone.equals("NA")) {
            mobilerl.setVisibility(View.GONE);
        } else {
            mobilerl.setVisibility(View.VISIBLE);
            mobileText.setText(phone);
        }

        //setting zipcode
        if (zipCode == null || zipCode.equals("") || zipCode.equals("null") || zipCode.equals("NA")) {
            zipcoderl.setVisibility(View.GONE);
        } else {
            zipcoderl.setVisibility(View.VISIBLE);
            zipcodeText.setText("Zipcode - " + zipCode);
        }


        //setting address
        if (address == null || address.equals("") || address.equals("null") || address.equals("NA")) {
            addressrl.setVisibility(View.GONE);
        } else {
            addressrl.setVisibility(View.VISIBLE);
            addressText.setText(address);
        }
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

    public void click_pick(View view) {
        showActionDialog("success");
    }

    public void click_failed(View view) {
        showActionDialog("fail");
    }

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
    Spinner iddropdown;
    EditText amountp;
    EditText Cda_id;

    public void showActionDialog(final String action_type){
        filepath=null;
        signfilepath=null;
        //creating dialog object
        final Dialog dil = new Dialog(SubTaskDetailActivity.this);
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
        if(action_type.equals("fail")){
            pickupqty.setVisibility(View.GONE);
            pinno.setVisibility(View.GONE);
        }
        else{
            pinno.setVisibility(View.VISIBLE);
            String noofitemsfetaure=tb.getString("noofitemsfetaure");
            if(noofitemsfetaure.contains(taskType))
                pickupqty.setVisibility(View.VISIBLE);
            else
                pickupqty.setVisibility(View.GONE);
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

        if(action_type.equals("fail"))
            ShowHideDisposition(action_type);
        else
            dropdown.setVisibility(View.GONE);

        GPSChecker.GPSCheck(SubTaskDetailActivity.this,false);
        signaturelayout =(RelativeLayout) dil.getWindow().findViewById(R.id.signaturelayout);//abir
        if(!action_type.equals("fail")){
            String signaturecaptureType=tb.getString("signaturecapture");
            if(signaturecaptureType.contains(taskType)) {
                String signaturecapturefacility=tb.getString("signaturecapturefacility");
                String[] signaturecapturefacilitySplit = signaturecapturefacility.split(",");
                Boolean showOrNot=false;
                for (int i = 0; i < signaturecapturefacilitySplit.length; i++) {
                    if (signaturecapturefacilitySplit[i].equals("*") || signaturecapturefacilitySplit[i].equals(taskfacility)) {
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
                    Intent intent=new Intent(SubTaskDetailActivity.this, CaptureSignature.class);
                    startActivityForResult(intent,SIGNATURE_ACTIVITY);
                }
            });

            signclicktextview =(TextView) dil.getWindow().findViewById(R.id.signclicktextview);//abir
            signclickedimage =(ImageView) dil.getWindow().findViewById(R.id.signclickedimage);//abir
        }
        else{
            signaturelayout.setVisibility(View.GONE);
        }

        pickupqty.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

                try {
                    if (pickupqty.getVisibility() == View.VISIBLE && (Integer.parseInt(s.toString()) >= Integer.parseInt(pickups))) {
                        // Toast.makeText(SubTaskDetailActivity.this, "Pickup quantity cannot be greater than HOS quantity.", Toast.LENGTH_LONG).show();
                        dropdown.setVisibility(View.GONE);
                        imageView.setVisibility(View.GONE);
                    } else if (pickupqty.getVisibility() == View.VISIBLE && (Integer.parseInt(s.toString()) < Integer.parseInt(pickups))) {
                        //Toast.makeText(SubTaskDetailActivity.this, "A dropdown with dispositions show.", Toast.LENGTH_LONG).show();
                        ShowHideDisposition("success");
                    }
                }catch(Exception e){

                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        rdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(SubTaskDetailActivity.this, new DatePickerDialog.OnDateSetListener() {

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

                TimePickerDialog tpd = new TimePickerDialog(SubTaskDetailActivity.this,new TimePickerDialog.OnTimeSetListener() {
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
                        Log.d(TAG, "Saving to :" + filepath);
                    } catch (IOException ex) {
                        Log.e(TAG, "Error in img file creation!", ex);
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }

            }
        });

        task_button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String pickupqtyText="";//pickupqty.getText().toString();
            if(action_type.equals("fail")){
                pickupqtyText="0";
            }
            else{
                String noofitemsfetaure=tb.getString("noofitemsfetaure");
                if(noofitemsfetaure.contains(taskType))
                    pickupqtyText=pickupqty.getText().toString();
                else
                    pickupqtyText="1";
            }
            String PinnoText=pinno.getText().toString();
            String reasonText="";
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

            int RetryLeftCount=db.getAvailableRetryCountForParticularTask(Long.parseLong(taskId));
            if(pickupqty.getVisibility() == View.VISIBLE && (pickupqtyText.equals("") || pickupqtyText.equals("0"))){
                Toast.makeText(SubTaskDetailActivity.this, "Picked quantity cannot be zero or blank.", Toast.LENGTH_LONG).show();
            }
            else if (pickupqty.getVisibility() == View.VISIBLE && (Integer.parseInt(pickupqtyText)>Integer.parseInt(pickups))) {
                Toast.makeText(SubTaskDetailActivity.this, "Pickup quantity cannot be greater than HOS quantity.", Toast.LENGTH_LONG).show();
                dropdown.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
            }
            else if(pinno.getVisibility() == View.VISIBLE && RetryLeftCount<=0){
                saveTaskActionEvent("fail", Integer.parseInt(pickupqtyText), reasonText.trim(), "");
            }
            else if(pinno.getVisibility() == View.VISIBLE && PinnoText.equals("")){
                Toast.makeText(SubTaskDetailActivity.this, "Pin no can not be blank.", Toast.LENGTH_LONG).show();
            }
            else if(pinno.getVisibility() == View.VISIBLE && !PinnoText.equals(savedPinno)){
                int updatedCount=(RetryLeftCount-1);
                db.decreaseRetryCountForParticularTask(Long.parseLong(taskId), updatedCount);
                Toast.makeText(SubTaskDetailActivity.this, "wrong pin! Now you have "+ updatedCount +" chance left.", Toast.LENGTH_LONG).show();
            }
            else if (datetimerl.getVisibility() == View.VISIBLE &&  rescheduledDate.equals("")) {
                Toast.makeText(SubTaskDetailActivity.this, "Please eneter reschedule date", Toast.LENGTH_LONG).show();
            }
            else if (datetimerl.getVisibility() == View.VISIBLE &&  rescheduledTime.equals("")) {
                Toast.makeText(SubTaskDetailActivity.this, "Please eneter reschedule time", Toast.LENGTH_LONG).show();
            }
            else if (imageView.getVisibility() == View.VISIBLE && filepath==null) {
                Toast.makeText(SubTaskDetailActivity.this, "Please capture image", Toast.LENGTH_LONG).show();
            }
            else if(signaturelayout.getVisibility() == View.VISIBLE && signfilepath==null){
                Toast.makeText(SubTaskDetailActivity.this, "Please capture Signature", Toast.LENGTH_LONG).show();
            }
            else{
                String originalString = rescheduledDate+" "+rescheduledTime;
                Date date = null;
                String rescheduleDateTime="";
                try {
                    if(!originalString.equals(" ")) {
                        date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(originalString);
                        rescheduleDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(date);
                    }
                } catch (ParseException e) {
                    rescheduleDateTime="";
                    e.printStackTrace();
                }
                //save into event table
                saveTaskActionEvent(action_type,Integer.parseInt(pickupqtyText),reasonText.trim(),rescheduleDateTime);
            }
            }
        });
    }

    private void ShowHideDisposition(String action_type){
        final ArrayList<Disposition> dispositions  = db.getAllDisposition(taskType,action_type);
        if(dispositions.size()==0){
            dropdown.setVisibility(View.GONE);
        }
        else {
            dropdown.setVisibility(View.VISIBLE);
            String[] items = new String[dispositions.size()];
            int i = 0;
            for (Disposition d : dispositions) {
                items[i++] = d.value;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
            dropdown.setAdapter(adapter);
        }

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    boolean cmshowOrNot = true;
                    boolean showOrNot = true;
                    boolean showDatetimeOrNot = true;
                    String disname=dispositions.get(position).name;
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
                    Log.e(TAG, "Error in onitem selected!", ex);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public static String gactionType="";
    public void saveTaskActionEvent(String action_type,int qty,String selectedreason,String rescheduleDateTime){
        try {
            gactionType=action_type;
            String tstatus="";
            if(action_type.equals("fail")){
                tstatus="CLD";
            }
            else{
                tstatus="CMT";
            }

            //update task table status from pending to complete
            JSONObject taskUpdateparams = new JSONObject();
            taskUpdateparams.put("taskId", taskId);
            taskUpdateparams.put("status", tstatus);
            taskUpdateparams.put("comments", comments);
            taskUpdateparams.put("qty", qty);
            taskUpdateparams.put("reason", selectedreason);
            taskUpdateparams.put("rescheduleDateTime", rescheduleDateTime);
            taskUpdateparams.put("securePin", savedPinno);
            taskUpdateparams.put("pinVerified", true);

            Long res=db.insertFieldEvent(tripId,tripExpiryDateTime,"taskupdate", EventUpdateService.mlocation, taskUpdateparams.toString(),SubTaskDetailActivity.this);
            if(res!=0 && action_type.equals("fail")){
                db.updateTaskStatus(Constants.task_status_failed_code, Long.parseLong(taskId));
            }
            else if(res!=0 && action_type.equals("success")){
                db.updateTaskStatus(Constants.task_status_done_code, Long.parseLong(taskId));
                db.updateHosQuantity(qty, Long.parseLong(taskId));
            }

            if (signfilepath!=null) {
                taskUpdateparams.put("filename", CaptureSignature.mypath.getPath());
                db.insertFieldEvent(tripId,tripExpiryDateTime,"imgupload", EventUpdateService.mlocation, taskUpdateparams.toString(),SubTaskDetailActivity.this);
            }

            if (filepath != null) {
                taskUpdateparams.put("filename", filepath);
                db.insertFieldEvent(tripId,tripExpiryDateTime,"imgupload", EventUpdateService.mlocation, taskUpdateparams.toString(),SubTaskDetailActivity.this);
            }

            finish();
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);

        } catch (JSONException e) {
            Log.e(TAG, "Error in json", e);
        } catch (Exception e) {
            Log.e(TAG, "Error in saveTaskActionEvent", e);
        }
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
                Log.e(TAG, "ERROR writing to image file!", e);
            }
            scaledbitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
            signclickedimage.setBackgroundDrawable(ob);
            signfilepath=filename;
        }
        catch(Exception e){
            Log.e(TAG, "ERROR writing to image file!", e);
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
                Log.e(TAG, "ERROR writing to image file!", e);
            }
            scaledbitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
            imageView.setBackgroundDrawable(ob);
        }
        catch(Exception e){
            Log.e(TAG, "ERROR writing to image file!", e);
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float)height;
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
}
