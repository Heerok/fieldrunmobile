package com.humaralabs.fieldrun.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.datastructure.Notifications;

import java.util.ArrayList;

public class NotificationAdapter extends BaseAdapter {

    private ArrayList<Notifications> notiList;
    private LayoutInflater inflater;
    private int resource;

    public NotificationAdapter(Context context, ArrayList<Notifications> notications, int resource) {
        this.notiList = notications;
        this.resource = resource;
        this.inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return notiList.size();
    }

    @Override
    public Object getItem(int position) {
        return notiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    private View createViewFromResource(int position, View convertView,ViewGroup parent) {
        Notifications noti = notiList.get(position);
        View v;
        if (convertView == null) {
            v = inflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }
        TextView notidesc = (TextView) v.findViewById(R.id.notidesc);
        TextView notidate = (TextView) v.findViewById(R.id.notidate);

        notidesc.setText(noti.desc);
        notidate.setText(noti.date);

        return v;
    }

}
