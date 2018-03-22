package com.humaralabs.fieldrun;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.humaralabs.fieldrun.adapter.NotificationAdapter;
import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.datastructure.Notifications;

import java.util.ArrayList;


public class NotificationActivity extends AppCompatActivity {

    private Toolbar toolbar;
    DbAdapter db=new DbAdapter(MainActivity.mcontext);

    ArrayList<Notifications> NotificationList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        if (toolbar != null) {
            toolbar.setTitle("Notification");
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        TextView messaage=(TextView) findViewById(R.id.messageText);
        ListView lv_noti=(ListView) findViewById(R.id.notilistview);
        NotificationList=db.GetNotificationsFromDatabase();

        if(NotificationList.size()>0) {
            messaage.setVisibility(View.GONE);
            NotificationAdapter notificationListAdapter = new NotificationAdapter(NotificationActivity.this, NotificationList, R.layout.activity_noti_list_item);
            lv_noti.setAdapter(notificationListAdapter);
            messaage.setVisibility(View.GONE);
        }
        else{
            messaage.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);;
        }
        return super.onOptionsItemSelected(item);
    }
}
