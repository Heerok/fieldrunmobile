package com.humaralabs.fieldrun.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.HyperLocalBaksetListActivity;
import com.humaralabs.fieldrun.MainActivity;
import com.humaralabs.fieldrun.OneshipBaksetListActivity;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.TripTaskListActivity;
import com.humaralabs.fieldrun.adapter.CustomAllTripsAdapter;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.Trip;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

/**
 * Created by Admin on 04-06-2015.
 */
public class ActiveTripsFragment extends Fragment {

    private static final String TAG ="ActiveTrips";
    TinyDB tinydb;
    DbAdapter db;
    ArrayList<Trip> ActiveTripList;
    ListView lview;
    TextView notrips;
    ImageView im;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_active_trips,container,false);
        ActiveTripList=new ArrayList<Trip>();
        im=(ImageView) v.findViewById(R.id.appbanneractive);
        lview=(ListView) v.findViewById(R.id.listView1);
        notrips=(TextView) v.findViewById(R.id.notrips);
        tinydb=new TinyDB(MainActivity.mcontext);
        db=new DbAdapter(MainActivity.mcontext);

        if(!tinydb.getString("appbanner").equals("")) {
            Picasso.with(MainActivity.mcontext).load(Constants.ServerUrl + "/" + tinydb.getString("appbanner")).into(target);
        }


        ArrayList<Trip> triplist = db.GetTripsFromDatabase("Active");
        SetTripDataInList(triplist);
        return v;
    }


    //this method is used to set data in a listview
    private void SetTripDataInList(final ArrayList<Trip> triplist){
        if(triplist.size()>0) {
            notrips.setVisibility(View.GONE);
            CustomAllTripsAdapter adapter = new CustomAllTripsAdapter(getActivity(), triplist);
            lview.setAdapter(adapter);
            lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(triplist.get(position).numTasks!=0) {
                        OpenTaskListActivity(triplist,position);
                    }
                    else{
                        //show toast
                        Toast.makeText(MainActivity.mcontext, "No task Assigned For " + triplist.get(position).tripId + " Trip", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else{
            notrips.setVisibility(View.VISIBLE);
        }
    }

    private CompanyLogoTarget target = new CompanyLogoTarget();
    private class CompanyLogoTarget implements Target {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            //imageLoadered = true;
            int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                im.setBackgroundDrawable(new BitmapDrawable(bitmap));
            } else {
                im.setBackground(new BitmapDrawable(getResources(), bitmap));
            }
            //im.setImageBitmap(bitmap);
            //bitmapOfImage = bitmap;
            Log.e("App","Success to load company logo in onBitmapLoaded method");
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            //imageLoadered = false;
            //imgLogo.setBackgroundResource(R.drawable.black_border);
            //imgLogo.setImageResource(R.drawable.building);
            Log.e("App","Failed to load company logo in onBitmapFailed method");
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            //imgLogo.setImageResource(R.drawable.loading);
            Log.e("App","Prepare to load company logo in onPrepareLoad method");
        }

    }

    //open task activity for selected trip
    private void OpenTaskListActivity(ArrayList<Trip> triplist,int position){
        Intent i;
        if(triplist.get(position).trip_type.contains("OPK"))
            i = new Intent(MainActivity.mcontext, OneshipBaksetListActivity.class);
        else if(triplist.get(position).trip_type.contains("HYP"))
            i = new Intent(MainActivity.mcontext, HyperLocalBaksetListActivity.class);
        else
            i = new Intent(MainActivity.mcontext, TripTaskListActivity.class);
        //Intent i = new Intent(MainActivity.mcontext, TripTaskListActivity.class);
        i.putExtra("trip_id", triplist.get(position).tripId);
        i.putExtra("origin", triplist.get(position).origin);
        i.putExtra("facility", triplist.get(position).trip_facility);
        i.putExtra("tripExpiryDateTime", triplist.get(position).tripExpiryDateTime);
        startActivity(i);
        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }
}


