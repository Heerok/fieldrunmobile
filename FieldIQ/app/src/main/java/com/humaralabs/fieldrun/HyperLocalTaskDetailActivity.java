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


public class HyperLocalTaskDetailActivity extends ActionBarActivity {

    private static final String TAG = "TDA";
    public static final int SIGNATURE_ACTIVITY = 2;//abir
    private String filepath;
    private String signfilepath;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Button print;
    Button doneOrpicked;
    Button doorstep;
    Button cancel;
    TextView signclicktextview;
    ImageView signclickedimage;
    ImageView call_custmer;
    public static String orderid;

    String taskamount = "";
    String codamount="";
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
    String pickupQty = "";
    String reason = "";
    String delieveryDateTime = "";
    String comments = "";
    String status = "";
    String click_taskstatus;
    String payment_mode="";
    String consigneeNumber;
    String consigneeName;
    DbAdapter db;
    TinyDB tb;
    Toolbar toolbar;
    TelephonyManager manager;
    String CollectableAmount="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        CaptureSignature.mypath=null;
        Log.d("CLICK", "Loading task");
        db=new DbAdapter(this);
        tb=new TinyDB(HyperLocalTaskDetailActivity.this);

        Intent detailIntent = getIntent();
        taskamount= detailIntent.getExtras().get("amount").toString();
        codamount= detailIntent.getExtras().get("codamount").toString();
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
        pickupQty = detailIntent.getExtras().get("pickupQty").toString();
        reason = detailIntent.getExtras().get("reason").toString();
        delieveryDateTime = detailIntent.getExtras().get("delieveryDateTime").toString();
        comments = detailIntent.getExtras().get("comments").toString();
        payment_mode = detailIntent.getExtras().get("payment_mode").toString();
        status = detailIntent.getExtras().get("status").toString();
        consigneeNumber=detailIntent.getExtras().get("consigneeNumber").toString();
        consigneeName=detailIntent.getExtras().get("consigneeName").toString();

        String showDoneButtonn=detailIntent.getExtras().get("showDoneButtonn").toString();

        if (taskamount == null || taskamount.equals("null"))
        {
            taskamount="0";
        }

        if (codamount == null || codamount.equals("null"))
        {
            codamount="0";
        }
            // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RelativeLayout triprl = (RelativeLayout) findViewById(R.id.triprl);
        RelativeLayout namerl = (RelativeLayout) findViewById(R.id.namerl);
        RelativeLayout mobilerl = (RelativeLayout) findViewById(R.id.mobilerl);
        RelativeLayout zipcoderl = (RelativeLayout) findViewById(R.id.zipcoderl);
        RelativeLayout addressrl = (RelativeLayout) findViewById(R.id.addressrl);

        TextView taskrefText = (TextView) findViewById(R.id.taskref);
        TextView codeidText = (TextView) findViewById(R.id.codeid);
        TextView collectableAmountText = (TextView) findViewById(R.id.amount);
        TextView tasktypeText = (TextView) findViewById(R.id.tasktype);
        TextView tripidText = (TextView) findViewById(R.id.tripid);
        TextView nameText = (TextView) findViewById(R.id.name);
        TextView mobileText = (TextView) findViewById(R.id.mobile);
        TextView zipcodeText = (TextView) findViewById(R.id.zipcode);
        TextView addressText = (TextView) findViewById(R.id.address);


        print=(Button)findViewById(R.id.task_button_print);
        doneOrpicked=(Button)findViewById(R.id.task_button_delivered);
        call_custmer=(ImageView)findViewById(R.id.callimage);

        doorstep=(Button)findViewById(R.id.task_button_doorstep);
        cancel=(Button)findViewById(R.id.task_button_cancel);


        showtasktype();
        //setting taskstatus
     /*   if (Constants.task_status_start_code==Integer.parseInt(status)) {
            findViewById(R.id.task_button_print).setVisibility(View.GONE);
            findViewById(R.id.task_button_delivered).setVisibility(View.GONE);
            findViewById(R.id.task_button_faild).setVisibility(View.GONE);
            findViewById(R.id.task_button_doorstep).setVisibility(View.VISIBLE);
            findViewById(R.id.task_button_cancel).setVisibility(View.VISIBLE);
        }
        else //if(Constants.task_status_doorstep_code==Integer.parseInt(status) ||(Constants.task_status_pending_code==Integer.parseInt(status) && taskType.equals("FWD")))
        {*/
            findViewById(R.id.task_button_print).setVisibility(View.VISIBLE);
            findViewById(R.id.task_button_delivered).setVisibility(View.VISIBLE);
            findViewById(R.id.task_button_faild).setVisibility(View.VISIBLE);
            findViewById(R.id.task_button_doorstep).setVisibility(View.GONE);
            findViewById(R.id.task_button_cancel).setVisibility(View.GONE);
        //}


        if(showDoneButtonn.equals("YES")){
            findViewById(R.id.task_button_print).setVisibility(View.VISIBLE);
            findViewById(R.id.task_button_delivered).setVisibility(View.VISIBLE);
            findViewById(R.id.task_button_faild).setVisibility(View.VISIBLE);
        }
        else{
            findViewById(R.id.task_button_print).setVisibility(View.GONE);
            findViewById(R.id.task_button_delivered).setVisibility(View.GONE);
            findViewById(R.id.task_button_faild).setVisibility(View.GONE);
        }

        //setting task ref
        if (ref == null || ref.equals("") || ref.equals("null") || ref.equals("NA")) {
            taskrefText.setVisibility(View.GONE);
        } else {
            taskrefText.setVisibility(View.VISIBLE);
            taskrefText.setText("Order - " + ref);
        }

        //setting task type
        if (taskType == null || taskType.equals("") || taskType.equals("null") || taskType.equals("NA")) {
            tasktypeText.setVisibility(View.GONE);
        } else {
            tasktypeText.setVisibility(View.VISIBLE);
            tasktypeText.setText("Type - "+taskType);
        }


        if (payment_mode == null || payment_mode.equals("") || payment_mode.equals("null") || payment_mode.equals("NA")) {
            codeidText.setVisibility(View.GONE);
        } else {
            codeidText.setVisibility(View.VISIBLE);
            if(payment_mode.equals("PPD")){
                codeidText.setText("Prepaid");
            }
            else if(payment_mode.equals("PSPD")){
                codeidText.setText("Postpaid");
            }
            else {
                codeidText.setText(payment_mode);
            }
        }


        switch (taskType) {
            case "HYP-PICKUP":
                if(payment_mode.equals("COD"))
                    CollectableAmount="";
                else if(payment_mode.equals("COP"))
                    CollectableAmount=taskamount;
                else if(payment_mode.equals("PPD") || payment_mode.equals("PSPD"))
                    CollectableAmount=codamount;
                else
                    CollectableAmount="";
                break;
            case "HYP-DELIVERY":
                if(payment_mode.equals("COD"))
                    CollectableAmount=String.valueOf(Integer.parseInt(taskamount)+Integer.parseInt(codamount));
                else if(payment_mode.equals("COP"))
                    CollectableAmount=codamount;
                else if(payment_mode.equals("PPD") || payment_mode.equals("PSPD"))
                    CollectableAmount=codamount;
                else
                    CollectableAmount="";
                break;
        }

        if(CollectableAmount.equals("0"))
            CollectableAmount="";
        //setting task type
        if (CollectableAmount == null || CollectableAmount.equals("") || CollectableAmount.equals("null") || CollectableAmount.equals("NA")) {
            collectableAmountText.setVisibility(View.GONE);
        } else {
            collectableAmountText.setVisibility(View.VISIBLE);
            collectableAmountText.setText("Collectable Amount - "+CollectableAmount);
        }


        //setting tripid
        if (tripId == null || tripId.equals("") || tripId.equals("null") || tripId.equals("NA")) {
            triprl.setVisibility(View.GONE);
        } else {
            triprl.setVisibility(View.VISIBLE);
            tripidText.setText("Trip no - "+tripId);
        }

        //setting seller name
        if (consigneeName == null || consigneeName.equals("") || consigneeName.equals("null") || consigneeName.equals("NA")) {
            namerl.setVisibility(View.GONE);
        } else {
            namerl.setVisibility(View.VISIBLE);
            nameText.setText("Name - "+consigneeName);
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
            zipcodeText.setText("Zipcode - "+zipCode);
        }


       // if (payment_mode.equals("COD")) {
            //codeidText.setVisibility(View.VISIBLE);
            //codeidText.setText("COD Id - " + taskId);
        //} else {
            //codeidText.setVisibility(View.GONE);
        //}

        //setting address
        if (address == null || address.equals("") || address.equals("null") || address.equals("NA")) {
            addressrl.setVisibility(View.GONE);
        } else {
            addressrl.setVisibility(View.VISIBLE);
            addressText.setText(address);
        }

        String printBarcodefetaure=tb.getString("printbarcode");
        if(printBarcodefetaure.contains(taskType))
            print.setVisibility(View.VISIBLE);
        else
            print.setVisibility(View.GONE);

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(HyperLocalTaskDetailActivity.this,TaskPrintActivity.class);
                startActivity(i);
            }
        });



        //Action for call cutomer
        call_custmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number="";
                if(phone.equals("null") || phone.equals("") || phone==null ) {
                    //number = "+917836085727";
                  /*  manager =(TelephonyManager)TaskDetailActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                    try {
                        number = manager.getLine1Number();
                  }
                    catch (NullPointerException e)
                    {
                        number="917836085739";
                    }*/
                    Toast.makeText(HyperLocalTaskDetailActivity.this,"Don't have  mobile number.",Toast.LENGTH_SHORT).show();

                }
                else{
                    number=phone;

                }
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" +number));
                startActivity(intent);

            }
        });

    }
   private void showtasktype() {
        switch (taskType) {
            case "OPK":
                doneOrpicked.setText("Picked");
                break;
            case "FWD":
                doneOrpicked.setText("Done");
                break;
            case "RPK":
                doneOrpicked.setText("Picked");
                break;
            case "FPK":
                doneOrpicked.setText("Picked");
                break;
            case "RED":
                doneOrpicked.setText("Done");
                break;
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

    public void click_delivered_or_pick(View view) {
        showActionDialog("success");
    }

   /* public void click_delivered(View view)
    {
       showActionDialog("success");
    }
*/
    public void click_failed(View view) {
        showActionDialog("fail");
    }
    /*public void click_cancel(View view)
    {
        click_taskstatus="CAN";
        new Get_ResponseFromServer().execute();
    }
    public void click_doorstep(View view)
    {
            click_taskstatus="ARD";
            new Get_ResponseFromServer().execute();
    }*/
    Spinner dropdown;
    ImageView imageView;
    RelativeLayout signaturelayout;
    Button task_button_ok;
    EditText pickupqty;
    EditText amount;
    LinearLayout datetimerl;
    EditText rdate;
    EditText rtime;
    EditText  comment_text;
    public void showActionDialog(final String action_type){
        filepath=null;
        signfilepath=null;
        //creating dialog object
        final Dialog dil = new Dialog(HyperLocalTaskDetailActivity.this);
        //hiding default title bar of dialog
        dil.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dil.setContentView(R.layout.task_action_popu);
        dil.getWindow().getAttributes().width= WindowManager.LayoutParams.MATCH_PARENT;
        dil.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dil.show(); //to show dialog box

        pickupqty=(EditText)dil.getWindow().findViewById(R.id.pickupqty);
        amount=(EditText)dil.getWindow().findViewById(R.id.amountp);
        EditText Cda_id=(EditText)dil.getWindow().findViewById(R.id.cdaid);
        Spinner iddropdown= (Spinner)dil.getWindow().findViewById(R.id.ids);
        Cda_id.setVisibility(View.GONE);
        iddropdown.setVisibility(View.GONE);

        if(CollectableAmount.equals("")){
            amount.setVisibility(View.GONE);
        }
        else{
            amount.setVisibility(View.VISIBLE);
        }
        EditText pinno=(EditText)dil.getWindow().findViewById(R.id.pinno);
        comment_text=(EditText)dil.getWindow().findViewById(R.id.comment_text);
        pinno.setVisibility(View.GONE);
        if(action_type.equals("fail")){
            pickupqty.setVisibility(View.GONE);
            amount.setVisibility(View.GONE);
        }
        else{
            comment_text.setVisibility(View.GONE);
            String noofitemsfetaure=tb.getString("noofitemsfetaure");
            if(noofitemsfetaure.contains(taskType))
                pickupqty.setVisibility(View.VISIBLE);
            else
                pickupqty.setVisibility(View.GONE);
        }

        task_button_ok=(Button)dil.getWindow().findViewById(R.id.task_button_ok);
        datetimerl = (LinearLayout)dil.getWindow().findViewById(R.id.datetimerl);
        rdate=(EditText)dil.getWindow().findViewById(R.id.rdate);
        rtime=(EditText)dil.getWindow().findViewById(R.id.rtime);
        imageView = (ImageView)dil.getWindow().findViewById(R.id.imageView);
        dropdown = (Spinner)dil.getWindow().findViewById(R.id.dispositions);
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
        GPSChecker.GPSCheck(HyperLocalTaskDetailActivity.this,false);
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
                    Intent intent=new Intent(HyperLocalTaskDetailActivity.this, CaptureSignature.class);
                    startActivityForResult(intent,SIGNATURE_ACTIVITY);
                }
            });

            signclicktextview =(TextView) dil.getWindow().findViewById(R.id.signclicktextview);//abir
            signclickedimage =(ImageView) dil.getWindow().findViewById(R.id.signclickedimage);//abir
        }
        else{
            signaturelayout.setVisibility(View.GONE);
        }

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    boolean cmshowOrNot = false;
                    boolean showOrNot = false;
                    boolean showDatetimeOrNot = false;
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
        rdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(HyperLocalTaskDetailActivity.this, new DatePickerDialog.OnDateSetListener() {

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

                TimePickerDialog tpd = new TimePickerDialog(HyperLocalTaskDetailActivity.this,new TimePickerDialog.OnTimeSetListener() {
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

                String CodAmount="0";
                String reasonText="";
                String rescheduledDate="";
                String rescheduledTime="";
                try {
                    CodAmount=amount.getText().toString();
                    reasonText = dropdown.getSelectedItem().toString();
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
                    Toast.makeText(HyperLocalTaskDetailActivity.this, "Please enter no of items", Toast.LENGTH_LONG).show();
                }
                else if (amount.getVisibility() == View.VISIBLE && (amount.getText().toString().equals(""))) {
                    Toast.makeText(HyperLocalTaskDetailActivity.this, "Please enter valid amount", Toast.LENGTH_LONG).show();
                }
                else if (datetimerl.getVisibility() == View.VISIBLE &&  rescheduledDate.equals("")) {
                    Toast.makeText(HyperLocalTaskDetailActivity.this, "Please enter reschedule date", Toast.LENGTH_LONG).show();
                }
                else if (datetimerl.getVisibility() == View.VISIBLE && rescheduledTime.equals("")) {
                    Toast.makeText(HyperLocalTaskDetailActivity.this, "Please enter reschedule time", Toast.LENGTH_LONG).show();
                }
                else if (imageView.getVisibility() == View.VISIBLE && filepath==null) {
                    Toast.makeText(HyperLocalTaskDetailActivity.this, "Please capture image", Toast.LENGTH_LONG).show();
                }
                else if(signaturelayout.getVisibility() == View.VISIBLE && signfilepath==null){
                    Toast.makeText(HyperLocalTaskDetailActivity.this, "Please capture Signature", Toast.LENGTH_LONG).show();
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
                    saveTaskActionEvent(action_type,Integer.parseInt(pickupqtyText),reasonText.trim(),rescheduleDateTime,CodAmount);
                    //Toast.makeText(TaskDetailActivity.this, "All Good", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static String gactionType="";
    public void saveTaskActionEvent(String action_type,int qty,String selectedreason,String rescheduleDateTime,String CodAmount){
        try {
            gactionType=action_type;
            String tstatus="";
            if(action_type.equals("fail")){
                tstatus="CLD";
            }
            else if(action_type.equals("cancel")){
                tstatus="CAN";
            }
            else if(action_type.equals("doorstep")){
                tstatus="ARD";
            }
            else{
                tstatus="CMT";
            }

            //update task table status from pending to complete
            JSONObject taskUpdateparams = new JSONObject();
            taskUpdateparams.put("taskId", taskId);
            taskUpdateparams.put("amount", CodAmount);
            taskUpdateparams.put("status", tstatus);
            taskUpdateparams.put("comments", comments);
            taskUpdateparams.put("qty", qty);
            taskUpdateparams.put("reason", selectedreason);
            taskUpdateparams.put("rescheduleDateTime", rescheduleDateTime);
            taskUpdateparams.put("securePin", null);
            taskUpdateparams.put("pinVerified", null);


            Long res=db.insertFieldEvent(tripId,tripExpiryDateTime,"taskupdate", EventUpdateService.mlocation, taskUpdateparams.toString(),HyperLocalTaskDetailActivity.this);
            if(res!=0 && action_type.equals("fail")){
                db.updateTaskStatus(Constants.task_status_failed_code,Long.parseLong(taskId));
            }
            else if(res!=0 && action_type.equals("success")){
                db.updateTaskStatus(Constants.task_status_done_code,Long.parseLong(taskId));
            }
            else if(res!=0 && action_type.equals("doorstep")){
                db.updateTaskStatus(Constants.task_status_doorstep_code,Long.parseLong(taskId));
            }
            else if(res!=0 && action_type.equals("cancel")){
                db.updateTaskStatus(Constants.task_status_pending_code,Long.parseLong(taskId));
            }

            if (signfilepath!=null) {
                taskUpdateparams.put("filename", CaptureSignature.mypath.getPath());
                db.insertFieldEvent(tripId,tripExpiryDateTime,"imgupload", EventUpdateService.mlocation, taskUpdateparams.toString(),HyperLocalTaskDetailActivity.this);
            }

            if (filepath != null) {
                taskUpdateparams.put("filename", filepath);
                db.insertFieldEvent(tripId,tripExpiryDateTime,"imgupload", EventUpdateService.mlocation, taskUpdateparams.toString(),HyperLocalTaskDetailActivity.this);
            }

            if(action_type.equals("doorstep") || action_type.equals("ARD"))
            {}
            else {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            }
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
    //checkinng battery level
  /*  private int batteryLevel(Context context)
    {
        int level   = 0;
        int scale   = 0;
        try {
            Intent intent  = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            level   = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            scale   = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (level*100)/scale;
    }
*/
    //Async class for getting response data from server on start
   /* private ProgressDialog Dialog = null;
    private class Get_ResponseFromServer extends AsyncTask<Void, Void, Void>
    {
        String Result="";
        protected void onPreExecute()
        {
            //do nothing
            Dialog = new ProgressDialog(HyperLocalTaskDetailActivity.this);
            Dialog.setMessage("Please wait...");
            Dialog.show();
            Dialog.setCanceledOnTouchOutside(false);
            Dialog.setCancelable(false);
        }
        protected void onPostExecute(Void unused)
        {
            if(Dialog != null && Dialog.isShowing())
            {
                try {
                    Dialog.dismiss();
                    if (ServerInterface.checkserver) {
                            JSONObject mydata = new JSONObject(Result);
                            String res = mydata.getString("Result");
                            Boolean status = mydata.getBoolean("status");
                            if (status && res.equals("OK")) {
                                if (click_taskstatus.equals("ARD"))
                                {
                                    db.updateTaskStatus(Constants.task_status_doorstep_code, Long.parseLong(taskId));
                                }
                                else if(click_taskstatus.equals("CAN")) {
                                    db.updateTaskStatus(Constants.task_status_pending_code, Long.parseLong(taskId));
                                    finish();
                                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                                }
                            }
                            else{
                                if (click_taskstatus.equals("ARD"))
                                    saveTaskActionEvent("doorstep",0,"","","");
                                else
                                    saveTaskActionEvent("cancel",0,"","","");
                            }

                    } else {
                        if (click_taskstatus.equals("ARD"))
                            saveTaskActionEvent("doorstep",0,"","","");
                        else
                            saveTaskActionEvent("cancel",0,"","","");
                    }
                    //findViewById(R.id.task_button_print).setVisibility(View.VISIBLE);
                    findViewById(R.id.task_button_doorstep).setVisibility(View.GONE);
                    findViewById(R.id.task_button_cancel).setVisibility(View.GONE);
                    findViewById(R.id.task_button_delivered).setVisibility(View.VISIBLE);
                    findViewById(R.id.task_button_faild).setVisibility(View.VISIBLE);
                    showtasktype();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        protected Void doInBackground(Void... par) {
            try {
                String url = Constants.ServerApiUrl + "mobile/TaskEventUpdate";
                JSONObject params = new JSONObject();
                //params.put("token", tinydb.getString("loginToken"));
                if (tb == null)
                    tb = new TinyDB(MainActivity.mcontext);

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
                taskUpdateparams.put("status", click_taskstatus);
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
                String loginToken = tb.getString("loginToken");
                jo.put("token", loginToken);
                jo.put("push", "yes");
                jo.put("updates", allparams);
                jo.put("reqId", taskId);

                JSONArray eventsUpdateArray = jo.getJSONArray("updates");
                if (eventsUpdateArray.length() > 0) {
                    String iemino = tb.getString("iemino");
                    Result = ServerInterface.CallServerApi(jo, url, 10, loginToken,iemino);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
*/
}
