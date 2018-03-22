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
import com.humaralabs.fieldrun.datastructure.Basket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class BasketListAdapter extends BaseAdapter {

    private static final String TAG = "BasketList";
    private ArrayList<Basket> basketList;
    private LayoutInflater inflater;
    DbAdapter db;
    TinyDB tinydb;;
    Context adapterContext;
    private int resource;
    public int taskStatus=0;
    String taskId;
    String tripId;
    SimpleDateFormat foramtter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat foramtterTime = new SimpleDateFormat("HH:mm a");
    public BasketListAdapter(Context context, ArrayList<Basket> baskets, int resource, int status) {
        if(db==null)
            db = new DbAdapter(context);
        if(tinydb==null)
            tinydb=new TinyDB(context);
        adapterContext=context;
        taskStatus=status;
        this.basketList = baskets;
        this.resource = resource;
        this.inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return basketList.size();
    }

    @Override
    public Object getItem(int position) {
        return basketList.get(position);
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
        Basket basket = basketList.get(position);
        View v;
        if (convertView == null) {
            v = inflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }

        TextView numtaskTextView = (TextView) v.findViewById(R.id.numtask);
        TextView taskrefTextView = (TextView) v.findViewById(R.id.ref);
        TextView nameTextView = (TextView) v.findViewById(R.id.name);
        TextView zipcodeTextView = (TextView) v.findViewById(R.id.zipcode);
        TextView addressTextView = (TextView) v.findViewById(R.id.address);


        String tag = "Pick";

        //setting number of task

        String numTasks=String.valueOf(basket.basket_eqty);
        if(numTasks==null || numTasks.equals("") || numTasks.equals("null") || numTasks.equals("NA")) {
            numtaskTextView.setVisibility(View.GONE);
        }
        else {
            numtaskTextView.setVisibility(View.VISIBLE);
            numtaskTextView.setText(numTasks+" "+tag);
        }

        //setting basket ref
        String basketref=String.valueOf(basket.basket_server_id);
        if(basketref==null || basketref.equals("") || basketref.equals("null") || basketref.equals("NA")) {
            taskrefTextView.setVisibility(View.GONE);
        }
        else {
            taskrefTextView.setVisibility(View.VISIBLE);
            taskrefTextView.setText("Basket "+basketref);
        }

        //setting name
        String name=String.valueOf(basket.basket_seller_name);
        if(name==null || name.equals("") || name.equals("null") || name.equals("NA")) {
            nameTextView.setVisibility(View.GONE);
        }
        else {
            nameTextView.setVisibility(View.VISIBLE);
            nameTextView.setText(name);
        }


        //setting zipcode
        String zipcode=String.valueOf(basket.basket_seller_zipcode);
        if(zipcode==null || zipcode.equals("") || zipcode.equals("null") || zipcode.equals("NA")) {
            zipcodeTextView.setVisibility(View.GONE);
        }
        else {
            zipcodeTextView.setVisibility(View.VISIBLE);
            zipcodeTextView.setText(zipcode);
        }

        //setting address
        String address=String.valueOf(basket.basket_seller_address);
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
