package com.humaralabs.fieldrun.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.humaralabs.fieldrun.CommonFunctions;
import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.MainActivity;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.server.ServerInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ProfileFragment extends Fragment {

    private static final String TAG ="Update Profile" ;
    TinyDB tiny;
    ImageView imgView;
    String fname;
    String lname;
    String mobile;
    String email;
    private String filepath;
    File photoFile = null;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile,container,false);
        RelativeLayout imagerl=(RelativeLayout) v.findViewById(R.id.imagerl);
        imgView = (ImageView) v.findViewById(R.id.iconimage);
        tiny=new TinyDB(MainActivity.mcontext);

        final EditText fnameEdit=(EditText) v.findViewById(R.id.firstname);
        final EditText lnameEdit=(EditText) v.findViewById(R.id.LastName);
        final EditText mobileEdit=(EditText) v.findViewById(R.id.mobile);
        final EditText emailEdit=(EditText) v.findViewById(R.id.email);

        fnameEdit.setText(tiny.getString("firstName"));
        lnameEdit.setText(tiny.getString("lastName"));
        mobileEdit.setText(tiny.getString("mobileNumber"));
        emailEdit.setText(tiny.getString("emaiId"));


        imagerl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filepath=null;
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                try {
                    photoFile = createImageFile();
                    filepath = photoFile.getPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    // Start the Intent
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMG);

                }
            }

            public File createImageFile() throws IOException {
                // Create an image file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "profileImage_" + timeStamp + "_";



                return File.createTempFile(
                        imageFileName,
                        ".jpg",
                        getExternalFilesDir(null)
                );
            }
            private File getExternalFilesDir(Object o) {
            return  null;}



        });

        Button updaterl=(Button) v.findViewById(R.id.updatebtn);
        updaterl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fname=fnameEdit.getText().toString();
                lname=lnameEdit.getText().toString();
                mobile=mobileEdit.getText().toString();
                email=emailEdit.getText().toString();
                new update_profile().execute();
            }
        });

        setProfilePic();
        return v;
    }




    private void setProfilePic(){
        try {
            String bm = tiny.getString("ProfileImageBitmap");
            if (bm != null && !bm.equals("")) {
                imgView.setImageBitmap(CommonFunctions.StringToBitMap(bm));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private ProgressDialog Dialog = null;
    //Async class to update user profile
    private class update_profile extends AsyncTask<Void, Void, Void>
    {
        String Result="";
        protected void onPreExecute()
        {
            Dialog = new ProgressDialog(MainActivity.mcontext);
            Dialog.setMessage("Please wait...");
            Dialog.show();
            Dialog.setCanceledOnTouchOutside(false);
            Dialog.setCancelable(false);
        }
        protected void onPostExecute(Void unused)
        {
            if(Dialog != null && Dialog.isShowing())
            {
                Dialog.dismiss();
            }
            try {
                JSONObject data = new JSONObject(Result);
                if(data.getBoolean("status")) {
                    tiny.putString("firstName",fname);
                    tiny.putString("lastName",lname);
                    tiny.putString("emaiId",email);
                    tiny.putString("mobileNumber",mobile);
                    CommonFunctions.sendMessageToActivity(1, MainActivity.mcontext);
                    CommonFunctions._messageToShow="Profile Updated Successfully!!";
                }
                else{
                    CommonFunctions.sendMessageToActivity(1, MainActivity.mcontext);
                    CommonFunctions._messageToShow="Please Try Again Later!!";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        protected Void doInBackground(Void... params)
        {
            try{
                String url= Constants.ServerApiUrl+"mobile/profileUpdate";
                JSONObject data = new JSONObject();
                data.put("token",tiny.getString("loginToken"));
                data.put("firstName",fname);
                data.put("lastName",lname);
                data.put("email",email);
                data.put("mobileNumber",mobile);
                String iemino = tiny.getString("iemino");
                Result= ServerInterface.CallServerApi(data, url,55,tiny.getString("loginToken"),iemino);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }//end send_feedback class


    private static int RESULT_LOAD_IMG = 1;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

                Uri uri = data.getData();
                filepath=getRealPathFromURI(uri);
                try {
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                    }
                    catch (Exception e){
                        Log.d(TAG, "error in converting bitmap");
                    }
                    try {
                        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(filepath), 100, 100, true);
                    }
                    catch (Exception e){
                        Log.d(TAG, "error in converting bitmap");
                    }
                    // Log.d(TAG, String.valueOf(bitmap));
                    if(bitmap!=null) {
                        new SendProfileImageUploadToServer().execute();
                        tiny.putString("ProfileImageBitmap", CommonFunctions.BitMapToString(bitmap));
                        imgView.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(MainActivity.mcontext, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e){
            Toast.makeText(MainActivity.mcontext, "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }
    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private class SendProfileImageUploadToServer extends AsyncTask<String, String, String>
    {
        String Result="";
        protected void onPostExecute(String result)
        {
            if (ServerInterface.Imagecheckserver && !Result.equals("")) {
                try {
                    JSONObject mydata = new JSONObject(Result);
                    Log.d(TAG, "bulk sync response" + mydata.toString());
                    String res = mydata.getString("Result");
                    String message = mydata.optString("Message");
                    if (res.equals("OK")) {
                        Toast.makeText(getActivity(),"Profile image upload successfully.",Toast.LENGTH_LONG).show();
                    } else {
                        tiny.putString("ProfileImageBitmap", "");
                        Toast.makeText(getActivity(),message+" not uploaded",Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
            {
                Toast.makeText(getActivity(),"Please check internet connection.",Toast.LENGTH_LONG).show();
            }

        }
        protected String doInBackground(String... par)
        {
            try{
                String url;// = Constants.ServerApiUrl + "mobile/task/uploadimage";
                if(tiny==null)
                    tiny=new TinyDB(MainActivity.mcontext);
                String loginToken=tiny.getString("loginToken");
                        JSONObject params = new JSONObject();
                        url = Constants.ServerApiUrl + "mobile/task/uploadimage";
                        params.put("type", "profileImage");
                        params.put("token", loginToken);
                        Result = ServerInterface.UploadImageApi(new File(filepath), url, params,loginToken);

                    } catch (JSONException e1) {
                e1.printStackTrace();
            }


            return null;
        }


    }//end
}
