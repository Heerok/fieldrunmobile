package com.humaralabs.fieldrun.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.humaralabs.fieldrun.MainActivity;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.database.DbAdapter;


public class StatsFragment extends Fragment {

    DbAdapter db;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats,container,false);
        db=new DbAdapter(MainActivity.mcontext);
        TextView mytripno=(TextView) v.findViewById(R.id.mytripno);
        TextView activetripno=(TextView) v.findViewById(R.id.activetripno);
        TextView pendingtripno=(TextView) v.findViewById(R.id.pendingtripno);
        TextView completetripno=(TextView) v.findViewById(R.id.completetripno);


        mytripno.setText(String.valueOf(db.GetTodayTripsCountFromDataBase("All")));
        activetripno.setText(String.valueOf(db.GetTodayTripsCountFromDataBase("Active")));
        pendingtripno.setText(String.valueOf(db.GetTodayTripsCountFromDataBase("Pending")));
        completetripno.setText(String.valueOf(db.GetTodayTripsCountFromDataBase("Complete")));
        return v;
    }
}
