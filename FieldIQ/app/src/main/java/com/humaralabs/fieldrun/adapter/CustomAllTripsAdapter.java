package com.humaralabs.fieldrun.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.MainActivity;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.datastructure.Trip;

import java.util.ArrayList;


public class CustomAllTripsAdapter extends BaseAdapter {

	private final Context context;
	ArrayList<Trip> MTripList;
	DbAdapter db;
	public CustomAllTripsAdapter(Context context, ArrayList<Trip> TripList) {
		this.context=context;
		MTripList=TripList;
	}
	
	@Override
	public View getView(final int position,View view,ViewGroup parent) {
		LayoutInflater inflater=((Activity) context).getLayoutInflater();
		View rowView=inflater.inflate(R.layout.all_trips_listviiew_item, null, true);
		db=new DbAdapter(MainActivity.mcontext);
		TextView numtaskTextView=(TextView)  rowView.findViewById(R.id.numtask);
		TextView tripidTextView=(TextView)  rowView.findViewById(R.id.tripid);
		TextView originTextView=(TextView)  rowView.findViewById(R.id.origin);
		TextView zipcodeTextView=(TextView)  rowView.findViewById(R.id.zipcode);
		TextView tripdateTextView=(TextView)  rowView.findViewById(R.id.tripdate);
		TextView tripstatusTextView=(TextView)  rowView.findViewById(R.id.tripstatus);

		//setting number of task
		String tag="Task";
		//if(MTripList.get(position).trip_type.contains("OPK"))
			//tag="Basket";


		String numTasks=String.valueOf(MTripList.get(position).numTasks);
		if(numTasks==null || numTasks.equals("") || numTasks.equals("null") || numTasks.equals("NA")) {
			numtaskTextView.setVisibility(View.GONE);
		}
		else {
			numtaskTextView.setVisibility(View.VISIBLE);
			//if(MTripList.get(position).trip_type.contains("OPK"))
				//numtaskTextView.setText(db.GetBasketForParticulatTripsCount(0,MTripList.get(position).tripId,"All")+" "+tag);
			//else
				numtaskTextView.setText(numTasks+" "+tag);
		}

		//setting number of task
		String tripId=String.valueOf(MTripList.get(position).tripId.toString());
		if(tripId==null || tripId.equals("") || tripId.equals("null") || tripId.equals("NA")) {
			tripidTextView.setVisibility(View.GONE);
		}
		else {
			tripidTextView.setVisibility(View.VISIBLE);
			tripidTextView.setText("Trip "+tripId);
		}

		//setting origin
		String originToDsiplay=MTripList.get(position).origin;
		if(originToDsiplay==null || originToDsiplay.equals("") || originToDsiplay.equals("null") || originToDsiplay.equals("NA")) {
			originTextView.setVisibility(View.GONE);
		}
		else {
			originTextView.setVisibility(View.VISIBLE);
			originTextView.setText(originToDsiplay);
		}

		//setting zipcode
		String ZipCodeToDsiplay=MTripList.get(position).zipCode;
		if(ZipCodeToDsiplay==null || ZipCodeToDsiplay.equals("") || ZipCodeToDsiplay.equals("null") || ZipCodeToDsiplay.equals("NA")) {
			zipcodeTextView.setVisibility(View.GONE);
		}
		else {
			zipcodeTextView.setVisibility(View.VISIBLE);
			zipcodeTextView.setText(ZipCodeToDsiplay);
		}

		//setting tripdate
		String TripDateToDsiplay=MTripList.get(position).tripDate;
		if(TripDateToDsiplay==null || TripDateToDsiplay.equals("") || TripDateToDsiplay.equals("null") || TripDateToDsiplay.equals("NA")) {
			tripdateTextView.setVisibility(View.GONE);
		}
		else {
			tripdateTextView.setVisibility(View.VISIBLE);
			tripdateTextView.setText(TripDateToDsiplay);
		}

		//setting tripstatus
		if(MTripList.get(position).status==Constants.trip_status_pending_code){
			tripstatusTextView.setText("Pending");
		}else if(MTripList.get(position).status==Constants.trip_status_active_code){
			tripstatusTextView.setText("Active");
		}
		else{
			tripstatusTextView.setText("Completed");
		}
        return rowView;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return MTripList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return MTripList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	};
}
