package com.humaralabs.fieldrun.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.humaralabs.fieldrun.R;

import com.humaralabs.fieldrun.TaskDetailActivity;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.datastructure.QuestionSet;

import java.util.ArrayList;



public class QuesListAdapter extends BaseAdapter {
    private static final String TAG = "QuestionList";
    private ArrayList<QuestionSet> quesList;
    private LayoutInflater inflater;
    DbAdapter db;
    TinyDB tinydb;
    Context adapterContext;
    private int resource;
    int pos = 0;

    public QuesListAdapter(Context context, ArrayList<QuestionSet> ques, int resource) {
        if(db==null)
            db = new DbAdapter(context);
        if(tinydb==null)
            tinydb=new TinyDB(context);
        adapterContext=context;
        this.quesList = ques;
        this.resource = resource;
        this.inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return 1;
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

    private View createViewFromResource(final int position, View convertView,ViewGroup parent) {
        QuestionSet ques = quesList.get(position);
        pos=position;
        final View v;
        if (convertView == null) {
            v = inflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }

        Switch simpleSwitch = (Switch) v.findViewById(R.id.simpleSwitch);
          simpleSwitch.setTag(position);
        // check current state of a Switch (true or false).
        Boolean switchState = simpleSwitch.isChecked();
        final TextView count = (TextView)v.findViewById(R.id.text_count);
        final TextView txtquestion = (TextView)v.findViewById(R.id.text_question);

        Button next = (Button) v.findViewById(R.id.task_next);
        Button prev = (Button) v.findViewById(R.id.task_pre);

        String questionID=String.valueOf(ques.questionId);
        final String question=String.valueOf(ques.question);
        if(question!=null || !question.equals("") || !question.equals("null") || !question.equals("NA")) {
            txtquestion.setText(question);
            count.
            setText(String.valueOf(position+1)+" - ");
        }
        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                QuestionSet questionSet = quesList.get((Integer) buttonView.getTag());
                questionSet.answerType=""+isChecked;
               // Toast.makeText(adapterContext,""+quesList.get((Integer) buttonView.getTag()).question+":"+quesList.get((Integer) buttonView.getTag()).answerType,Toast.LENGTH_SHORT).show();
            }
        });
            //Toast.makeText(adapterContext,switchState.toString(),Toast.LENGTH_SHORT).show();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pos < quesList.size() - 1){
                    if(question!=null || !question.equals("") || !question.equals("null") || !question.equals("NA")) {
                        txtquestion.setText(question);
                        count.setText(String.valueOf(position + 1) + " - ");
                    }
                    Toast.makeText(adapterContext,quesList.get(pos).question,Toast.LENGTH_SHORT).show();
                    pos++;

                }
                else
                    Toast.makeText(adapterContext,"Last record",Toast.LENGTH_SHORT).show();
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pos > 0){
                    if(question!=null || !question.equals("") || !question.equals("null") || !question.equals("NA")) {
                        txtquestion.setText(question);
                        count.setText(String.valueOf(position+1)+" - ");}
                        QuesListAdapter quesListAdapter = new QuesListAdapter(adapterContext, quesList, R.layout.item_quest);
                        TaskDetailActivity.ques_list.setAdapter(quesListAdapter);

                    Toast.makeText(adapterContext,quesList.get(pos).question,Toast.LENGTH_SHORT).show();
                    pos--;
                }
                else
                    Toast.makeText(adapterContext,"First record",Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }


}
