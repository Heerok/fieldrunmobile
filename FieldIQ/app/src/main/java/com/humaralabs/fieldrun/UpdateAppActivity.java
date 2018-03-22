package com.humaralabs.fieldrun;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.humaralabs.fieldrun.database.TinyDB;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class UpdateAppActivity extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     */
    private ProgressDialog prgDialog;
    // Progress Dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;
    TinyDB tiny;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_app);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        //installAPkfile();
        setupButton();
    }



    private void setupButton() {
        Button button = (Button) findViewById(R.id.updateapp);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                createNotification();
            }
        });
    }

    private void createNotification() {
        if(tiny==null)
            tiny=new TinyDB(UpdateAppActivity.this);
        String file_url = Constants.ServerApiUrl+"mobile/download/latestApk?appname=fieldiq&versioncode="+ BuildConfig.VERSION_CODE+"&token="+tiny.getString("loginToken");
        new DownloadMusicfromInternet().execute(file_url);
       /* File file = new File(Environment.getExternalStorageDirectory().getPath()+"/big_buck_bunny_720p_1mb.mp4");
        // Check if the Music file already exists
        if (file.exists()) {
            Toast.makeText(getApplicationContext(), "File already exist under SD card, playing Music", Toast.LENGTH_LONG).show();
            // Play Music

            // If the Music File doesn't exist in SD card (Not yet downloaded)
        } else {
            Toast.makeText(getApplicationContext(), "File doesn't exist under SD Card, downloading Mp3 from Internet", Toast.LENGTH_LONG).show();
            // Trigger Async Task (onPreExecute method)
            new DownloadMusicfromInternet().execute(file_url);
        }*/

    }

    // Show Dialog Box with Progress bar
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                prgDialog = new ProgressDialog(this);
                prgDialog.setMessage("Downloading New Version. Please wait...");
                prgDialog.setIndeterminate(false);
                prgDialog.setMax(100);
                prgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                prgDialog.setCancelable(false);
                prgDialog.show();
                return prgDialog;
            default:
                return null;
        }
    }

    // Async Task Class
    class DownloadMusicfromInternet extends AsyncTask<String, String, String> {

        // Show Progress bar before downloading Music
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Shows Progress Bar Dialog and then call doInBackground method
            showDialog(progress_bar_type);
        }

        // Download Music File from Internethttp://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_1mb.mp4
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);

                URLConnection conection = url.openConnection();
                conection.connect();
                //URLConnection conection = url.openConnection();
               // conection.set(false);
                //URL secondURL = new URL(conection.getHeaderField("Location"));
                //URLConnection conn = secondURL.openConnection();
                //conn.connect();
                // Get Music file length
                int lenghtOfFile = conection.getContentLength();//6000;
                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(),10*1024);
                // Output stream to write file in SD card
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/fieldiq.apk");
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // Publish the progress which triggers onProgressUpdate method
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // Write data to file
                    output.write(data, 0, count);
                }
                // Flush output
                output.flush();
                // Close streams
                output.close();
                input.close();
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        // While Downloading Music File
        protected void onProgressUpdate(String... progress) {
            // Set progress percentage
            prgDialog.setProgress(Integer.parseInt(progress[0]));
        }

        // Once Music File is downloaded
        @Override
        protected void onPostExecute(String file_url) {
            // Dismiss the dialog after the Music file was downloaded
            prgDialog.setProgress(0);
            dismissDialog(progress_bar_type);
           // Toast.makeText(getApplicationContext(), "Download complete, playing Music", Toast.LENGTH_LONG).show();
            // Play the music
           /* Intent intent = new Intent(Intent.ACTION_VIEW);

            File sdCard = Environment.getExternalStorageDirectory();
            File file = new File(sdCard, "/fieldiq.apk");

            intent.setDataAndType(Uri.fromFile(file), "video*//*");

            startActivity(intent);*/
           installAPkfile();
        }
    }

    public void installAPkfile(){

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File
                (Environment.getExternalStorageDirectory() + "/fieldiq.apk")), "application/vnd.android.package-archive");
        startActivity(intent);
    }
}
