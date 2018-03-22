package com.humaralabs.fieldrun.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.HyperLocalTripTaskListActivity;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.HyperBasket;
import com.humaralabs.fieldrun.datastructure.Task;

import java.util.ArrayList;

public class HyperBasketListAdapterold extends BaseAdapter {

    private static final String TAG = "BasketList";
    private ArrayList<HyperBasket> hyperbasketList;
    private LayoutInflater inflater;
    DbAdapter db;
    TinyDB tinydb;
    Context adapterContext;
    private int resource;
    public int taskStatus=0;
    String tripId;
    String tripOrigin="";
    String tripFacility="";
    String tripExpiryDateTime="";

    public HyperBasketListAdapterold(Context context, ArrayList<HyperBasket> hyperbaskets, int resource, String tOrigin, String tFacility, String tDateTime) {
        if(db==null)
            db = new DbAdapter(context);
        if(tinydb==null)
            tinydb=new TinyDB(context);
        adapterContext=context;
        tripOrigin=tOrigin;
        tripFacility=tFacility;
        tripExpiryDateTime=tDateTime;
        this.hyperbasketList = hyperbaskets;
        this.resource = resource;
        this.inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return hyperbasketList.size();
    }

    @Override
    public Object getItem(int position) {
        return hyperbasketList.get(position);
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
        final HyperBasket hyperbasket = hyperbasketList.get(position);
        View v;
        if (convertView == null) {
            v = inflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }

        LinearLayout linearLayout=(LinearLayout) v.findViewById(R.id.mainly);
        TextView numtaskTextView = (TextView) v.findViewById(R.id.numtask);
        TextView taskrefTextView = (TextView) v.findViewById(R.id.ref);
        Button buttonView = (Button) v.findViewById(R.id.viewdetails);

         //String tag = "Pick";

        //setting number of task


        numtaskTextView.setVisibility(View.VISIBLE);
        final String basketref=String.valueOf(hyperbasket.hyper_basket_ref_no);
        numtaskTextView.setText(db.GetTaskForParticulatTripsCount(Constants.task_status_pending_code, basketref, "HyperBasketPick")
                +" Pick \n"+db.GetTaskForParticulatTripsCount(Constants.task_status_pending_code, basketref, "HyperBasketDel")+" Del");

        //setting basket ref
        if(basketref==null || basketref.equals("") || basketref.equals("null") || basketref.equals("NA")) {
            taskrefTextView.setVisibility(View.GONE);
        }
        else {
            taskrefTextView.setVisibility(View.VISIBLE);
            taskrefTextView.setText("Order  "+basketref);
        }

        int pendingTaskCount = db.GetTaskForParticulatTripsCount(Constants.task_status_pending_code, hyperbasket.hyper_basket_ref_no,"HyperBasket");
        if (pendingTaskCount == 0)
            linearLayout.setVisibility(View.GONE);
        else
            linearLayout.setVisibility(View.VISIBLE);


        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showdialog(hyperbasket);
            }
        });

        return v;
    }

    TextView paddress;
    TextView daddress;

    Button proceed,cancle;
    private void showdialog(final HyperBasket hyperBasket) {
        final Dialog dialog = new Dialog(adapterContext);
        //hiding default title bar of dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.display_pickup_del_popup);
        dialog.getWindow().getAttributes().width= WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        paddress=(TextView)dialog.getWindow().findViewById(R.id.paddress);
        daddress =(TextView)dialog.getWindow().findViewById(R.id.daddress);
        cancle=(Button)dialog.getWindow().findViewById(R.id.task_button_can);
        proceed=(Button)dialog.getWindow().findViewById(R.id.task_button_proceed);
        dialog.setCanceledOnTouchOutside(false);

        String pickupAddressText="";
        String delAddressText="";
        ArrayList<Task> taskData=db.GetAllHyperTaskForParticulatTrips(hyperBasket.hyper_basket_ref_no);
        for (Task taskobject: taskData) {
            if(taskobject.taskType.equals("HYP-PICKUP")){
                pickupAddressText +=taskobject.address+" \n\n";
            }
            else if(taskobject.taskType.equals("HYP-DELIVERY")){
                delAddressText +=taskobject.address+" \n\n";
            }
        }

        paddress.setText(pickupAddressText);
        daddress.setText(delAddressText);

        dialog.show(); //to show dialog box
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                int tripstatus = db.getTripStatus(hyperBasket.hyper_basket_trip_id);
                if (tripstatus == Constants.trip_status_active_code) {
                    Intent detailIntent = new Intent(adapterContext, HyperLocalTripTaskListActivity.class);
                    detailIntent.putExtra("hyper_basket_ref_no", hyperBasket.hyper_basket_ref_no);
                    detailIntent.putExtra("trip_id", hyperBasket.hyper_basket_trip_id);
                    detailIntent.putExtra("origin", tripOrigin);
                    detailIntent.putExtra("facility", tripFacility);
                    detailIntent.putExtra("tripExpiryDateTime", tripExpiryDateTime);
                    adapterContext.startActivity(detailIntent);
                    //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                } else if (tripstatus == Constants.basket_status_pending_code) {
                    Toast.makeText(adapterContext, "Trip not started yet.", Toast.LENGTH_LONG).show();
                } else if (tripstatus == Constants.basket_status_complete_code) {
                    Toast.makeText(adapterContext, "Trip already completed.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
