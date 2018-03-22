package com.humaralabs.fieldrun;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.humaralabs.fieldrun.adapter.QuesListAdapter;
import com.humaralabs.fieldrun.adapter.ReviewListAdapter;
import com.humaralabs.fieldrun.datastructure.QuestionSet;

import java.util.ArrayList;

import static com.humaralabs.fieldrun.TaskDetailActivity.doneReview;


public class ReviewPageActivity extends AppCompatActivity {
    ListView r_list;
    ArrayList<QuestionSet> ReviewQuestionList;
    ArrayList<String> ReviewAnswerList;
    Button btn_review;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_review_page);
        r_list = (ListView)findViewById(R.id.review_list);
        btn_review = (Button)findViewById(R.id.button_review);
        //ReviewQuestionList = new ArrayList<String>();
        //ReviewQuestionList = (ArrayList<QuestionSet>)getIntent().getSerializableExtra("QuestionList");
      //  ReviewAnswerList = (ArrayList<String>)getIntent().getSerializableExtra("AnswerList");
        //Toast.makeText(ReviewPageActivity.this,"success",Toast.LENGTH_SHORT).show();
        ReviewListAdapter quesListAdapter = new ReviewListAdapter(this,TaskDetailActivity.QuestionList, R.layout.review_item);
        r_list.setAdapter(quesListAdapter);

        btn_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneReview = true;
                ReviewPageActivity.this.finish();
            }
        });
    }

}
