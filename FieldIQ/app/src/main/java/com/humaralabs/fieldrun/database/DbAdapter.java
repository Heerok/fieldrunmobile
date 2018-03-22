package com.humaralabs.fieldrun.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;


import com.humaralabs.fieldrun.BuildConfig;
import com.humaralabs.fieldrun.Constants;
import com.humaralabs.fieldrun.datastructure.Basket;
import com.humaralabs.fieldrun.datastructure.Disposition;
import com.humaralabs.fieldrun.datastructure.HyperBasket;
import com.humaralabs.fieldrun.datastructure.HyperBasketChild;
import com.humaralabs.fieldrun.datastructure.Notifications;
import com.humaralabs.fieldrun.datastructure.QuestionSet;
import com.humaralabs.fieldrun.datastructure.Task;
import com.humaralabs.fieldrun.datastructure.Trip;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class DbAdapter {
    private SQLiteDatabase database;
    private DBHelper databaseHelper;
    private static final String TAG = "Database";

    WifiManager wifiManager;
    Context context;

    public static final int NETWORK_TYPE_EHRPD = 14;
    public static final int NETWORK_TYPE_EVDO_B = 12;
    public static final int NETWORK_TYPE_HSPAP = 15;
    public static final int NETWORK_TYPE_IDEN = 11;
    public static final int NETWORK_TYPE_LTE = 13;


    public DbAdapter(Context context) {
        databaseHelper = new DBHelper(context);
    }

    public DbAdapter open(boolean writable) throws Exception{

        if( database != null && writable == true && database.isReadOnly())
            ////database.close();

        if( database != null && database.isOpen())
            return this;

        if( writable)
            database = databaseHelper.getWritableDatabase();
        else
            database = databaseHelper.getReadableDatabase();

        return this;
    }

    //////////dispostiion crud//////////////////////////
    public void insertDispostionData(String name,String value, String type,String actiontype) {
        // TODO Auto-generated method stub
        try {
           open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.disposition_table + " where " + Constants.disposition_name + " = '" + name + "'";

            int count=0;
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count = cursor.getCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.disposition_name, name);
                values.put(Constants.disposition_value, value);
                values.put(Constants.disposition_type, type);
                values.put(Constants.disposition_actiontype, actiontype);

                if (count == 0) {
                    database.insert(Constants.disposition_table, null, values);
                } else {
                    database.update(Constants.disposition_table, values, Constants.disposition_name + "='" + name + "'", null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
    }

    public ArrayList<Disposition> getAllDisposition(String Type, String action_type) {
        ArrayList<Disposition> AllDispositionList=new ArrayList<>();

        try {
            open(false);
            String selectQuery = "";

            if (action_type.equals("fail")) {
                selectQuery = "SELECT * " + " FROM " + Constants.disposition_table + " where " + Constants.disposition_type + " = '" + Type
                        + "' and " + Constants.disposition_actiontype + " != 'success'";
            } else {
                selectQuery = "SELECT * " + " FROM " + Constants.disposition_table + " where " + Constants.disposition_type + " = '" + Type
                        + "' and " + Constants.disposition_actiontype + " = 'success'";
                    }

            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                try {
                    // looping through all rows and adding to list
                    if (cursor.moveToFirst()) {
                        do {
                            String name = cursor.getString(cursor.getColumnIndex(Constants.disposition_name));
                            String value = cursor.getString(cursor.getColumnIndex(Constants.disposition_value));
                            String type = cursor.getString(cursor.getColumnIndex(Constants.disposition_type));
                            String actiontype = cursor.getString(cursor.getColumnIndex(Constants.disposition_actiontype));
                            Disposition disobj = new Disposition(name, value, type, actiontype);
                            AllDispositionList.add(disobj);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
        return AllDispositionList;
    }

    // Questionset data
    //////////dispostiion crud//////////////////////////
    public void insertQuestionsData(String questionId,String platformId,String question, String answerType,String itemCategory,String noEffect) {
        // TODO Auto-generated method stub
        try {
            open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.question_table + " where " + Constants.questionId + " = '" + questionId + "'";

            int count=0;
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count = cursor.getCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.questionId, questionId);
                values.put(Constants.plateform_id, platformId);
                values.put(Constants.itemCategory, itemCategory);
                values.put(Constants.question, question);
                values.put(Constants.answer_type, answerType);
                values.put(Constants.noEffect, noEffect);

                if (count == 0) {
                    database.insert(Constants.question_table, null, values);
                } else {
                    database.update(Constants.question_table, values, Constants.questionId + "='" + questionId + "'", null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
    }

    // Get Question data from question table
    public ArrayList<QuestionSet> getAllQuestionSet(String plateform_id ,String category) {
        ArrayList<QuestionSet> AllQuestionSetList=new ArrayList<>();

        try {
            open(false);
            String selectQuery = "";
            if(category.equals("") || category.equals(null) && !plateform_id.equals("All"))
                selectQuery = "SELECT * " + " FROM " + Constants.question_table + " where " + Constants.plateform_id + " = " + plateform_id;
            else
                selectQuery = "SELECT * " + " FROM " + Constants.question_table + " where " + Constants.plateform_id + " = " + plateform_id+ " and " + Constants.itemCategory + " = '"+category+"'";

            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                try {
                    // looping through all rows and adding to list
                    if (cursor.moveToFirst()) {
                        do {
                            String questionId = cursor.getString(cursor.getColumnIndex(Constants.questionId));
                            String plateformid = cursor.getString(cursor.getColumnIndex(Constants.plateform_id));
                            String itemCategory = cursor.getString(cursor.getColumnIndex(Constants.itemCategory));
                            String question = cursor.getString(cursor.getColumnIndex(Constants.question));
                            String answer_type = cursor.getString(cursor.getColumnIndex(Constants.answer_type));
                            String noEffect = cursor.getString(cursor.getColumnIndex(Constants.noEffect));
                            QuestionSet disobj = new QuestionSet(questionId,plateformid, question, answer_type,itemCategory,noEffect);
                            AllQuestionSetList.add(disobj);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
        return AllQuestionSetList;
    }

    ///////////////////////////////   Trips Crud   /////////////////////////////////////
    public void insertTripData(Long tripId,String tripDate,String trip_expiryDateTime, int numTasks,String zipCode,String origin,int status,String trip_facility,String trip_type) {
        // TODO Auto-generated method stub
        try {
           open(false);
            int count= 0;
            try {
                String selectQuery = "SELECT * " + " FROM " + Constants.trip_table + " where " + Constants.trip_id + " = '" + tripId + "'";
                count = 0;
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count = cursor.getCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }

            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.trip_id, tripId);
                values.put(Constants.trip_date, tripDate);
                values.put(Constants.trip_expiryDateTime, trip_expiryDateTime);
                values.put(Constants.trip_num_task, numTasks);
                values.put(Constants.trip_zipcode, zipCode);
                values.put(Constants.trip_origin, origin);
                values.put(Constants.trip_facility, trip_facility);
                values.put(Constants.trip_type,trip_type);
                values.put(Constants.trip_status, status);//0 means pending

                if (count == 0) {
                    database.insert(Constants.trip_table, null, values);
                } else {
                    database.update(Constants.trip_table, values, Constants.trip_id + "=" + tripId, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }
        catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
    }

    //this function is used to get today trip count for a particulat type
    public int GetTodayTripsCountFromDataBase(String type){
        int count=0;
        try {
            String TodayDate = GetCurrentDate();
           open(false);
            String selectQuery = "";
            if (type.equals("All"))
                selectQuery = "SELECT * " + " FROM " + Constants.trip_table + " where " + Constants.trip_date + " = '" + TodayDate + "'";
            else if (type.equals("Active"))
                selectQuery = "SELECT * " + " FROM " + Constants.trip_table + " where " + Constants.trip_date + " = '" + TodayDate + "' and " + Constants.trip_status + " = " + Constants.trip_status_active_code;
            else if (type.equals("Pending"))
                selectQuery = "SELECT * " + " FROM " + Constants.trip_table + " where " + Constants.trip_date + " = '" + TodayDate + "' and " + Constants.trip_status + " = " + Constants.trip_status_pending_code;
            else if (type.equals("Complete"))
                selectQuery = "SELECT * " + " FROM " + Constants.trip_table + " where " + Constants.trip_date + " = '" + TodayDate + "' and " + Constants.trip_status + " = " + Constants.trip_status_complete_code;
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count = cursor.getCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
        return count;
    }

    //this function is used to fetch trip data for today
    public ArrayList<Trip> GetTripsFromDatabase(String type) {
        ArrayList<Trip> AllTripDataList=new ArrayList<>();
        try {
            String TodayDate = GetCurrentDate();
           open(false);
            String selectQuery = "";
            if (type.equals("All"))
                selectQuery = "SELECT * " + " FROM " + Constants.trip_table;
            else
                selectQuery = "SELECT * " + " FROM " + Constants.trip_table + " where "  + Constants.trip_status + "=" + Constants.trip_status_active_code;

            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            Long tripId = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.trip_id)));
                            //SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);

                            String tripDate = cursor.getString(cursor.getColumnIndex(Constants.trip_date));
                            int numTasks = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.trip_num_task)));
                            String zipCode = cursor.getString(cursor.getColumnIndex(Constants.trip_zipcode));
                            String origin = cursor.getString(cursor.getColumnIndex(Constants.trip_origin));
                            String trip_facility = cursor.getString(cursor.getColumnIndex(Constants.trip_facility));
                            String trip_type = cursor.getString(cursor.getColumnIndex(Constants.trip_type));
                            String expiry_date = cursor.getString(cursor.getColumnIndex(Constants.trip_expiryDateTime));
                            int status = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.trip_status)));
                            Trip tripobj = new Trip(tripId, tripDate, numTasks, zipCode, origin, status,expiry_date,trip_facility,trip_type);
                            AllTripDataList.add(tripobj);

                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }
        catch(Exception e){
            Log.e(TAG, "error in database", e);
        }
        return AllTripDataList;
    }

    //this function is used to check status for a particular trip
    public int getTripStatus(Long trip_id){
        int status=0;
        try {
           open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.trip_table + " where " + Constants.trip_id +
                    " = '" + trip_id + "'";

            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            status = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.trip_status)));
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }
        catch(Exception e){
            Log.e(TAG, "error in database", e);
        }
        return status;
    }

    //this function is use to get currently active trip
    public long getActiveTripId(){
        long tripid=0;
        try {
           open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.trip_table + " where " + Constants.trip_status +
                    " = '" + Constants.trip_status_active_code + "'";

            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                try {
                    if (cursor.getCount() > 0) {
                        // looping through all rows and adding to list
                        if (cursor.moveToFirst()) {
                            do {
                                tripid = Long.parseLong(cursor.getString(cursor.getColumnIndex(Constants.trip_id)));
                            } while (cursor.moveToNext());
                        }
                    }
                } catch (Exception e) {
                    tripid=0;
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                tripid=0;
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }catch(Exception e){
            tripid=0;
            Log.e(TAG, "error in database", e);
        }
        return tripid;
    }

    //this function is use to get currently active trip
    public JSONObject getActiveTripDetails(){
       JSONObject tripdetails=new JSONObject();
        try {
            open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.trip_table + " where " + Constants.trip_status +
                    " = '" + Constants.trip_status_active_code + "'";

            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                try {
                    if (cursor.getCount() > 0) {
                        // looping through all rows and adding to list
                        if (cursor.moveToFirst()) {
                            do {
                                String tripid = cursor.getString(cursor.getColumnIndex(Constants.trip_id));
                                String trip_expiryDateTime = cursor.getString(cursor.getColumnIndex(Constants.trip_expiryDateTime));
                                tripdetails.put("tripid", tripid);
                                tripdetails.put("trip_expiryDateTime", trip_expiryDateTime);

                            } while (cursor.moveToNext());
                        }
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {

                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }catch(Exception e){
            Log.e(TAG, "error in database", e);
        }
        return tripdetails;
    }

    //if new task is added in running trip then this function will call
    public void updateTripStatus(int status,Long tripId){
        try {
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.trip_status, status);
                int b = database.update(Constants.trip_table, values, Constants.trip_id + "=" + tripId, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }
        catch(Exception e) {
            Log.e(TAG, "error in database", e);
        }
    }

    public void increaseTripNumTask(Long tripId){
        try {
            int numtask = 0;
           open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.trip_table + " where " + Constants.trip_id +
                    " = '" + tripId + "'";

            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                try {
                    if (cursor.getCount() > 0) {
                        // looping through all rows and adding to list
                        if (cursor.moveToFirst()) {
                            do {
                                numtask = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.trip_num_task)));
                            } while (cursor.moveToNext());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.trip_num_task, (numtask + 1));
                int b = database.update(Constants.trip_table, values, Constants.trip_id + "=" + tripId, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch(Exception e){
            Log.e(TAG, "error in database", e);
        }
    }

    public void deletePreviousData() {
        try {
            open(true);
            try {
                database.delete(Constants.trip_table, Constants.KEY_ID + "!=''", null);
                database.delete(Constants.task_table, Constants.KEY_ID + "!=''", null);
                database.delete(Constants.basket_table, Constants.KEY_ID + "!=''", null);
                //database.delete(Constants.notification_table, Constants.KEY_ID + "!=''", null);
                database.delete(Constants.hyper_basket_table, Constants.KEY_ID + "!=''", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
    }


    ///////////////////////////////   Tasks Crud   /////////////////////////////////////
    public String insertTaskData(Long tripId, Long taskId, String ref,String platformId, long pickups, String name,
                                 String address, String zipCode, String phone, String taskType,
                                 int pickupQty, String reason, String delieveryDateTime, String comments,
                                 int taskstatus, String payment_mode, String basket_id,
                                 String consignee_number, String consignee_name, String pinno, int retrycount,String amount,
                                 String codamount,String itemCategory,String itemDescription,String mandatoryPhotocount,String optionPhotocount) {
        String res="";
        try {
            open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_id + " = '" + taskId + "'";
            int count=0;
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count = cursor.getCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.task_tripid, tripId);
                values.put(Constants.task_id, taskId);
                values.put(Constants.task_ref, ref);
                values.put(Constants.platformId, platformId);
                values.put(Constants.itemCategory, itemCategory);
                values.put(Constants.itemDescription, itemDescription);
                values.put(Constants.pickups, pickups);
                values.put(Constants.name, name);
                values.put(Constants.address, address);
                values.put(Constants.zipCode, zipCode);
                values.put(Constants.phone, phone);
                values.put(Constants.task_type, taskType);
                values.put(Constants.reason, reason);
                values.put(Constants.delievery_date_time, delieveryDateTime);
                values.put(Constants.comments, comments);
                values.put(Constants.payment_mode,payment_mode);
                values.put(Constants.basket_id,basket_id);
                values.put(Constants.consignee_number,consignee_number);
                values.put(Constants.consignee_name,consignee_name);
                values.put(Constants.task_pinno,pinno);
                values.put(Constants.task_amount,amount);
                values.put(Constants.mandatoryPhotocount,mandatoryPhotocount);
                values.put(Constants.optionPhotocount,optionPhotocount);
                values.put(Constants.task_codamount,codamount);
                values.put(Constants.task_retry_count,retrycount);
                values.put(Constants.task_status, taskstatus);

                if (count == 0) {
                    values.put(Constants.pickup_qty, pickupQty);
                    res="NEW";
                    Long a=database.insert(Constants.task_table, null, values);
                    Log.e(TAG, "error in database  "+a);
                } else {
                    res="OLD";
                    database.update(Constants.task_table, values, Constants.task_id + "=" + taskId, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch(Exception e){
            Log.e(TAG, "error in database", e);
        }
        return res;
    }

    //get task count for particular trip
    public int GetTaskForParticulatTripsCount(int status,String trip_idOrBasketIDOrRefNo,String Type){
        int count=500;
        try {
            open(false);
            String selectQuery="";
            if(Type.equals("Basket") && status==0){
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.basket_id +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and (" + Constants.task_status + " = " + status+" OR "+Constants.task_status +
                        " = "+Constants.task_status_start_code+")";
            }
            else if(Type.equals("Basket")){
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.basket_id +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and " + Constants.task_status + " = " + status;
            }
            else if(Type.equals("HyperBasket")){
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_ref +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and (" + Constants.task_status + " = " + status+" OR "+Constants.task_status +
                        " = "+Constants.task_status_start_code+")";
            }
            else if(Type.equals("HyperBasketOther")){
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_ref +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and " + Constants.task_status + " = " + status;
            }
            else if(Type.equals("HyperBasketPick")){
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_ref +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and " + Constants.task_type + " = 'HYP-PICKUP'";
            }
            else if(Type.equals("HyperBasketDel")){
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_ref +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and " + Constants.task_type + " = 'HYP-DELIVERY'" ;
            }
            else if(status==0)
            {
                selectQuery = "SELECT * FROM " + Constants.task_table + " where " + Constants.task_tripid +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and (" + Constants.task_status + " = '" + status+"' OR "+Constants.task_status +
                        " = '"+Constants.task_status_start_code+"' OR " + Constants.task_status + " = "+Constants.task_status_doorstep_code +")";
            }
            else {
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_tripid +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and " + Constants.task_status + " = " + status;
            }

            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count = cursor.getCount();
                } catch (Exception e) {
                    count=500;
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                count=500;
                e.printStackTrace();
            }
        }
        catch (Exception e){
            count=500;
            Log.e(TAG, "error in database", e);
        }
        return count;
    }

    //get all active task count
    //get task count for particular trip
    public int GetCountForAllActiveTask(){
        int count=500;
        try {
            open(false);
            String selectQuery="";
            selectQuery = "SELECT * FROM " + Constants.task_table
                    + " where " + Constants.task_status + " = '" + Constants.task_status_pending_code+"' OR "+Constants.task_status +
                        " = '"+Constants.task_status_start_code+"' OR " + Constants.task_status + " = "+Constants.task_status_doorstep_code;



            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count = cursor.getCount();
                } catch (Exception e) {
                    count=500;
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                count=500;
                e.printStackTrace();
            }
        }
        catch (Exception e){
            count=500;
            Log.e(TAG, "error in database", e);
        }
        return count;
    }
    //get all task list fo aparticular trip
    public ArrayList<Task> GetTaskForParticulatTrips(int status,String trip_idOrBasketIDOrRefNo,String Type,Boolean hideDelieveryButtons){
        ArrayList<Task> AllTaskDataList = new ArrayList<>();
        try {
           open(false);
            String selectQuery ="";
            if(Type.equals("Basket") && status==0){
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.basket_id +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and (" + Constants.task_status + " = " + status+" OR "+Constants.task_status +
                        " = "+Constants.task_status_start_code+")";
            }
            else if(Type.equals("Basket")){
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.basket_id +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and " + Constants.task_status + " = " + status;
            }
            else if(Type.equals("HyperBasket") && status==0){
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_ref +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and (" + Constants.task_status + " = " + status+" OR "+Constants.task_status +
                        " = "+Constants.task_status_start_code+")";
            }
            else if(Type.equals("HyperBasket")){
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_ref +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and " + Constants.task_status + " = " + status;
            }
            else if(status==0)
            {
                selectQuery = "SELECT * FROM " + Constants.task_table + " where " + Constants.task_tripid +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and (" + Constants.task_status + " = '" + status+"' OR "+Constants.task_status +
                        " = '"+Constants.task_status_start_code+"' OR " + Constants.task_status + " = "+Constants.task_status_doorstep_code +")";
            }
            else {
                selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_tripid +
                        " = '" + trip_idOrBasketIDOrRefNo + "' and " + Constants.task_status + " = " + status;
            }
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            Long tripId = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.task_tripid)));
                            Long taskId = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.task_id)));
                            String ref = cursor.getString(cursor.getColumnIndex(Constants.task_ref));
                            String platformId = cursor.getString(cursor.getColumnIndex(Constants.platformId));
                            String itemCategory = cursor.getString(cursor.getColumnIndex(Constants.itemCategory));
                            String itemDescription = cursor.getString(cursor.getColumnIndex(Constants.itemDescription));
                            long pickups = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.pickups)));
                            String name = cursor.getString(cursor.getColumnIndex(Constants.name));
                            String address = cursor.getString(cursor.getColumnIndex(Constants.address));
                            String zipcode = cursor.getString(cursor.getColumnIndex(Constants.zipCode));
                            String phone = cursor.getString(cursor.getColumnIndex(Constants.phone));
                            String taskType = cursor.getString(cursor.getColumnIndex(Constants.task_type));
                            int pickupQty = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.pickup_qty)));
                            String reason = cursor.getString(cursor.getColumnIndex(Constants.reason));
                            String delieveryDateTime = cursor.getString(cursor.getColumnIndex(Constants.delievery_date_time));//completed by datetime
                            String comments = cursor.getString(cursor.getColumnIndex(Constants.comments));
                            String taskstatus = cursor.getString(cursor.getColumnIndex(Constants.task_status));
                            String consignee_number=cursor.getString(cursor.getColumnIndex(Constants.consignee_number));
                            String consignee_name=cursor.getString(cursor.getColumnIndex(Constants.consignee_name));
                            String payment_mode=cursor.getString(cursor.getColumnIndex(Constants.payment_mode));
                            String pinno=cursor.getString(cursor.getColumnIndex(Constants.task_pinno));
                            String amount=cursor.getString(cursor.getColumnIndex(Constants.task_amount));
                            String mandatoryPhotocount=cursor.getString(cursor.getColumnIndex(Constants.mandatoryPhotocount));
                            String optionPhotocount=cursor.getString(cursor.getColumnIndex(Constants.optionPhotocount));
                            String codamount=cursor.getString(cursor.getColumnIndex(Constants.task_codamount));

                            int retry_count=Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.task_retry_count)));

                            if (taskstatus.equals("NEW")) {
                                taskstatus = "0";
                            }
                            if(!hideDelieveryButtons && taskType.equals("HYP-DELIVERY")){
                                continue;
                            }

                            Task taskobj = new Task(tripId, taskId, ref,platformId, pickups, name, address, zipcode,
                                    phone, taskType, pickupQty, reason, delieveryDateTime, comments, taskstatus,
                                    consignee_number,consignee_name,payment_mode,pinno,retry_count,
                                    amount,codamount,itemCategory,itemDescription,mandatoryPhotocount,optionPhotocount);
                            AllTaskDataList.add(taskobj);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
        return AllTaskDataList;
    }


    public ArrayList<Task> GetAllHyperTaskForParticulatTrips(String BasketRefNo){
        ArrayList<Task> AllTaskDataList = new ArrayList<>();
        try {
            open(false);

            String selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_ref +
                        " = '" + BasketRefNo + "'";

            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            Long tripId = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.task_tripid)));
                            Long taskId = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.task_id)));
                            String ref = cursor.getString(cursor.getColumnIndex(Constants.task_ref));
                            String platformId = cursor.getString(cursor.getColumnIndex(Constants.platformId));
                            String itemCategory = cursor.getString(cursor.getColumnIndex(Constants.itemCategory));
                            String itemDescription = cursor.getString(cursor.getColumnIndex(Constants.itemDescription));
                            long pickups = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.pickups)));
                            String name = cursor.getString(cursor.getColumnIndex(Constants.name));
                            String address = cursor.getString(cursor.getColumnIndex(Constants.address));
                            String zipcode = cursor.getString(cursor.getColumnIndex(Constants.zipCode));
                            String phone = cursor.getString(cursor.getColumnIndex(Constants.phone));
                            String taskType = cursor.getString(cursor.getColumnIndex(Constants.task_type));
                            int pickupQty = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.pickup_qty)));
                            String reason = cursor.getString(cursor.getColumnIndex(Constants.reason));
                            String delieveryDateTime = cursor.getString(cursor.getColumnIndex(Constants.delievery_date_time));//completed by datetime
                            String comments = cursor.getString(cursor.getColumnIndex(Constants.comments));
                            String taskstatus = cursor.getString(cursor.getColumnIndex(Constants.task_status));
                            String consignee_number=cursor.getString(cursor.getColumnIndex(Constants.consignee_number));
                            String consignee_name=cursor.getString(cursor.getColumnIndex(Constants.consignee_name));
                            String payment_mode=cursor.getString(cursor.getColumnIndex(Constants.payment_mode));
                            String pinno=cursor.getString(cursor.getColumnIndex(Constants.task_pinno));
                            String amount=cursor.getString(cursor.getColumnIndex(Constants.task_amount));
                            String mandatoryPhotocount=cursor.getString(cursor.getColumnIndex(Constants.mandatoryPhotocount));
                            String optionPhotocount=cursor.getString(cursor.getColumnIndex(Constants.optionPhotocount));
                            String codamount=cursor.getString(cursor.getColumnIndex(Constants.task_codamount));

                            int retry_count=Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.task_retry_count)));

                            if (taskstatus.equals("NEW")) {
                                taskstatus = "0";
                            }
                            Task taskobj = new Task(tripId, taskId, ref,platformId, pickups, name, address, zipcode,
                                    phone, taskType, pickupQty, reason, delieveryDateTime, comments, taskstatus,
                                    consignee_number,consignee_name,payment_mode,pinno,retry_count,amount,
                                    codamount,itemCategory,itemDescription,mandatoryPhotocount,optionPhotocount);
                            AllTaskDataList.add(taskobj);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
        return AllTaskDataList;
    }

    //checkinng task status
    public int getTaskStatus(Long task_id){
        int status=0;
        try {
           open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_id +
                    " = '" + task_id + "'";

            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            status = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.task_status)));
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch(Exception e){
            Log.e(TAG, "error in database", e);
        }
        return status;
    }

    public Boolean CheckPickupDoneORNot(Long s,String ref) {
        Boolean pickupDoneORNot=false;
        try {
            open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.task_table
                    + " where " + Constants.task_tripid + " = '" + s + "' and "
                    + Constants.task_ref + " = '"+ref+"' and "
                    + Constants.task_type + " = 'HYP-PICKUP' and "
                    + Constants.task_status + " = " + Constants.task_status_done_code;

            int count=0;
            Cursor cursor = database.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            try {
                count = cursor.getCount();
                if(count>0)
                    pickupDoneORNot=true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pickupDoneORNot;
    }

    //checkinng task retry count
    public int getAvailableRetryCountForParticularTask(Long task_id){
        int retrycount=0;
        try {
            open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.task_table + " where " + Constants.task_id +
                    " = '" + task_id + "'";

            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            retrycount = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.task_retry_count)));
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch(Exception e){
            Log.e(TAG, "error in database", e);
        }
        return retrycount;
    }

    //checkinng task retry count
    public int getTotalBasketPickedQuantity(Long basket_id){
        int qty=0;
        try {
            open(false);
            String selectQueryBasket = "SELECT basket_picked_qty " + " FROM " + Constants.basket_table + " where " + Constants.basket_server_id +
                    " = '" + basket_id + "'";

            try {
                Cursor cursor = database.rawQuery(selectQueryBasket, null);
                // looping through all rows and adding to list
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("basket_picked_qty")));
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(qty==0) {
                String selectQuery = "SELECT SUM(pickup_qty) as tt " + " FROM " + Constants.task_table + " where " + Constants.basket_id +
                        " = '" + basket_id + "'";

                try {
                    Cursor cursor = database.rawQuery(selectQuery, null);
                    // looping through all rows and adding to list
                    try {
                        if (cursor.moveToFirst()) {
                            do {
                                qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("tt")));
                            } while (cursor.moveToNext());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch(Exception e){
            Log.e(TAG, "error in database", e);
        }
        return qty;
    }

    //update task status
    public void updateTaskStatus(int status,Long taskId){
        try {
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.task_status, status);
                database.update(Constants.task_table, values, Constants.task_id + "=" + taskId, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
    }




    //update task status
    public void updateHosQuantity(int qty,Long taskId){
        try {
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.pickup_qty, qty);
                database.update(Constants.task_table, values, Constants.task_id + "=" + taskId, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
    }

    //update task status
    public void updateBasketStatus(int status,Long basketId){
        try {
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.basket_status, status);
                database.update(Constants.basket_table, values, Constants.basket_server_id + "=" + basketId, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
    }


    //update task retry count
    public void decreaseRetryCountForParticularTask(Long taskId,int updatedcount){
        try {
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.task_retry_count, updatedcount);
                database.update(Constants.task_table, values, Constants.task_id + "=" + taskId, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*if (database != null && database.isOpen()) {
                ////database.close();
            }*/
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
    }

    /////////////////////event crud///////////////
    public long insertFieldEvent(String tripId,String tripExpiryDateTime,String gpsevent,Location location,String params,Context context) {
        //get current active trip if active trip is available then save location event
        long Result=0;
        try {
            long tripid=0;
            String tripExpiry="";
            if(tripId!=null && !tripId.equals("")) {
                tripid = Long.parseLong(tripId);
                tripExpiry=tripExpiryDateTime;
            }
            else {
                JSONObject tripdetails = getActiveTripDetails();
                int activetaskCount=GetCountForAllActiveTask();
                if(tripdetails.length()>0 && activetaskCount!=0){
                    tripid = Long.parseLong(tripdetails.getString("tripid"));
                    tripExpiry=tripdetails.getString("trip_expiryDateTime");
                }
            }

            if (tripid == 0 && BuildConfig.FLAVOR.equals("fieldiq")) {
                    return 0;
            }
            //abir
            Double latitude = (double) 0, longitude = (double) 0, altitude = (double) 0;
            Float accuracy= (float) 0, bearing= (float) 0, speed= (float) 0;
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                altitude=location.getAltitude();
                accuracy=location.getAccuracy();
                bearing=location.getBearing();
                speed=location.getSpeed();
                //if(speed==0 && gpsevent.equals("gpsevent")){
                    //return 0;
                //}
            }

            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.event_tripId, tripid);
                values.put(Constants.event_type, gpsevent);
                values.put(Constants.latitude, latitude);
                values.put(Constants.longitude, longitude);
                values.put(Constants.accuracy, accuracy);
                values.put(Constants.altitude, altitude);
                values.put(Constants.altitude, altitude);
                values.put(Constants.bearing, bearing);
                values.put(Constants.speed, speed);
                values.put(Constants.battery, batteryLevel(context));
                values.put(Constants.signal_status, getNetworkStatus(context));
                values.put(Constants.ts, getCurrentTime());
                values.put(Constants.params, params);
                values.put(Constants.event_date, GetCurrentDate());
                values.put(Constants.event_tripExpiryDateTime, tripExpiry);
                values.put(Constants.reqId, "");
                values.put(Constants.event_status, 0);
                Result = database.insert(Constants.event_table, null, values);
            } catch (Exception e) {
                Result=0;
                e.printStackTrace();
            }
        }
        catch (Exception e){
            Result=0;
            Log.e(TAG, "error in database", e);
        }
        return  Result;
    }

    Random r = new Random();
    //reading all pending events
    public JSONObject getPendingEvents(int maxEvents,String loginToken,String type) {
        JSONObject jo = new JSONObject();
        try {
            Log.d(TAG, "reading event");
            long reqid = r.nextInt(9999999) + 1;
            int currentcount = 0;
            int lastFieldEventPrimarryId = 0;
            JSONArray allparams = new JSONArray();
           open(false);
            try {
                Cursor cursor;

                String selectQuery;
                if(type.equals("ALL")) {
                    selectQuery = "SELECT * " + " FROM " + Constants.event_table + " where " + Constants.event_type +"!= 'imgupload'";
                }
                else if(type.equals("ALLTASK")){
                    selectQuery = "SELECT * " + " FROM " + Constants.event_table + " where " + Constants.event_type +"= 'taskupdate'";
                }
                else if(type.equals("ALLGPS")){
                    selectQuery = "SELECT * " + " FROM " + Constants.event_table + " where " + Constants.event_type +"= 'gpsevent'";
                }
                else{
                    selectQuery = "SELECT * " + " FROM " + Constants.event_table + " where " + Constants.event_status +
                            " = '" + Constants.event_status_pending_code + "' and event_type != 'imgupload'";
                }
                cursor = database.rawQuery(selectQuery, null);
                try {
                    // looping through all rows and adding to list
                    if (cursor.moveToFirst()) {
                        do {
                            lastFieldEventPrimarryId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.KEY_ID)));
                            Long tripId = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.event_tripId)));
                            String eventType = cursor.getString(cursor.getColumnIndex(Constants.event_type));
                            Double latitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex(Constants.latitude)));
                            Double longitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex(Constants.longitude)));
                            Float accuracy = Float.parseFloat(cursor.getString(cursor.getColumnIndex(Constants.accuracy)));
                            Double altitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex(Constants.altitude)));
                            Float bearing = Float.parseFloat(cursor.getString(cursor.getColumnIndex(Constants.bearing)));
                            Float speed = Float.parseFloat(cursor.getString(cursor.getColumnIndex(Constants.speed)));
                            int battery = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.battery)));
                            String signal_status = cursor.getString(cursor.getColumnIndex(Constants.signal_status));
                            Long ts = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.ts)));
                            String params = cursor.getString(cursor.getColumnIndex(Constants.params));

                            JSONObject paramsObj;
                            if(params.equals(""))
                                paramsObj = new JSONObject();
                            else
                                paramsObj = new JSONObject(params);

                            paramsObj.put("tripId", tripId);
                            paramsObj.put("eventType", eventType);
                            paramsObj.put("latitude", latitude);
                            paramsObj.put("longitude", longitude);
                            paramsObj.put("accuracy", accuracy);
                            paramsObj.put("altitude", altitude);
                            paramsObj.put("bearing", bearing);
                            paramsObj.put("speed", speed);
                            paramsObj.put("battery", battery);
                            paramsObj.put("signalStatus", signal_status);
                            paramsObj.put("ts", ts);
                            allparams.put(paramsObj);
                            currentcount++;
                            if (currentcount > maxEvents) break;
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateEventReqId(lastFieldEventPrimarryId, reqid,type);
            jo.put("token", loginToken);
            jo.put("push", "yes");
            jo.put("updates", allparams);
            jo.put("reqId", reqid);
        } catch (Exception e) {
            Log.e(TAG, "error in reading pending events for a trip", e);
        }
        return jo;
    }

    //reeading pending image upload event
    public JSONObject getPendingImageUploadEvent() {
        JSONObject jo = new JSONObject();
        try {
            long reqid = 0;
            int lastFieldEventPrimarryId = 0;
           open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.event_table + " where  event_type = 'imgupload' order by id desc LIMIT 1";
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            String params = cursor.getString(cursor.getColumnIndex(Constants.params));
                            JSONObject paramsObj = new JSONObject(params);
                            lastFieldEventPrimarryId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.KEY_ID)));
                            long taskId = paramsObj.getLong("taskId");
                            String filename = paramsObj.getString("filename");
                            reqid=taskId;
                            jo.put("filename", filename);
                            jo.put("taskId", taskId);
                            jo.put("reqId", taskId);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateEventReqId(lastFieldEventPrimarryId, reqid, "Image");
        } catch (Exception e) {
            Log.e(TAG, "error in reading pending events for a trip", e);
        }
        return jo;
    }

    //update requestid in local evennt table
    private void updateEventReqId(int FieldEventPrimaryId,long reqid,String type){
        try {
            long a=0;
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.reqId, reqid);
                values.put(Constants.event_status, Constants.event_status_done_code);
                if (type.equals("Image"))
                    a=database.update(Constants.event_table, values, Constants.KEY_ID + "=" + FieldEventPrimaryId, null);// + " and " + Constants.event_status + "=" + Constants.event_status_pending_code
                else if (type.equals("ALLTASK"))
                    a=database.update(Constants.event_table, values, Constants.KEY_ID + "<=" + FieldEventPrimaryId + " and " + Constants.event_type + "='taskupdate'", null);
                else if (type.equals("ALLGPS"))
                    a=database.update(Constants.event_table, values, Constants.KEY_ID + "<=" + FieldEventPrimaryId + " and " + Constants.event_type + "='gpsevent'", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }
        catch(Exception e){
            Log.e(TAG, "error in database", e);
        }
    }

    public int getPendingEventsCount(String type) {
        int count=0;
        String selectQuery;
        try {
            Log.d(TAG, "reading event");
            open(false);
            if(type.equals("image"))
            {
                selectQuery = "SELECT * " + " FROM " + Constants.event_table + " where  event_type = 'imgupload'";
            }
            else if(type.equals("event"))
            {
                selectQuery = "SELECT * " + " FROM " + Constants.event_table + " where  event_type = 'gpsevent'";
            }
            else
            {
                selectQuery = "SELECT * " + " FROM " + Constants.event_table + " where  event_type != 'imgupload'";
            }
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                try {
                    count=cursor.getCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "error in reading pending events for a trip", e);
        }
        return count;
    }

    /*public int getPendingEventsCount() {
        int count=0;
        try {
            Log.d(TAG, "reading event");
           open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.event_table + " where  event_type != 'imgupload'";
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                try {
                    count=cursor.getCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "error in reading pending events for a trip", e);
        }
        return count;
    }*/
    //delete uploaded events
    public void deleteUpdatedEvents(int resid,String type) {
        try {
            open(true);
            long a=0;
            try {
                switch (type) {
                    case "ALL":
                        a=database.delete(Constants.event_table, Constants.KEY_ID + "!='' and event_type != 'imgupload'", null);
                        break;
                    case "DATE":
                        a=database.delete(Constants.event_table,Constants.event_date + "!='" + GetCurrentDate() + "'", null);
                        break;
                    case "NOTALL":
                        a=database.delete(Constants.event_table, Constants.reqId + "=" + resid, null);
                        break;
                    default:
                        a=database.delete(Constants.event_table, Constants.reqId + "=" + resid, null);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch(Exception e){
            Log.e(TAG, "error in database", e);
        }
    }

    //updating event status
    public void updateEventStatus(int status,int reqId){
        try{
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.event_status, status);
                int b = database.update(Constants.event_table, values, Constants.reqId + "=" + reqId, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
    }

    //checkinng battery level
    private int batteryLevel(Context context)
    {
        int level   = 0;
        int scale   = 0;
        try {
            Intent intent  = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (intent != null) {
                level   = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                scale   = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (level*100)/scale;
    }


    ///////////////////Notifiaction crud///////////// ///

    public void insertNewNotification(String desc) {
        try {
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.noti_description, desc);
                values.put(Constants.noti_date, GetCurrentDAteTime());
                values.put(Constants.noti_status, 0);//o means not viewed
                database.insert(Constants.notification_table, null, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
    }

    //this function is used to fetch All notifications data for today


    public ArrayList<Notifications> GetNotificationsFromDatabase() {
        ArrayList<Notifications> NotificationDataList=new ArrayList<>();
        try {
            String TodayDate= GetCurrentDate();
            open(false);
            String selectQuery =  "SELECT * " + " FROM " + Constants.notification_table +" where "+Constants.noti_date+" Like '"+TodayDate+"%'";
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list

                try {
                    if (cursor.moveToFirst()) {
                        do {
                            String noti_description = cursor.getString(cursor.getColumnIndex(Constants.noti_description));
                            String noti_date=cursor.getString(cursor.getColumnIndex(Constants.noti_date));
                            Notifications notiobj=new Notifications(noti_description,noti_date);
                            NotificationDataList.add(notiobj);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
        return NotificationDataList;
    }

    //reading all the notification
    public int getPendingNotificationCount(){
        int count=0;
        try {
           open(false);
            String selectQuery =  "SELECT * " + " FROM " + Constants.notification_table +" where "+Constants.noti_status+"=0";
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count=cursor.getCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
        return count;
    }
    //update status for already read or not
    public void updateAllNotificationStatus() {
        try {
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.noti_status, 1);
                database.update(Constants.notification_table, values, Constants.noti_description + "!=''", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
    }



    /////////////crud persitance notification///////////
    public void updateAllPerNotificationStatus() {
        try {
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.per_noti_status, 1);
                database.update(Constants.persistance_notification_table, values, Constants.per_noti_description + "!=''", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
    }


    public void insertNewPerNotification(String desc) {
        try {
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.per_noti_description, desc);
                values.put(Constants.per_noti_status, 0);//o means not viewed
                database.insert(Constants.persistance_notification_table, null, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
    }


    public String GetLastPerNotificationsFromDatabase() {
        String per_noti_desc="";
        try {
            open(false);
            String selectQuery =  "SELECT * " + " FROM " + Constants.persistance_notification_table
                    +" where "+Constants.per_noti_status+" = 0 order by id desc LIMIT 1";
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list

                try {
                    if (cursor.moveToFirst()) {
                        do {
                            per_noti_desc = cursor.getString(cursor.getColumnIndex(Constants.per_noti_description));
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
        return per_noti_desc;
    }



    //update transfer record
    public void insertUpdatesCount(String Type) {
        try {
            int count=0;
           open(false);
            String selectQuery =  "SELECT * " + " FROM " + Constants.update_transfer_results_table
                    +" where "+Constants.update_transfer_results_type+"='"+Type+"' and "
                    +Constants.update_transfer_results_date+"='"+GetCurrentDate()+"'";
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    int rowcount=cursor.getCount();
                    if(rowcount>0) {
                        if (cursor.moveToFirst()) {
                            do {
                                count = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.update_transfer_results_count)));
                            } while (cursor.moveToNext());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
            int updatedcount=(count+1);
            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.update_transfer_results_count, updatedcount);
                values.put(Constants.update_transfer_results_date, GetCurrentDate());
                values.put(Constants.update_transfer_results_type, Type);//o means not viewed
                if(count==0)
                    database.insert(Constants.update_transfer_results_table, null, values);
                else {
                    database.update(Constants.update_transfer_results_table, values, Constants.update_transfer_results_type + "='" + Type + "'", null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
    }

    //reading all the updates count
    public int getUpdatesCount(String Type){
        deleteUpdateCount();
        int count=0;
        try {
           open(false);
            String selectQuery =  "SELECT * " + " FROM " + Constants.update_transfer_results_table
                    +" where "+Constants.update_transfer_results_type+"='"+Type+"' and "
                    +Constants.update_transfer_results_date+"='"+GetCurrentDate()+"'";
            try {
                Cursor cursor = null;
                try {
                    cursor = database.rawQuery(selectQuery, null);
                    // looping through all rows and adding to list
                    int rowcount=cursor.getCount();
                    if(rowcount>0) {
                        if (cursor.moveToFirst()) {
                            do {
                                count = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.update_transfer_results_count)));
                            } while (cursor.moveToNext());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(cursor!=null)
                    cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
        return count;
    }

    //delete uploaded events
    public void deleteUpdateCount() {
        try {
            open(true);
            try {
                database.delete(Constants.update_transfer_results_table, Constants.update_transfer_results_date + "!='" + GetCurrentDate() + "'", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (database != null && database.isOpen()) {
                ////database.close();
            }
        }catch(Exception e){
            Log.e(TAG, "error in database", e);
        }
    }

    /**
     * Check if there is any connectivity
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is fast connectivity
     *
     * @param context
     * @return
     */
    public static String getNetworkStatus(Context context) {
        String response="No NetWork Access";
        try {
            if (isConnected(context)) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = cm.getActiveNetworkInfo();

                if ((info != null && info.isConnected())) {
                    response= isConnectionFast(info.getType(),info.getSubtype());
                } else
                    response="No NetWork Access";
            } else {
                response="No NetWork Access";
            }
        }
        catch(Exception e){
            response="No NetWork Access";
        }
        return response;
    }


    //////basket crud////

    ///////////////////////////////   Trips Crud   /////////////////////////////////////
    public void insertBasketData(Long basket_server_id, Long basket_trip_id, String basket_trip_type,
                                 String basket_seller_name, String basket_seller_address,String basket_seller_zipcode,
                                 int basket_eqty, int basket_status) {
        // TODO Auto-generated method stub
        try {
            open(false);
            int count= 0;
            try {
                String selectQuery = "SELECT * " + " FROM " + Constants.basket_table +
                        " where " + Constants.basket_server_id + " = '" + basket_server_id + "'";
                count = 0;
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count = cursor.getCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.basket_server_id, basket_server_id);
                values.put(Constants.basket_trip_id, basket_trip_id);
                values.put(Constants.basket_trip_type, basket_trip_type);
                values.put(Constants.basket_seller_name, basket_seller_name);
                values.put(Constants.basket_seller_address, basket_seller_address);
                values.put(Constants.basket_seller_zipcode, basket_seller_zipcode);
                values.put(Constants.basket_eqty, basket_eqty);
                values.put(Constants.basket_status, basket_status);

                if (count == 0) {
                    values.put(Constants.basket_picked_qty, 0);
                    database.insert(Constants.basket_table, null, values);
                } else {
                    database.update(Constants.basket_table, values, Constants.basket_server_id + "=" + basket_server_id, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
    }


    //get basket count for particular trip
    public int GetBasketForParticulatTripsCount(int status,Long trip_id,String type){
        int count=500;
        try {
            open(false);
            String selectQuery ="";
            if(type.equals("All")) {
                selectQuery = "SELECT * " + " FROM " + Constants.basket_table + " where " + Constants.basket_trip_id +
                        " = '" + trip_id+"'";
            }
            else{
                selectQuery = "SELECT * " + " FROM " + Constants.basket_table + " where " + Constants.basket_trip_id +
                        " = '" + trip_id + "' and " + Constants.basket_status + " = " + status;
            }
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count = cursor.getCount();
                } catch (Exception e) {
                    count=500;
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                count=500;
                e.printStackTrace();
            }
        }
        catch (Exception e){
            count=500;
            Log.e(TAG, "error in database", e);
        }
        return count;
    }

    //get all basket list for a particular trip
    public ArrayList<Basket> GetBasketForParticulatTrips(int status,Long trip_id){
        ArrayList<Basket> AllBasketDataList = new ArrayList<>();
        try {
            open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.basket_table + " where " + Constants.basket_trip_id +
                        " = '" + trip_id + "' and " + Constants.basket_status + " = " + status;
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            Long basket_server_id = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.basket_server_id)));
                            Long basket_trip_id = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.basket_trip_id)));
                            int basket_eqty = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.basket_eqty)));
                            String basket_seller_name = cursor.getString(cursor.getColumnIndex(Constants.basket_seller_name));
                            String basket_seller_zipcode = cursor.getString(cursor.getColumnIndex(Constants.basket_seller_zipcode));
                            String basket_seller_address = cursor.getString(cursor.getColumnIndex(Constants.basket_seller_address));
                            int basket_status = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Constants.basket_status)));
                            String basket_trip_type = cursor.getString(cursor.getColumnIndex(Constants.basket_trip_type));
                            Basket basketobj = new Basket(basket_server_id,basket_trip_id,
                                    basket_trip_type,basket_seller_name,basket_seller_address,
                                    basket_seller_zipcode, basket_eqty,basket_status);
                            AllBasketDataList.add(basketobj);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
        return AllBasketDataList;
    }


///////////////////////hyper basket crud/////////
    public void insertHyperBasketData(String hyper_basket_ref_no, Long hyper_basket_trip_id) {
        try {
            open(false);
            int count= 0;
            try {
                String selectQuery = "SELECT * " + " FROM " + Constants.hyper_basket_table +
                        " where " + Constants.hyper_basket_ref_no + " = '" + hyper_basket_ref_no + "'";
                count = 0;
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count = cursor.getCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            open(true);
            try {
                ContentValues values = new ContentValues();
                values.put(Constants.hyper_basket_ref_no, hyper_basket_ref_no);
                values.put(Constants.hyper_basket_trip_id, hyper_basket_trip_id);
                if (count == 0) {
                    database.insert(Constants.hyper_basket_table, null, values);
                } else {
                    database.update(Constants.hyper_basket_table, values, Constants.hyper_basket_ref_no + "=" + hyper_basket_ref_no, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            Log.e(TAG, "error in database", e);
        }
    }


    //get hyper basket count for particular trip
    public int GetHyperBasketForParticulatTripsCount(int status,Long trip_id){
        int count=500;
        try {
            open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.hyper_basket_table
                                + " where " + Constants.hyper_basket_trip_id + " = '" + trip_id + "'";
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    count = cursor.getCount();
                } catch (Exception e) {
                    count=500;
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                count=500;
                e.printStackTrace();
            }
        }
        catch (Exception e){
            count=500;
            Log.e(TAG, "error in database", e);
        }
        return count;
    }

    //get all basket list for a particular trip
    public ArrayList<HyperBasket> GetHyperBasketForParticulatTrips(int status, Long trip_id){
        ArrayList<HyperBasket> AllHyperBasketDataList = new ArrayList<>();
        try {
            open(false);
            String selectQuery = "SELECT * " + " FROM " + Constants.hyper_basket_table
                    + " where " + Constants.hyper_basket_trip_id + " = '" + trip_id + "'";
            try {
                Cursor cursor = database.rawQuery(selectQuery, null);
                // looping through all rows and adding to list
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            String hyper_basket_ref_no = cursor.getString(cursor.getColumnIndex(Constants.hyper_basket_ref_no));
                            Long hyper_basket_trip_id = Long.valueOf(cursor.getString(cursor.getColumnIndex(Constants.hyper_basket_trip_id)));
                            String pickupDetails="";
                            String delDetails="";

                            int pendingTaskCount = GetTaskForParticulatTripsCount(Constants.task_status_pending_code, hyper_basket_ref_no,"HyperBasket");
                            if (pendingTaskCount == 0)
                               continue;


                            ArrayList<Task> taskData=GetAllHyperTaskForParticulatTrips(hyper_basket_ref_no);

                            for (Task taskobject: taskData) {
                                String CollectableAmount="0";
                                if(taskobject.taskType.equals("HYP-PICKUP")){
                                    String amount=taskobject.amount;
                                    String codamount=taskobject.codamount;
                                    if(amount==null || amount.equals("null"))
                                        amount="0";
                                    if(codamount==null || codamount.equals("null"))
                                        codamount="0";


                                    if(taskobject.payment_mode.equals("COD"))
                                        CollectableAmount="0";
                                    else if(taskobject.payment_mode.equals("COP"))
                                        CollectableAmount=amount;
                                    else if(taskobject.payment_mode.equals("PPD") || taskobject.payment_mode.equals("PSPD"))
                                        CollectableAmount=codamount;
                                    else
                                        CollectableAmount="0";
                                    pickupDetails +=taskobject.consignee_name+" \n"+taskobject.phone+" \n"+CollectableAmount+" Rs /-"+" \n"+taskobject.address+" \n";
                                }
                                else if(taskobject.taskType.equals("HYP-DELIVERY")){
                                    String amount=taskobject.amount;
                                    String codamount=taskobject.codamount;
                                    if(amount==null || amount.equals("null"))
                                        amount="0";
                                    if(codamount==null || codamount.equals("null"))
                                        codamount="0";

                                    if(taskobject.payment_mode.equals("COD"))
                                        CollectableAmount=String.valueOf(Integer.parseInt(amount)+Integer.parseInt(codamount));
                                    else if(taskobject.payment_mode.equals("COP"))
                                        CollectableAmount=codamount;
                                    else if(taskobject.payment_mode.equals("PPD") || taskobject.payment_mode.equals("PSPD"))
                                        CollectableAmount=codamount;
                                    else
                                        CollectableAmount="0";
                                    delDetails +=taskobject.consignee_name+" \n"+taskobject.phone+" \n"+CollectableAmount+" Rs /-"+" \n"+taskobject.address+" \n";
                                }
                            }

                            ArrayList<HyperBasketChild> hcarraylist=new ArrayList<HyperBasketChild>();
                            HyperBasketChild hcobj = new HyperBasketChild(pickupDetails,delDetails);
                            hcarraylist.add(hcobj);
                            HyperBasket hyperbasketobj = new HyperBasket(hyper_basket_ref_no,hyper_basket_trip_id,hcarraylist);
                            AllHyperBasketDataList.add(hyperbasketobj);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            Log.e(TAG, "error in database", e);
        }
        return AllHyperBasketDataList;
    }

    /**
     * Check if the connection is fast
     *
     * @param type
     * @param subType
     * @return
     */
    public static String isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            //System.out.println("CVW");
            return "CVW";//CONNECTED VIA WIFI
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return "NETWORK TYPE 1xRTT"; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return "3G 2 Mbps"; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:

                    return "2.75G 100-120 Kbps"; // ~
                // 50-100
                // kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return "NETWORK TYPE EVDO_0"; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return "NETWORK TYPE EVDO_A"; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return "2.5G  40-50 Kbps"; // ~ 100
                // kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return "4G  2-14 Mbps"; // ~ 2-14
                // Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return "4G 0.7-1.7 Mbps"; // ~
                // 700-1700
                // kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return "3G 1-23 Mbps"; // ~ 1-23
                // Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return "3G 0.4-7 Mbps"; // ~ 400-7000
                // kbps
                // NOT AVAILABLE YET IN API LEVEL 7
                case NETWORK_TYPE_EHRPD:
                    return "1 Mbps"; // ~ 1-2 Mbps
                case NETWORK_TYPE_EVDO_B:
                    return "5 Mbps"; // ~ 5 Mbps
                case NETWORK_TYPE_HSPAP:
                    return "4G 10-20 Mbps"; // ~ 10-20
                // Mbps
                case NETWORK_TYPE_IDEN:
                    return "IDEN"; // ~25 kbps
                case NETWORK_TYPE_LTE:
                    return "4G 10+ Mbps"; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    return "NTU";
                default:
                    return "";
            }
        } else {
            return "";
        }
    }

    //this method is used to get current datetime of system
    public String GetCurrentDAteTime(){
        Calendar cal1 = Calendar.getInstance(); // creates calendar
        cal1.setTime(new Date()); // sets calendar time/date
        Date b= cal1.getTime();
        SimpleDateFormat foramtter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String dateStart = foramtter.format(b);
        return dateStart;
    }

    //this method is used to get current system date
    public String GetCurrentDate(){
        Calendar cal1 = Calendar.getInstance(); // creates calendar
        cal1.setTime(new Date()); // sets calendar time/date
        Date b= cal1.getTime();
        SimpleDateFormat foramtter = new SimpleDateFormat("yyyy-MM-dd");
        String dateStart = foramtter.format(b);
        return dateStart;
    }

    //this method is used to get current datetime of system in long format
    public long getCurrentTime() {
        Time now = new Time();
        now.setToNow();
        return now.toMillis(false);
    }

}