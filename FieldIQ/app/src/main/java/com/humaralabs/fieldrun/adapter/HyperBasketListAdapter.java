package com.humaralabs.fieldrun.adapter;

/**
 * Created by pc1 on 08-09-2016.
 */
import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
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
import com.humaralabs.fieldrun.datastructure.HyperBasketChild;

public class HyperBasketListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<HyperBasket> hyperbaskets;

    DbAdapter db;
    TinyDB tinydb;

    String tripOrigin="";
    String tripFacility="";
    String tripExpiryDateTime="";

    public HyperBasketListAdapter(Context context, ArrayList<HyperBasket> hyperbaskets,String tOrigin,String tFacility,String tDateTime) {
        this.context = context;
        this.hyperbaskets = hyperbaskets;
        tripOrigin=tOrigin;
        tripFacility=tFacility;
        tripExpiryDateTime=tDateTime;
        if(db==null)
            db = new DbAdapter(context);
        if(tinydb==null)
            tinydb=new TinyDB(context);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return hyperbaskets.get(groupPosition).hychild;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        ArrayList<HyperBasketChild> child = hyperbaskets.get(groupPosition).hychild;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.child_item, null);
        }
        TextView tvp = (TextView) convertView.findViewById(R.id.pdetails);
        TextView tvd = (TextView) convertView.findViewById(R.id.ddetails);
        Button btnProceed=(Button) convertView.findViewById(R.id.task_button_proceed);
        //ImageView iv = (ImageView) convertView.findViewById(R.id.flag);

        tvp.setText(child.get(childPosition).pDetails);
        tvd.setText(child.get(childPosition).dDetails);
        //iv.setImageResource(child.getImage());
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                int tripstatus = db.getTripStatus(hyperbaskets.get(groupPosition).hyper_basket_trip_id);
                if (tripstatus == Constants.trip_status_active_code) {
                    Intent detailIntent = new Intent(context, HyperLocalTripTaskListActivity.class);
                    detailIntent.putExtra("hyper_basket_ref_no", hyperbaskets.get(groupPosition).hyper_basket_ref_no);
                    detailIntent.putExtra("trip_id", hyperbaskets.get(groupPosition).hyper_basket_trip_id);
                    detailIntent.putExtra("origin", tripOrigin);
                    detailIntent.putExtra("facility", tripFacility);
                    detailIntent.putExtra("tripExpiryDateTime", tripExpiryDateTime);
                    context.startActivity(detailIntent);
                    //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                } else if (tripstatus == Constants.basket_status_pending_code) {
                    Toast.makeText(context, "Trip not started yet.", Toast.LENGTH_LONG).show();
                } else if (tripstatus == Constants.basket_status_complete_code) {
                    Toast.makeText(context, "Trip already completed.", Toast.LENGTH_LONG).show();
                }
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<HyperBasketChild> chList = hyperbaskets.get(groupPosition).hychild;
        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return hyperbaskets.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return hyperbaskets.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        final HyperBasket hyperbasket = hyperbaskets.get(groupPosition);
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) context
                    .getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inf.inflate(R.layout.activity_hyper_basket_list_item, null);
        }
        LinearLayout linearLayout=(LinearLayout) convertView.findViewById(R.id.mainly);
        TextView numtaskTextView = (TextView) convertView.findViewById(R.id.numtask);
        TextView taskrefTextView = (TextView) convertView.findViewById(R.id.ref);
        Button buttonView = (Button) convertView.findViewById(R.id.viewdetails);

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

        /*int pendingTaskCount = db.GetTaskForParticulatTripsCount(Constants.task_status_pending_code, hyperbasket.hyper_basket_ref_no,"HyperBasket");
        if (pendingTaskCount == 0)
            linearLayout.setVisibility(View.GONE);
        else
            linearLayout.setVisibility(View.VISIBLE);
        */

        buttonView.setVisibility(View.GONE);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
