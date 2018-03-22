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
import com.humaralabs.fieldrun.datastructure.QuestionSet;

import java.util.ArrayList;

public class ReviewListAdapter extends BaseAdapter {
    private static final String TAG = "QuestionList";
    private ArrayList<QuestionSet> quesList;
  //  ArrayList<String> anslist;
    private LayoutInflater inflater;
    DbAdapter db;
    TinyDB tinydb;
    Context adapterContext;
    private int resource;

        public ReviewListAdapter(Context context, ArrayList<QuestionSet> ques, int resource) {
            if (db == null)
                db = new DbAdapter(context);
            if (tinydb == null)
                tinydb = new TinyDB(context);
            adapterContext = context;
            this.quesList = ques;
            //this.anslist = ans;
            this.resource = resource;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return quesList.size();
        }

        @Override
        public Object getItem(int position) {
            return quesList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createViewFromResource(position, convertView, parent);
        }

        private View createViewFromResource(final int position, View convertView, ViewGroup parent) {

            View v;
            if (convertView == null) {
                v = inflater.inflate(resource, parent, false);
            } else {
                v = convertView;
            }
            TextView count = (TextView)v.findViewById(R.id.review_count);
            TextView txtquestion = (TextView)v.findViewById(R.id.review_question);
            TextView txtanswer = (TextView)v.findViewById(R.id.review_ans);

            count.setText(String.valueOf(position+1)+"");
            txtquestion.setText(quesList.get(position).question);
            txtanswer.setText(quesList.get(position).answerType.equals("true")?"YES":"NO");
            return v;
        }
    }
