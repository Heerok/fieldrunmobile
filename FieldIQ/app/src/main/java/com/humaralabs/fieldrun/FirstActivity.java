package com.humaralabs.fieldrun;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.humaralabs.fieldrun.database.TinyDB;


public class FirstActivity extends AppCompatActivity {



    TinyDB tinydb;
    EditText texta;
    Button goBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstscreen);

        tinydb=new TinyDB(this);
        if(tinydb.getString("url").equals("")){
            tinydb.putString("url",Constants.ServerUrl);
        }
        texta=(EditText) findViewById(R.id.texta);

        texta.setText(tinydb.getString("url"));
        goBtn=(Button) findViewById(R.id.gobtn);
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String TextValue=texta.getText().toString();
                tinydb.putString("url",TextValue.trim());
                Constants.ServerUrl=TextValue.trim();
                Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
                Intent i=new Intent(FirstActivity.this,SplashActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}
