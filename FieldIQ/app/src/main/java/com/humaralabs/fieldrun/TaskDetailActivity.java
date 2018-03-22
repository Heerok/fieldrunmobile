package com.humaralabs.fieldrun;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
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
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.humaralabs.fieldrun.adapter.QuesListAdapter;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.Disposition;
import com.humaralabs.fieldrun.datastructure.QuestionSet;
import com.humaralabs.fieldrun.datastructure.Review;
import com.humaralabs.fieldrun.server.ServerInterface;
import com.humaralabs.fieldrun.service.EventUpdateService;

import org.json.JSONArray;
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


public class TaskDetailActivity extends ActionBarActivity {

    private static final String TAG = "TDA";
    public static final int SIGNATURE_ACTIVITY = 2;//abir
    private String filepath;
    private String filepath2;
    private String filepath3;
    private String filepath4;
    private String filepath5;
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
    String payment_mode = "";
    String consigneeNumber;
    String codamount;
    String plateformid;
    String category = "";
    String description = "";
    DbAdapter db;
    TinyDB tb;
    Toolbar toolbar;
    //count for photo capture
    String mandatoryPhotocount = "";
    String optionPhotocount = "";
    int totalPhotocount;
    int count = 1;

    public static boolean doneReview = false;
    public boolean QcSuccessOrNot = false;
    public boolean QcFailOrPass = false;
    public boolean QcFail = false;
    public static ArrayList<QuestionSet> QuestionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        CaptureSignature.mypath = null;
        Log.d("CLICK", "Loading task");
        db = new DbAdapter(this);
        tb = new TinyDB(TaskDetailActivity.this);
        QuestionList = new ArrayList<QuestionSet>();
        Intent detailIntent = getIntent();
        tripId = detailIntent.getExtras().get("tripId").toString();
        tripExpiryDateTime = detailIntent.getExtras().get("tripExpiryDateTime").toString();
        taskId = detailIntent.getExtras().get("taskId").toString();
        ref = detailIntent.getExtras().get("ref").toString();
        orderid = ref;
        name = detailIntent.getExtras().get("name").toString();
        pickups = detailIntent.getExtras().get("pickups").toString();
        address = detailIntent.getExtras().get("address").toString();
        zipCode = detailIntent.getExtras().get("zipCode").toString();
        phone = detailIntent.getExtras().get("phone").toString();
        taskType = detailIntent.getExtras().get("taskType").toString();
        taskfacility = detailIntent.getExtras().get("tripFacility").toString();
        pickupQty = detailIntent.getExtras().get("pickupQty").toString();
        reason = detailIntent.getExtras().get("reason").toString();
        delieveryDateTime = detailIntent.getExtras().get("delieveryDateTime").toString();
        comments = detailIntent.getExtras().get("comments").toString();
        payment_mode = detailIntent.getExtras().get("payment_mode").toString();
        status = detailIntent.getExtras().get("status").toString();
        consigneeNumber = detailIntent.getExtras().get("consigneeNumber").toString();
        codamount = detailIntent.getExtras().get("codamount").toString();
        category = detailIntent.getExtras().get("category").toString();
        description = detailIntent.getExtras().get("description").toString();
        plateformid = detailIntent.getExtras().get("platformId").toString();
        mandatoryPhotocount = detailIntent.getExtras().get("mandatoryPhotocount").toString();
        optionPhotocount = detailIntent.getExtras().get("optionPhotocount").toString();

        //getting total no of image to capture
        if (!mandatoryPhotocount.equals("") && !optionPhotocount.equals("") && !mandatoryPhotocount.equals(null) && !optionPhotocount.equals(null))
            totalPhotocount = Integer.parseInt(mandatoryPhotocount) + Integer.parseInt(optionPhotocount);

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

        TextView amount = (TextView) findViewById(R.id.amount);
        TextView taskrefText = (TextView) findViewById(R.id.taskref);
        TextView codeidText = (TextView) findViewById(R.id.codeid);
        TextView tasktypeText = (TextView) findViewById(R.id.tasktype);
        TextView tripidText = (TextView) findViewById(R.id.tripid);
        TextView nameText = (TextView) findViewById(R.id.name);
        TextView mobileText = (TextView) findViewById(R.id.mobile);
        TextView zipcodeText = (TextView) findViewById(R.id.zipcode);
        TextView addressText = (TextView) findViewById(R.id.address);

        print = (Button) findViewById(R.id.task_button_print);
        doneOrpicked = (Button) findViewById(R.id.task_button_delivered);
        call_custmer = (ImageView) findViewById(R.id.callimage);

        doorstep = (Button) findViewById(R.id.task_button_doorstep);
        cancel = (Button) findViewById(R.id.task_button_cancel);


        showtasktype();
        //setting taskstatus
       /* if (Constants.task_status_start_code==Integer.parseInt(status)) {
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


        //setting task ref
        if (ref == null || ref.equals("") || ref.equals("null") || ref.equals("NA")) {
            taskrefText.setVisibility(View.GONE);
        } else {
            taskrefText.setVisibility(View.VISIBLE);
            taskrefText.setText("Task ref - " + ref);
        }

        //setting task type
        if (taskType == null || taskType.equals("") || taskType.equals("null") || taskType.equals("NA")) {
            tasktypeText.setVisibility(View.GONE);
        } else {
            tasktypeText.setVisibility(View.VISIBLE);
            tasktypeText.setText("Task type - " + taskType);
        }

        //setting tripid
        if (tripId == null || tripId.equals("") || tripId.equals("null") || tripId.equals("NA")) {
            triprl.setVisibility(View.GONE);
        } else {
            triprl.setVisibility(View.VISIBLE);
            tripidText.setText("Trip no - " + tripId);
        }

        //setting seller name
        if (name == null || name.equals("") || name.equals("null") || name.equals("NA")) {
            namerl.setVisibility(View.GONE);
        } else {
            namerl.setVisibility(View.VISIBLE);
            nameText.setText("Seller name - " + name);
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

        if (payment_mode.equals("COD") && (!taskType.equals("FPK") && !taskType.equals("RPK"))) {
            codeidText.setVisibility(View.VISIBLE);
            codeidText.setText("COD Id - " + taskId);
            amount.setText("Rs. " + codamount);
        } else {

            codeidText.setVisibility(View.GONE);
            amount.setVisibility(View.GONE);

        }
        // reverse task reason
        if (reason == null || reason.equals("") || reason.equals("null") && taskType.equals("RPK")) {
            codeidText.setVisibility(View.GONE);
            //amount.setVisibility(View.GONE);
        } else {
            codeidText.setVisibility(View.VISIBLE);
            codeidText.setText("Reason - " + reason);
            codeidText.setTextColor(Color.RED);
        }
        //item description
        if (description == null || description.equals("") || description.equals("null") && taskType.equals("RPK")) {
            //codeidText.setVisibility(View.GONE);
            amount.setVisibility(View.GONE);
        } else {
            amount.setVisibility(View.VISIBLE);
            amount.setText(description);
            //amount.setTextColor(Color.CYAN);
        }

        //setting address
        if (address == null || address.equals("") || address.equals("null") || address.equals("NA")) {
            addressrl.setVisibility(View.GONE);
        } else {
            addressrl.setVisibility(View.VISIBLE);
            addressText.setText(address);
        }

        String printBarcodefetaure = tb.getString("printbarcode");
        if (printBarcodefetaure.contains(taskType))
            print.setVisibility(View.VISIBLE);
        else
            print.setVisibility(View.GONE);

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TaskDetailActivity.this, TaskPrintActivity.class);
                startActivity(i);
            }
        });


        //Action for call cutomer
        call_custmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number;
                if (consigneeNumber.equals("null") || consigneeNumber.equals("") || consigneeNumber == null) {

                    Toast.makeText(TaskDetailActivity.this, "Don't have customer mobile number.", Toast.LENGTH_SHORT).show();

                } else {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + consigneeNumber));
                    startActivity(intent);


                }

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
            case "RTV":
                doneOrpicked.setText("Returned");
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
    }*/
    public void click_failed(View view) {
        showActionDialog("fail");
    }

    public void click_cancel(View view) {
        click_taskstatus = "CAN";
        new Get_ResponseFromServer().execute();
    }

    public void click_doorstep(View view) {
        click_taskstatus = "ARD";
        new Get_ResponseFromServer().execute();
    }

    float rate;
    Spinner dropdown;
    Spinner iddropdown;
    ImageView imageView;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;
    ImageView imageView5;
    RelativeLayout signaturelayout;
    RelativeLayout questionlayout;
    Button task_button_ok;
    Button task_ques_ok;
    Button next;
    Button prev;
    //Button camera_btn;
    EditText pickupqty;
    EditText pickupBagqty;
    EditText pinno;
    LinearLayout datetimerl;
    EditText rdate;
    EditText rtime;
    EditText comment_text;
    EditText amountp;
    EditText Cda_id;

    // FrameLayout id_framelayout;
    public static ListView ques_list;
    ScrollView scrlview;
    TextView title;
    TextView txtquestion;
    TextView txt_count;
    TextView txt_reason;
    TextView txt_description;
    TextView txt_addmore;
    int pos;
    String question;
    JSONObject answers = new JSONObject();
    boolean showOrNot = false;
    boolean validOrNot = false;
    final ArrayList<String> id_name = new ArrayList<String>();
    String CodRecieveType;
    String id_value;
    String id_type;

    //switch
    Switch simpleSwitch;
    ArrayList<String> CheckQuesStatus = new ArrayList<String>();
    ArrayList<String> CheckQuesReview = new ArrayList<String>();
    ArrayList<String> CheckAnsReview = new ArrayList<String>();
    //creating dialog object
    Dialog dil;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void showActionDialog(final String action_type) {
        filepath = null;
        signfilepath = null;
        dil = new Dialog(TaskDetailActivity.this);
        //hiding default title bar of dialog
        dil.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dil.setContentView(R.layout.task_action_popu);
        dil.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        dil.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dil.setCanceledOnTouchOutside(false);
        dil.show(); //to show dialog box

        //id_framelayout=(FrameLayout)dil.getWindow().findViewById(R.id.framecdaid);
        //camera_btn=(Button)dil.getWindow().findViewById(R.id.btn_camera);
        //task plateform id
        if (category != null || !category.equals("") || !category.equals("null") || !category.equals("NA"))
            QuestionList = db.getAllQuestionSet(plateformid, category);
        else
            QuestionList = db.getAllQuestionSet(plateformid, "");

        Cda_id = (EditText) dil.getWindow().findViewById(R.id.cdaid);
        amountp = (EditText) dil.getWindow().findViewById(R.id.amountp);
        title = (TextView) dil.getWindow().findViewById(R.id.text);
        txt_addmore = (TextView) dil.getWindow().findViewById(R.id.addmore);
        pickupqty = (EditText) dil.getWindow().findViewById(R.id.pickupqty);
        pickupBagqty = (EditText) dil.getWindow().findViewById(R.id.pickupBagqty);
        pinno = (EditText) dil.getWindow().findViewById(R.id.pinno);
        comment_text = (EditText) dil.getWindow().findViewById(R.id.comment_text);
        task_ques_ok = (Button) dil.getWindow().findViewById(R.id.task_ques_ok);
        questionlayout = (RelativeLayout) dil.getWindow().findViewById(R.id.qrl);


        ques_list = (ListView) dil.getWindow().findViewById(R.id.list_ques);
        scrlview = (ScrollView) dil.getWindow().findViewById(R.id.popupscrol);

        simpleSwitch = (Switch) dil.getWindow().findViewById(R.id.simpleSwitch);
        next = (Button) dil.findViewById(R.id.task_next);
        prev = (Button) dil.findViewById(R.id.task_pre);
        txt_count = (TextView) dil.findViewById(R.id.text_count);
        txtquestion = (TextView) dil.findViewById(R.id.text_question);
        txt_description = (TextView) dil.findViewById(R.id.questDescription);
        txt_reason = (TextView) dil.findViewById(R.id.questReason);
        simpleSwitch.setTextOn("yes"); // displayed text of the Switch whenever it is in checked or on state
        simpleSwitch.setTextOff("no");
        simpleSwitch.setShowText(true);


        pinno.setVisibility(View.GONE);
        RatingBar ratingBar = (RatingBar) dil.getWindow().findViewById(R.id.ratingBar);
        if (action_type.equals("fail")) {
            QcSuccessOrNot = true;
            pickupBagqty.setVisibility(View.GONE);
            pickupqty.setVisibility(View.GONE);
            ratingBar.setVisibility(View.GONE);
            questionlayout.setVisibility(View.GONE);
            scrlview.setVisibility(View.VISIBLE);
        } else if (action_type.equals("image")) {
            showOrNot = true;
            QcSuccessOrNot = false;
            pickupqty.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            scrlview.setVisibility(View.VISIBLE);
            questionlayout.setVisibility(View.GONE);
        } else {
            if (QuestionList.size() > 0 && taskType.equals("RPK") && !action_type.equals("image")) {
                scrlview.setVisibility(View.GONE);
                questionlayout.setVisibility(View.VISIBLE);
            }
            comment_text.setVisibility(View.GONE);

            //no of item feature
            String noofBagsfetaure = tb.getString("noofbagsfetaure");
            if (noofBagsfetaure.contains(taskType))
                pickupBagqty.setVisibility(View.VISIBLE);
            else
                pickupBagqty.setVisibility(View.GONE);


            //noof bag feature
            String noofitemsfetaure = tb.getString("noofitemsfetaure");
            if (noofitemsfetaure.contains(taskType))
                pickupqty.setVisibility(View.VISIBLE);
            else
                pickupqty.setVisibility(View.GONE);

            String ratingType = tb.getString("ratingtype");
            String ratingfacility = tb.getString("ratingfacility");

            if (ratingType.contains(taskType) && ratingfacility.contains(taskfacility)) {
                ratingBar.setVisibility(View.VISIBLE);
            } else {
                ratingBar.setVisibility(View.GONE);
            }


        }

        task_button_ok = (Button) dil.getWindow().findViewById(R.id.task_button_ok);
        datetimerl = (LinearLayout) dil.getWindow().findViewById(R.id.datetimerl);
        rdate = (EditText) dil.getWindow().findViewById(R.id.rdate);
        rtime = (EditText) dil.getWindow().findViewById(R.id.rtime);
        imageView = (ImageView) dil.getWindow().findViewById(R.id.imageView);
        imageView2 = (ImageView) dil.getWindow().findViewById(R.id.imageView2);
        imageView3 = (ImageView) dil.getWindow().findViewById(R.id.imageView3);
        imageView4 = (ImageView) dil.getWindow().findViewById(R.id.imageView4);
        imageView5 = (ImageView) dil.getWindow().findViewById(R.id.imageView5);
        dropdown = (Spinner) dil.getWindow().findViewById(R.id.dispositions);
        iddropdown = (Spinner) dil.getWindow().findViewById(R.id.ids);

        if (QuestionList.size() > 0 && taskType.equals("RPK") && !action_type.equals("image")) {

            txt_reason.setVisibility((pos + 1)%2==1?View.GONE:View.VISIBLE);
            txt_reason.setText((pos + 1)==2?"Expected Quantity - "+pickups:"Reason - " + reason);
            txt_description.setText(description);
            txt_count.setText(String.valueOf(pos + 1) + " - ");
            simpleSwitch.setChecked(Boolean.parseBoolean(QuestionList.get(pos).answerType));
            txtquestion.setText(QuestionList.get(pos).question);
            if (pos >= 0)
                prev.setEnabled(true);
            else
                prev.setEnabled(false);

            //pos = 1;
            /*QuesListAdapter quesListAdapter = new QuesListAdapter(this, QuestionList, R.layout.item_quest);
            ques_list.setAdapter(quesListAdapter);
            quesListAdapter.notifyDataSetChanged();*/
        } else if (taskType.equals("RPK") && action_type.equals("image")) {
            showOrNot = true;
            QcSuccessOrNot = false;
            imageView.setVisibility(View.VISIBLE);
            scrlview.setVisibility(View.VISIBLE);
            questionlayout.setVisibility(View.GONE);
        } else {
            scrlview.setVisibility(View.VISIBLE);
            questionlayout.setVisibility(View.GONE);
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos < QuestionList.size() - 1) {
                    pos++;
                    prev.setEnabled(true);
                    txt_count.setText(String.valueOf(pos + 1) + " - ");
                    txtquestion.setText(QuestionList.get(pos).question);
                    simpleSwitch.setChecked(Boolean.parseBoolean(QuestionList.get(pos).answerType));
                    txt_reason.setVisibility((pos + 1)%2==1?View.GONE:View.VISIBLE);
                    txt_reason.setText((pos + 1)==2?"Expected Quantity - "+pickups:"Reason - " + reason);
                    txt_description.setText(description);

                } else if (next.getText().equals("Finish")) {
                    // dil.dismiss();
                    QcFailOrPassCheck();
                    Intent review = new Intent(TaskDetailActivity.this, ReviewPageActivity.class);
                    //review.putExtra("QuestionList", QuestionList);
                    // review.putExtra("AnswerList", CheckAnsReview);
                    startActivity(review);

                } else {
                    txt_count.setText(String.valueOf(pos + 1) + " - ");
                    txtquestion.setText(QuestionList.get(pos).question);
                    simpleSwitch.setChecked(Boolean.parseBoolean(QuestionList.get(pos).answerType));
                    next.setText("Finish");
                    txt_reason.setVisibility((pos + 1)%2==1?View.GONE:View.VISIBLE);
                    txt_reason.setText((pos + 1)==2?"Expected Quantity - "+pickups:"Reason - " + reason);
                    txt_description.setText(description);
                }
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos > 0) {
                    pos--;
                    prev.setEnabled(true);
                    next.setText("Next");
                    simpleSwitch.setChecked(Boolean.parseBoolean(QuestionList.get(pos).answerType));
                    txt_count.setText(String.valueOf(pos + 1) + " - ");
                    txtquestion.setText(QuestionList.get(pos).question);
                    txt_reason.setVisibility((pos + 1)%2==1?View.GONE:View.VISIBLE);
                    txt_reason.setText((pos + 1)==2?"Expected Quantity - "+pickups:"Reason - " + reason);
                    txt_description.setText(description);

                } else {
                    txt_count.setText(String.valueOf(pos + 1) + " - ");
                    simpleSwitch.setChecked(Boolean.parseBoolean(QuestionList.get(pos).answerType));
                    txtquestion.setText(QuestionList.get(pos).question);
                    prev.setEnabled(false);
                    txt_reason.setVisibility((pos + 1)%2==1?View.GONE:View.VISIBLE);
                    txt_reason.setText((pos + 1)==2?"Expected Quantity - "+pickups:"Reason - " + reason);
                    txt_description.setText(description);
                }
            }
        });
        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //QuestionSet questionSet = QuestionList.get((Integer) buttonView.getTag());
                QuestionList.get(pos).answerType = "" + isChecked;
                //Toast.makeText(TaskDetailActivity.this,""+QuestionList.get(pos).question+":"+QuestionList.get(pos).answerType,Toast.LENGTH_SHORT).show();
            }
        });
        txt_addmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check for optonal image for QC FAIL AND QC PASS //&& !QcSuccessOrNot
                if (count < totalPhotocount) {
                    if (count == 1)
                        imageView2.setVisibility(View.VISIBLE);
                    else if (count == 2)
                        imageView3.setVisibility(View.VISIBLE);
                    else if (count == 3)
                        imageView4.setVisibility(View.VISIBLE);
                    else if (count == 4)
                        imageView5.setVisibility(View.VISIBLE);
                    count++;
                } else {
                    txt_addmore.setVisibility(View.GONE);
                }
            }
        });
        /*task_ques_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                QcFailOrPassCheck();
                if(CheckQuesStatus.contains("false"))
                {
                    CheckQuesStatus.clear();
                    Toast.makeText(TaskDetailActivity.this,"Please give all answer of question!",Toast.LENGTH_LONG).show();
                }
                else if(showOrNot){
                    if(doneReview) {
                        pickupqty.setVisibility(View.VISIBLE);
                        comment_text.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                        scrlview.setVisibility(View.VISIBLE);
                        questionlayout.setVisibility(View.GONE);
                        QcSuccessOrNot = false;
                        doneReview =false;
                        } else {
                            Intent review = new Intent(TaskDetailActivity.this, ReviewPageActivity.class);
                            review.putExtra("QuestionList", CheckQuesReview);
                            review.putExtra("AnswerList", CheckAnsReview);
                            startActivity(review);
                            CheckQuesStatus.clear();
                            CheckQuesReview.clear();
                            CheckAnsReview.clear();
                        }
                    showOrNot=false;
                }
                else
                {
                    if(doneReview) {
                        scrlview.setVisibility(View.VISIBLE);
                        questionlayout.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        signaturelayout.setVisibility(View.VISIBLE);
                        QcSuccessOrNot = true;
                        QcFail = false;
                        doneReview =false;
                    } else {
                        Intent review = new Intent(TaskDetailActivity.this, ReviewPageActivity.class);
                        review.putExtra("QuestionList", CheckQuesReview);
                        review.putExtra("AnswerList", CheckAnsReview);
                        startActivity(review);
                        CheckQuesStatus.clear();
                        CheckQuesReview.clear();
                        CheckAnsReview.clear();
                    }
                }

            }
        });*/


        amountp.setVisibility(View.GONE);
        Cda_id.setVisibility(View.GONE);
        iddropdown.setVisibility(View.GONE);

        final ArrayList<Disposition> dispositions = db.getAllDisposition(taskType, action_type);
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


        GPSChecker.GPSCheck(TaskDetailActivity.this, false);
        signaturelayout = (RelativeLayout) dil.getWindow().findViewById(R.id.signaturelayout);//abir
        if (!action_type.equals("fail")) {
            String signaturecaptureType = tb.getString("signaturecapture");
            String signaturecapturefacility = tb.getString("signaturecapturefacility");
            if (signaturecaptureType.contains(taskType) && signaturecapturefacility.contains(taskfacility) && QuestionList.size() <= 0) {

                signaturelayout.setVisibility(View.VISIBLE);
                QcSuccessOrNot = true;
            } else
                signaturelayout.setVisibility(View.GONE);

            signaturelayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TaskDetailActivity.this, CaptureSignature.class);
                    startActivityForResult(intent, SIGNATURE_ACTIVITY);
                }
            });

            signclicktextview = (TextView) dil.getWindow().findViewById(R.id.signclicktextview);//abir
            signclickedimage = (ImageView) dil.getWindow().findViewById(R.id.signclickedimage);//abir

            CodRecieveType = tb.getString("codtype");

            if (CodRecieveType.equals(taskType) && payment_mode.equals("COD")) {
                amountp.setVisibility(View.VISIBLE);
            }

            id_type = tb.getString("idstype");
            if (id_type.equals(taskType) && payment_mode.equals("COD")) {
                Cda_id.setVisibility(View.VISIBLE);
                iddropdown.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                id_value = tb.getString("idsvalue");
                final String[] idvalues = id_value.split(",");
                if (idvalues.length > 0) {
                    for (int i = 0; i < idvalues.length; i++) {
                        id_name.add(idvalues[i]);
                    }
                }
                ArrayAdapter<String> idadapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, id_name);
                iddropdown.setAdapter(idadapter);
            }
        } else {
            signaturelayout.setVisibility(View.GONE);
            //QcSuccessOrNot = false;
            Cda_id.setVisibility(View.GONE);
            iddropdown.setVisibility(View.GONE);
        }


        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                float rateValue = ratingBar.getRating();
                rate = rateValue;
                System.out.println("Rate for Module is" + rateValue);
            }
        });


        iddropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String idsname = id_name.get(position);

                if (idsname.equals("Pan Card")) {
                    Cda_id.setHint("Please enter pan card no.");
                } else if (idsname.equals("Adhar Card")) {
                    Cda_id.setHint("Please enter adhar card no.");
                } else if (idsname.equals("Voter id")) {
                    Cda_id.setHint("Please enter voter id no.");
                } else if (idsname.equals("Driving Licence")) {
                    Cda_id.setHint("Please enter driving licence no.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    boolean cmshowOrNot = false;
                    showOrNot = false;
                    boolean showDatetimeOrNot = false;
                    String disname = "";

                    disname = dispositions.get(position).name;
                    if (disname != null && !disname.isEmpty()) {
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
                } catch (Exception ex) {
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

                DatePickerDialog dpd = new DatePickerDialog(TaskDetailActivity.this, new DatePickerDialog.OnDateSetListener() {

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

                TimePickerDialog tpd = new TimePickerDialog(TaskDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
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
                        if (Cda_id.getVisibility() == View.VISIBLE)
                            photoFile = createImageFile("ids");
                        else
                            photoFile = createImageFile("task");
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
        //image 2
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        if (Cda_id.getVisibility() == View.VISIBLE)
                            photoFile = createImageFile("ids");
                        else
                            photoFile = createImageFile("task");
                        filepath = photoFile.getPath();
                        filepath2 = photoFile.getPath();
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
        //image 3
        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        if (Cda_id.getVisibility() == View.VISIBLE)
                            photoFile = createImageFile("ids");
                        else
                            photoFile = createImageFile("task");
                        filepath = photoFile.getPath();
                        filepath3 = photoFile.getPath();
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
        //image 4
        imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        if (Cda_id.getVisibility() == View.VISIBLE)
                            photoFile = createImageFile("ids");
                        else
                            photoFile = createImageFile("task");
                        filepath = photoFile.getPath();
                        filepath4 = photoFile.getPath();
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
        //image 5
        imageView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        if (Cda_id.getVisibility() == View.VISIBLE)
                            photoFile = createImageFile("ids");
                        else
                            photoFile = createImageFile("task");
                        filepath = photoFile.getPath();
                        filepath5 = photoFile.getPath();
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
        // after review page
        if (QcFailOrPass && doneReview) {
            pickupqty.setVisibility(View.VISIBLE);
            comment_text.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            scrlview.setVisibility(View.VISIBLE);
            questionlayout.setVisibility(View.GONE);
            QcFailOrPass = false;
            QcFail = true;
            QcSuccessOrNot = false;
            doneReview = false;
        } else if (!QcFailOrPass && doneReview) {
            scrlview.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            comment_text.setVisibility(View.VISIBLE);
            questionlayout.setVisibility(View.GONE);
            signaturelayout.setVisibility(View.VISIBLE);
            QcSuccessOrNot = true;
            doneReview = false;
            QcFail = false;
        }

        task_button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rating = 0;
                String CdaId;
                String ReceviedAmount;
                String pickupBagqtyText = "";//pickupqty.getText().toString();
                String pickupqtyText = "";
                if (action_type.equals("fail")) {
                    rating = 0;
                    pickupBagqtyText = "0";
                    pickupqtyText = "0";
                    ReceviedAmount = "0";
                    CdaId = "";
                    QcSuccessOrNot = true;
                } else {
                    rating = Math.round(rate);
                    String noofitemsfetaure = tb.getString("noofitemsfetaure");
                    if (noofitemsfetaure.contains(taskType))
                        pickupqtyText = pickupqty.getText().toString();
                    else
                        pickupqtyText = "1";

                    String noofbagsfetaure = tb.getString("noofbagsfetaure");
                    if (noofbagsfetaure.contains(taskType))
                        pickupBagqtyText = pickupBagqty.getText().toString();
                    else
                        pickupBagqtyText = "1";

                    ReceviedAmount = amountp.getText().toString();
                    CdaId = Cda_id.getText().toString();
                }

                String reasonText = "";
                String rescheduledDate = "";
                String rescheduledTime = "";
                try {
                    reasonText = dropdown.getSelectedItem().toString();
                    if (comment_text.getVisibility() == View.VISIBLE) {
                        reasonText = reasonText + " " + comment_text.getText().toString().trim();
                    }
                } catch (Exception e) {
                    reasonText = "";
                }
                if (comment_text.getVisibility() == View.VISIBLE && taskType.equals("RPK")) {
                    reasonText = comment_text.getText().toString().trim();
                }
                try {
                    rescheduledDate = rdate.getText().toString();
                    rescheduledTime = rtime.getText().toString();
                } catch (Exception e) {
                    rescheduledDate = "";
                    rescheduledTime = "";
                }

                String c_id_type = "";
                try {
                    c_id_type = iddropdown.getSelectedItem().toString();
                } catch (Exception e) {
                    c_id_type = "";
                }
                if (pickupBagqty.getVisibility() == View.VISIBLE && (pickupBagqtyText.equals("") || pickupBagqtyText.equals("0"))) {
                    Toast.makeText(TaskDetailActivity.this, "Please enter no of bags", Toast.LENGTH_LONG).show();
                } else if (pickupqty.getVisibility() == View.VISIBLE && (pickupqtyText.equals("") || pickupqtyText.equals("0"))) {
                    Toast.makeText(TaskDetailActivity.this, "Please enter no of items", Toast.LENGTH_LONG).show();
                } else if (datetimerl.getVisibility() == View.VISIBLE && rescheduledDate.equals("")) {
                    Toast.makeText(TaskDetailActivity.this, "Please enter reschedule date", Toast.LENGTH_LONG).show();
                } else if (datetimerl.getVisibility() == View.VISIBLE && rescheduledTime.equals("")) {
                    Toast.makeText(TaskDetailActivity.this, "Please enter reschedule time", Toast.LENGTH_LONG).show();
                } else if (Cda_id.getVisibility() == View.GONE && imageView.getVisibility() == View.VISIBLE && filepath == null) {
                    Toast.makeText(TaskDetailActivity.this, "Please capture image", Toast.LENGTH_LONG).show();
                } else if (count == 2 && imageView2.getVisibility() == View.VISIBLE && filepath2 == null && count <= Integer.parseInt(mandatoryPhotocount)) {
                    Toast.makeText(TaskDetailActivity.this, "Please capture image2", Toast.LENGTH_LONG).show();
                } else if (count == 3 && imageView3.getVisibility() == View.VISIBLE && filepath3 == null && count <= Integer.parseInt(mandatoryPhotocount)) {
                    Toast.makeText(TaskDetailActivity.this, "Please capture image3", Toast.LENGTH_LONG).show();
                } else if (count == 4 && imageView4.getVisibility() == View.VISIBLE && filepath4 == null && count <= Integer.parseInt(mandatoryPhotocount)) {
                    Toast.makeText(TaskDetailActivity.this, "Please capture image4", Toast.LENGTH_LONG).show();
                } else if (count == 5 && imageView5.getVisibility() == View.VISIBLE && filepath5 == null && count <= Integer.parseInt(mandatoryPhotocount)) {
                    Toast.makeText(TaskDetailActivity.this, "Please capture image5", Toast.LENGTH_LONG).show();
                } else if (signaturelayout.getVisibility() == View.VISIBLE && signfilepath == null && !QcSuccessOrNot || (signaturelayout.getVisibility() == View.VISIBLE && count >= Integer.parseInt(mandatoryPhotocount) && signfilepath == null)) {
                    Toast.makeText(TaskDetailActivity.this, "Please capture Signature", Toast.LENGTH_LONG).show();
                } else if (amountp.getVisibility() == View.VISIBLE && (ReceviedAmount.equals("") || ReceviedAmount.equals("0"))) {
                    Toast.makeText(TaskDetailActivity.this, "Please enter received amount.", Toast.LENGTH_LONG).show();
                } else if (amountp.getVisibility() == View.VISIBLE && Double.parseDouble(ReceviedAmount) != Double.parseDouble(codamount)) {
                    Toast.makeText(TaskDetailActivity.this, "Please enter valid received amount.", Toast.LENGTH_LONG).show();
                } else if (Cda_id.getVisibility() == View.VISIBLE && Integer.parseInt(ReceviedAmount) >= 50000 && (CdaId.equals("") || CdaId.equals("0"))) {
                    Toast.makeText(TaskDetailActivity.this, "Please enter pan card or id no.", Toast.LENGTH_LONG).show();
                } else if (comment_text.getVisibility() == View.VISIBLE && taskType.equals("RPK") && (reasonText.equals("") && !QcSuccessOrNot || reasonText.equals(" "))) {
                    Toast.makeText(TaskDetailActivity.this, "Please enter comment.", Toast.LENGTH_LONG).show();
                } else {
                    String originalString = rescheduledDate + " " + rescheduledTime;
                    Date date = null;
                    String rescheduleDateTime = "";
                    try {
                        if (!originalString.equals(" ")) {
                            date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(originalString);
                            rescheduleDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(date);
                        }
                    } catch (ParseException e) {
                        rescheduleDateTime = "";
                        e.printStackTrace();
                    }

                    //save into event table
                    saveTaskActionEvent(c_id_type, CdaId, ReceviedAmount, rating, action_type, Integer.parseInt(pickupBagqtyText), Integer.parseInt(pickupqtyText), reasonText.trim(), rescheduleDateTime, answers);
                    //Toast.makeText(TaskDetailActivity.this, "All Good", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void QcFailOrPassCheck() {
        for (int i = 0; i < QuestionList.size(); i++) {
            try {
                if (QuestionList.get(i).answerType.equals("Boolean")) {
                    validOrNot = false;
                    CheckQuesStatus.add("false");
                } else {
                    CheckQuesStatus.add("true");
                    validOrNot = true;
                    if (QuestionList.get(i).answerType.equals("false") && QuestionList.get(i).noEffect.equals("false")) {
                        showOrNot = true;
                        QcFailOrPass = true;
                        QcFail = true;
                    }
                    answers.put(QuestionList.get(i).questionId, QuestionList.get(i).answerType);
                    CheckQuesReview.add(QuestionList.get(i).question);
                    CheckAnsReview.add(QuestionList.get(i).answerType);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("MSG" + QuestionList.get(i).questionId, "" + QuestionList.get(i).question + ":Answer Type: " + QuestionList.get(i).answerType + ":NoEffect :" + QuestionList.get(i).noEffect);
        }

    }

    @Override
    protected void onResume() {
        if (doneReview) {
            dil.dismiss();
            showActionDialog("success");
        }
        super.onResume();
    }

    public static String gactionType = "";

    public void saveTaskActionEvent(String c_id_type, String C_id, String ReceviedAmount, int rating, String action_type, int bagqty, int qty, String selectedreason, String rescheduleDateTime, JSONObject QC) {
        try {
            gactionType = action_type;
            String tstatus = "";
            if (action_type.equals("fail")) {
                tstatus = "CLD";
            } else if (action_type.equals("cancel")) {
                tstatus = "CAN";
            } else if (action_type.equals("doorstep")) {
                tstatus = "ARD";
            } else {
                tstatus = "CMT";
            }

            //update task table status from pending to complete
            JSONObject taskUpdateparams = new JSONObject();
            taskUpdateparams.put("taskId", taskId);
            taskUpdateparams.put("status", tstatus);
            taskUpdateparams.put("comments", comments);
            taskUpdateparams.put("qty", qty);
            taskUpdateparams.put("bagqty", bagqty);
            taskUpdateparams.put("reason", selectedreason);
            taskUpdateparams.put("rating", rating);
            taskUpdateparams.put("receivedAmount", ReceviedAmount);
            taskUpdateparams.put("cid", C_id);
            taskUpdateparams.put("cIdType", c_id_type);
            taskUpdateparams.put("rescheduleDateTime", rescheduleDateTime);
            taskUpdateparams.put("securePin", null);
            taskUpdateparams.put("pinVerified", null);
            taskUpdateparams.put("qc", QC);
            Long res=db.insertFieldEvent(tripId,tripExpiryDateTime,"taskupdate", EventUpdateService.mlocation, taskUpdateparams.toString(),TaskDetailActivity.this);
            if(res!=0 && action_type.equals("fail")){
                db.updateTaskStatus(Constants.task_status_failed_code,Long.parseLong(taskId));
            }
            else if(res!=0 && action_type.equals("success") &&(taskType.equals("RPK") && QcFail))
            {db.updateTaskStatus(Constants.task_status_qc_fail_code,Long.parseLong(taskId));
            }
            else if(res!=0 && action_type.equals("success") && !QcFail){
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
                db.insertFieldEvent(tripId,tripExpiryDateTime,"imgupload", EventUpdateService.mlocation, taskUpdateparams.toString(),TaskDetailActivity.this);
            }

            if (filepath != null) {
                taskUpdateparams.put("filename", filepath);
                db.insertFieldEvent(tripId, tripExpiryDateTime, "imgupload", EventUpdateService.mlocation, taskUpdateparams.toString(), TaskDetailActivity.this);
            }
            if (action_type.equals("doorstep") || action_type.equals("ARD") || (taskType.equals("RPK") && count < totalPhotocount && txt_addmore.getVisibility() != View.VISIBLE)) {
                //&& !QcSuccessOrNot

                if (taskType.equals("RPK") && count < Integer.parseInt(mandatoryPhotocount)) {

                    if (count == 1)
                        imageView2.setVisibility(View.VISIBLE);
                    else if (count == 2)
                        imageView3.setVisibility(View.VISIBLE);
                    else if (count == 3)
                        imageView4.setVisibility(View.VISIBLE);
                    else if (count == 4)
                        imageView5.setVisibility(View.VISIBLE);
                    count++;
                    Toast.makeText(TaskDetailActivity.this, "Please take at least " + mandatoryPhotocount + " image.", Toast.LENGTH_SHORT).show();
                } else
                    txt_addmore.setVisibility(View.VISIBLE);
                /*  if (count == 1 )
                                imageView2.setVisibility(View.VISIBLE);
                            else if(count == 2)
                                imageView3.setVisibility(View.VISIBLE);
                            else if(count == 3)
                                imageView4.setVisibility(View.VISIBLE);
                            else if(count == 4)
                                imageView5.setVisibility(View.VISIBLE);
                           //showActionDialog("image");
                                count++;///Toast.makeText(TaskDetailActivity.this,"Once More",Toast.LENGTH_SHORT).show();
*/
            } else {
                count = 1;
                QcSuccessOrNot = false;
                txt_addmore.setVisibility(View.GONE);
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

            if (count > 3) {
                Dialog = new ProgressDialog(TaskDetailActivity.this);
                Dialog.setMessage("uploading...");
                Dialog.show();
                Dialog.setCanceledOnTouchOutside(false);
                Dialog.setCancelable(false);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                // On complete call either onSignupSuccess or onSignupFailed
                                // depending on success
                                setPic();
                            }
                        }, 7000);
            } else {
                setPic();
            }
            //findViewById(R.id.task_button_failed).setVisibility(View.VISIBLE);
        } else if (requestCode == SIGNATURE_ACTIVITY && resultCode == RESULT_OK) {//abir
            Bundle bundle = data.getExtras();
            String status = bundle.getString("status");
            if (status.equalsIgnoreCase("done")) {
                signclicktextview.setVisibility(View.GONE);
                signclickedimage.setVisibility(View.VISIBLE);
                if (CaptureSignature.mypath != null) {
                    setSignaturePic(CaptureSignature.mypath.getPath());
                    Toast toast = Toast.makeText(this, "Signature capture successful!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        } else {
            filepath = null;
            filepath2 = null;
            filepath3 = null;
            filepath4 = null;
            filepath5 = null;
            CaptureSignature.mypath = null;
            signfilepath = null;
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
            signfilepath = filename;
        } catch (Exception e) {
            Log.e(TAG, "ERROR writing to image file!", e);
        }
    }

    Bitmap bitmap = null;
    Bitmap scaledbitmap = null;


    private void setPic() {
        try {
            // Get the dimensions of the View
            int targetW = imageView.getWidth();
            int targetH = imageView.getHeight();
            int targetW2 = imageView2.getWidth();
            int targetH2 = imageView2.getHeight();
            int targetW3 = imageView3.getWidth();
            int targetH3 = imageView3.getHeight();
            int targetW4 = imageView4.getWidth();
            int targetH4 = imageView4.getHeight();
            int targetW5 = imageView5.getWidth();
            int targetH5 = imageView5.getHeight();
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            BitmapFactory.decodeFile(filepath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor;
            if (count == 2) {
                scaleFactor = Math.min(photoW / targetW2, photoH / targetH2);
            } else if (count == 3) {
                scaleFactor = Math.min(photoW / targetW3, photoH / targetH3);
            } else if (count == 4) {
                scaleFactor = Math.min(photoW / targetW4, photoH / targetH4);
            } else if (count == 5) {
                scaleFactor = Math.min(photoW / targetW5, photoH / targetH5);
            } else {
                scaleFactor = Math.min(photoW / targetW, photoH / targetH);
            }


            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            bitmap = BitmapFactory.decodeFile(filepath, bmOptions);
            scaledbitmap = getResizedBitmap(bitmap, 320);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(filepath);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "ERROR writing to image file!", e);
            }
            BitmapDrawable ob = null;
            try {
                scaledbitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                ob = new BitmapDrawable(getResources(), bitmap);
            } catch (Exception e) {
                Log.e(TAG, "ERROR in compress image file!", e);
            }
            // imageView.setBackgroundDrawable(ob);
            if (count == 2) {
                imageView2.setBackgroundDrawable(ob);
            } else if (count == 3) {
                imageView3.setBackgroundDrawable(ob);
            } else if (count == 4) {
                imageView4.setBackgroundDrawable(ob);
            } else if (count == 5) {
                imageView5.setBackgroundDrawable(ob);
            } else {
                imageView.setBackgroundDrawable(ob);
            }
            if (Dialog != null)
                Dialog.dismiss();

        } catch (Exception e) {
            Log.e(TAG, "ERROR writing to image file!", e);
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private File createImageFile(String imagetype) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName;
        if (imagetype.equals("task")) {
            imageFileName = "TASK_" + timeStamp + "_";
        } else {
            imageFileName = "IDPROOF_" + timeStamp + "_";
        }


        return File.createTempFile(
                imageFileName,
                ".jpg",
                getExternalFilesDir(null)
        );
    }

    //checkinng battery level
    private int batteryLevel(Context context) {
        int level = 0;
        int scale = 0;
        try {
            Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (level * 100) / scale;
    }
//////////////////////////////////////////////////////


    //Async class for getting response data from server on start
    private ProgressDialog Dialog = null;

    private class Get_ResponseFromServer extends AsyncTask<Void, Void, Void> {
        String Result = "";

        protected void nPreExecute() {
            //do nothing
            Dialog = new ProgressDialog(TaskDetailActivity.this);
            Dialog.setMessage("Please wait...");
            Dialog.show();
            Dialog.setCanceledOnTouchOutside(false);
            Dialog.setCancelable(false);
        }

        protected void onPostExecute(Void unused) {
            if (Dialog != null && Dialog.isShowing()) {
                try {
                    Dialog.dismiss();
                    if (ServerInterface.checkserver) {
                        JSONObject mydata = new JSONObject(Result);
                        String res = mydata.getString("Result");
                        Boolean status = mydata.getBoolean("status");
                        if (status && res.equals("OK")) {
                            if (click_taskstatus.equals("ARD")) {
                                db.updateTaskStatus(Constants.task_status_doorstep_code, Long.parseLong(taskId));
                            } else if (click_taskstatus.equals("CAN")) {
                                db.updateTaskStatus(Constants.task_status_pending_code, Long.parseLong(taskId));
                                finish();
                                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                            }
                        } else {
                            if (click_taskstatus.equals("ARD"))
                                saveTaskActionEvent("", "", "", 0, "doorstep", 0, 0, "", "", null);
                            else
                                saveTaskActionEvent("", "", "", 0, "cancel", 0, 0, "", "", null);
                        }

                    } else {
                        if (click_taskstatus.equals("ARD"))
                            saveTaskActionEvent("", "", "", 0, "doorstep", 0, 0, "", "", null);
                        else
                            saveTaskActionEvent("", "", "", 0, "cancel", 0, 0, "", "", null);
                    }
                    //findViewById(R.id.task_button_print).setVisibility(View.VISIBLE);
                    findViewById(R.id.task_button_doorstep).setVisibility(View.GONE);
                    findViewById(R.id.task_button_cancel).setVisibility(View.GONE);
                    findViewById(R.id.task_button_delivered).setVisibility(View.VISIBLE);
                    findViewById(R.id.task_button_faild).setVisibility(View.VISIBLE);
                    showtasktype();
                } catch (Exception e) {
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

                Location location = EventUpdateService.mlocation;
                Double latitude = (double) 0, longitude = (double) 0, altitude = (double) 0;
                Float accuracy = (float) 0, bearing = (float) 0, speed = (float) 0;
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    altitude = location.getAltitude();
                    accuracy = location.getAccuracy();
                    bearing = location.getBearing();
                    speed = location.getSpeed();
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
                allparams.put(answers);
                String loginToken = tb.getString("loginToken");
                jo.put("token", loginToken);
                jo.put("push", "yes");
                jo.put("updates", allparams);
                jo.put("reqId", taskId);

                JSONArray eventsUpdateArray = jo.getJSONArray("updates");
                if (eventsUpdateArray.length() > 0) {
                    String iemino = tb.getString("iemino");
                    Result = ServerInterface.CallServerApi(jo, url, 10, loginToken, iemino);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
