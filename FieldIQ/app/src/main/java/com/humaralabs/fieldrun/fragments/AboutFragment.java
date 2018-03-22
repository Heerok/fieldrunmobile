package com.humaralabs.fieldrun.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.humaralabs.fieldrun.MainActivity;
import com.humaralabs.fieldrun.R;


public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about_us,container,false);
        TextView versionView = (TextView) v.findViewById(R.id.versionno);
        versionView.setText("Version No - "+getVersion());
        return v;
    }

    //this method checks current version of app
    public String getVersion() {
        try {
            PackageInfo packageInfo = MainActivity.mcontext.getPackageManager().getPackageInfo(MainActivity.mcontext.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "FAILED";
    }
}
