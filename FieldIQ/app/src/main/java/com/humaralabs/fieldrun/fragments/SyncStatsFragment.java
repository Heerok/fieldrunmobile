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


public class SyncStatsFragment extends Fragment {

    DbAdapter db;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sync_stats,container,false);
        db=new DbAdapter(MainActivity.mcontext);
        TextView bgcallserverAttempt=(TextView) v.findViewById(R.id.bgcallserverAttempt);
        TextView bgcallserverSuccess=(TextView) v.findViewById(R.id.bgcallserverSuccess);
        TextView bgcallserverFail=(TextView) v.findViewById(R.id.bgcallserverFail);

        TextView rccallserverAttempt=(TextView) v.findViewById(R.id.rccallserverAttempt);
        TextView rccallserverSuccess=(TextView) v.findViewById(R.id.rccallserverSuccess);
        TextView rccallserverFail=(TextView) v.findViewById(R.id.rccallserverFail);

        TextView cccallserverAttempt=(TextView) v.findViewById(R.id.cccallserverAttempt);
        TextView cccallserverSuccess=(TextView) v.findViewById(R.id.cccallserverSuccess);
        TextView cccallserverFail=(TextView) v.findViewById(R.id.cccallserverFail);

        TextView pendingevents=(TextView) v.findViewById(R.id.pendingevents);

        bgcallserverAttempt.setText("Server call attempt count "+String.valueOf(db.getUpdatesCount("bgcallserverAttempt")));
        bgcallserverSuccess.setText("Server call attempt success count "+String.valueOf(db.getUpdatesCount("bgcallserverSuccess")));
        bgcallserverFail.setText("Server call attempt fail count "+String.valueOf(db.getUpdatesCount("bgcallserverFail")));

        rccallserverAttempt.setText("Server call attempt count "+String.valueOf(db.getUpdatesCount("rccallserverAttempt")));
        rccallserverSuccess.setText("Server call attempt success count "+String.valueOf(db.getUpdatesCount("rccallserverSuccess")));
        rccallserverFail.setText("Server call attempt fail count "+String.valueOf(db.getUpdatesCount("rccallserverFail")));

        cccallserverAttempt.setText("Server call attempt count "+String.valueOf(db.getUpdatesCount("cccallserverAttempt")));
        cccallserverSuccess.setText("Server call attempt success count "+String.valueOf(db.getUpdatesCount("cccallserverSuccess")));
        cccallserverFail.setText("Server call attempt fail count "+String.valueOf(db.getUpdatesCount("cccallserverFail")));

        pendingevents.setText("Pending Events Count "+String.valueOf(db.getPendingEventsCount("all")));
        return v;
    }
}
