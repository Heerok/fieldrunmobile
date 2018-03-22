package com.humaralabs.fieldrun;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.humaralabs.fieldrun.database.TinyDB;
public class SelectOrgActivity extends AppCompatActivity {



    TinyDB tinydb;
    EditText texta;
    Button goBtn;
    String Orgurl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_select_org);

        tinydb=new TinyDB(this);
        Orgurl=tinydb.getString("url");
        if(Orgurl.equals(Constants.merataskurl)){
            //tinydb.putString("url",Constants.ServerUrl);
            Constants.ServerUrl=Constants.merataskurl.trim();
            Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
            Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
            startActivity(i);
            finish();
        }
        else if( Orgurl.equals(Constants.streetwiseurl))
        {
            Constants.ServerUrl=Constants.streetwiseurl.trim();
            Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
            Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
            startActivity(i);
            finish();
        }
        else  if(Orgurl.equals(Constants.bulkvanurl))
        {
            Constants.ServerUrl=Constants.bulkvanurl.trim();
            Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
            Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
            startActivity(i);
            finish();
        }
        else  if(Orgurl.equals(Constants.daakuaturl))
        {
            Constants.ServerUrl=Constants.daakuaturl.trim();
            Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
            Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
            startActivity(i);
            finish();
        }
        else  if(Orgurl.equals(Constants.deliverysolutionsurl))
        {
            Constants.ServerUrl=Constants.deliverysolutionsurl.trim();
            Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
            Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
            startActivity(i);
            finish();
        }
        else  if(Orgurl.equals(Constants.claexpressurl))
        {
            Constants.ServerUrl=Constants.claexpressurl.trim();
            Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
            Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
            startActivity(i);
            finish();
        }
        else  if(Orgurl.equals(Constants.bvclogistic))
        {
            Constants.ServerUrl=Constants.bvclogistic.trim();
            Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
            Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
            startActivity(i);
            finish();
        }
        else  if(Orgurl.equals(Constants.taskmasterurl))
        {
            Constants.ServerUrl=Constants.taskmasterurl.trim();
            Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
            Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
            startActivity(i);
            finish();
        }
        texta=(EditText) findViewById(R.id.texta);

        //texta.setText(tinydb.getString("url"));
        goBtn=(Button) findViewById(R.id.gobtn);
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String TextValue=texta.getText().toString().trim();
                if (TextValue.equalsIgnoreCase("meratask"))
                {   tinydb.putString("url",Constants.merataskurl);
                    Constants.ServerUrl=Constants.merataskurl.trim();
                    Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
                    Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
                    startActivity(i);
                    finish();}
                else if(TextValue.equalsIgnoreCase("streetwise"))
                {   tinydb.putString("url",Constants.streetwiseurl);
                    Constants.ServerUrl=Constants.streetwiseurl.trim();
                    Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
                    Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
                    startActivity(i);
                    finish();}
                else if(TextValue.equalsIgnoreCase("bulkvan"))
                {   tinydb.putString("url",Constants.bulkvanurl);
                    Constants.ServerUrl=Constants.bulkvanurl.trim();
                    Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
                    Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
                    startActivity(i);
                    finish();}
                else if(TextValue.equalsIgnoreCase("bvc"))
                {   tinydb.putString("url",Constants.bvclogistic);
                    Constants.ServerUrl=Constants.bvclogistic.trim();
                    Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
                    Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
                    startActivity(i);
                    finish();}
                else if(TextValue.equalsIgnoreCase("del"))
                {   tinydb.putString("url",Constants.deliverysolutionsurl);
                    Constants.ServerUrl=Constants.deliverysolutionsurl.trim();
                    Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
                    Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
                    startActivity(i);
                    finish();}
                else if(TextValue.equalsIgnoreCase("clr"))
                {   tinydb.putString("url",Constants.claexpressurl);
                    Constants.ServerUrl=Constants.claexpressurl.trim();
                    Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
                    Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
                    startActivity(i);
                    finish();}
                else if(TextValue.equalsIgnoreCase("daak"))
                {   tinydb.putString("url",Constants.daakuaturl);
                    Constants.ServerUrl=Constants.daakuaturl.trim();
                    Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
                    Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
                    startActivity(i);
                    finish();}
                else if(TextValue.equalsIgnoreCase("taskmaster"))
                {   tinydb.putString("url",Constants.taskmasterurl);
                    Constants.ServerUrl=Constants.taskmasterurl.trim();
                    Constants.ServerApiUrl = Constants.ServerUrl + "/api/";
                    Intent i=new Intent(SelectOrgActivity.this,SplashActivity.class);
                    startActivity(i);
                    finish();}
                else
                {
                    Toast.makeText(SelectOrgActivity.this,"Enter correct organization name",Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
