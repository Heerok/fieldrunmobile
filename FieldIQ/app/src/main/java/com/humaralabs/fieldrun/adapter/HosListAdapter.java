package com.humaralabs.fieldrun.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class HosListAdapter extends BaseAdapter {
    private static final String TAG = "TaskList";
    private ArrayList<Task> hosList;
    private LayoutInflater inflater;
    DbAdapter db;
    TinyDB tinydb;
    Context adapterContext;
    private int resource;
    public int taskStatus=0;
    String taskId;
    String tripId;
    SimpleDateFormat foramtter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat foramtterTime = new SimpleDateFormat("HH:mm a");
    public HosListAdapter(Context context, ArrayList<Task> hos, int resource,int status) {
        if(db==null)
            db = new DbAdapter(context);
        if(tinydb==null)
            tinydb=new TinyDB(context);
        adapterContext=context;
        taskStatus=status;
        this.hosList = hos;
        this.resource = resource;
        this.inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return hosList.size();
    }

    @Override
    public Object getItem(int position) {
        return hosList.get(position);
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
        Task task = hosList.get(position);
        View v;
        if (convertView == null) {
            v = inflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }

        TextView numtaskTextView = (TextView) v.findViewById(R.id.numtask);
        TextView taskrefTextView = (TextView) v.findViewById(R.id.ref);
        TextView nameTextView = (TextView) v.findViewById(R.id.name);
        TextView dateTextView = (TextView) v.findViewById(R.id.date);
        TextView zipcodeTextView = (TextView) v.findViewById(R.id.zipcode);
        TextView addressTextView = (TextView) v.findViewById(R.id.address);
        TextView tripstatusTextView = (TextView) v.findViewById(R.id.tripstatus);


        String type = task.taskType;
        String tag = "Pick";
        switch (type) {
            case "FPK":
            case "OPK":
            case "RPK":
                tag = "Pick";
                break;
            case "RED":
            case "FWD":
                tag = "Del";
                break;
        }

        //setting number of task
        String numTasks=String.valueOf(task.pickups);
        if(numTasks==null || numTasks.equals("") || numTasks.equals("null") || numTasks.equals("NA")) {
            numtaskTextView.setVisibility(View.GONE);
        }
        else {
            numtaskTextView.setVisibility(View.VISIBLE);
            numtaskTextView.setText(numTasks+" "+tag);
        }

        //setting task ref
        String taskref=String.valueOf(task.ref);
        if(taskref==null || taskref.equals("") || taskref.equals("null") || taskref.equals("NA")) {
            taskrefTextView.setVisibility(View.GONE);
        }
        else {
            taskrefTextView.setVisibility(View.VISIBLE);
            taskrefTextView.setText("Hos - "+taskref);
        }

        //setting name
        String name=String.valueOf(task.name);
        if(name==null || name.equals("") || name.equals("null") || name.equals("NA")) {
            nameTextView.setVisibility(View.GONE);
        }
        else {
            nameTextView.setVisibility(View.VISIBLE);
            nameTextView.setText(name);
        }

        //setting date
        String delieveryDateTime=String.valueOf(task.delieveryDateTime);
        if(delieveryDateTime==null || delieveryDateTime.equals("") || delieveryDateTime.equals("null") || delieveryDateTime.equals("NA")) {
            dateTextView.setVisibility(View.GONE);
        }
        else {
            dateTextView.setVisibility(View.VISIBLE);
            String reformattedStr = "";
            try {
                reformattedStr = foramtterTime.format(foramtter.parse(delieveryDateTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
            dateTextView.setText(reformattedStr);
        }

        //setting zipcode
        String zipcode=String.valueOf(task.zipCode);
        if(zipcode==null || zipcode.equals("") || zipcode.equals("null") || zipcode.equals("NA")) {
            zipcodeTextView.setVisibility(View.GONE);
        }
        else {
            zipcodeTextView.setVisibility(View.VISIBLE);
            zipcodeTextView.setText(zipcode);
        }

        //setting address
        String address=String.valueOf(task.address);
        if(address==null || address.equals("") || address.equals("null") || address.equals("NA")) {
            addressTextView.setVisibility(View.GONE);
        }
        else {
            addressTextView.setVisibility(View.VISIBLE);
            addressTextView.setText(address);
        }
        return v;
    }


}
