package com.humaralabs.fieldrun.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.humaralabs.fieldrun.Constants;


public class DBHelper  extends SQLiteOpenHelper {
    //version number to upgrade database version
    private static final int DATABASE_VERSION = 65;

    // Database Name
    private static final String DATABASE_NAME = "fieldiqdb";

    public DBHelper(Context context ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //create Notification table
        String CREATE_TABLE_NOTIFICATION = "CREATE TABLE " + Constants.notification_table  + "("
                + Constants.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Constants.noti_description + " TEXT, "
                + Constants.noti_date + " TEXT,"
                + Constants.noti_status + " INTEGER)";
        db.execSQL(CREATE_TABLE_NOTIFICATION);

        //create per Notification table
        String CREATE_TABLE_PERSISTANCE_NOTIFICATION = "CREATE TABLE " + Constants.persistance_notification_table  + "("
                + Constants.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Constants.per_noti_description + " TEXT, "
                + Constants.per_noti_status + " INTEGER)";
        db.execSQL(CREATE_TABLE_PERSISTANCE_NOTIFICATION);


        //create disposition table
        String CREATE_TABLE_DISPOSITION = "CREATE TABLE " + Constants.disposition_table  + "("
                + Constants.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Constants.disposition_name + " TEXT, "
                + Constants.disposition_value + " TEXT,"
                + Constants.disposition_type + " TEXT,"
                + Constants.disposition_actiontype + " TEXT)";
        db.execSQL(CREATE_TABLE_DISPOSITION);

        //create questionSet table
        String CREATE_TABLE_QESTIONSET = "CREATE TABLE " + Constants.question_table  + "("
                + Constants.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Constants.questionId + " TEXT, "
                + Constants.plateform_id + " TEXT, "
                + Constants.itemCategory + " TEXT, "
                + Constants.question + " TEXT,"
                + Constants.noEffect + " TEXT,"
                + Constants.answer_type + " TEXT)";
        db.execSQL(CREATE_TABLE_QESTIONSET);

        //create trip table
        String CREATE_TABLE_TRIP = "CREATE TABLE " + Constants.trip_table  + "("
                + Constants.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Constants.trip_id + " INTEGER, "
                + Constants.trip_date + " TEXT, "
                + Constants.trip_expiryDateTime + " TEXT, "
                + Constants.trip_num_task + " TEXT,"
                + Constants.trip_zipcode + " TEXT,"
                + Constants.trip_origin + " TEXT, "
                + Constants.trip_facility + " TEXT, "
                + Constants.trip_type + " TEXT, "
                + Constants.trip_status + " INTEGER)"; //0 means pending 1 means Active 2 means Complete
        db.execSQL(CREATE_TABLE_TRIP);

        //create task table
        String CREATE_TABLE_TASK = "CREATE TABLE " + Constants.task_table  + "("
                + Constants.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Constants.task_id + " INTEGER, "
                + Constants.task_tripid + " INTEGER, "
                + Constants.task_ref + " TEXT,"
                + Constants.platformId + " TEXT,"
                + Constants.itemCategory + " TEXT,"
                + Constants.itemDescription + " TEXT,"
                + Constants.pickups + " INTEGER,"
                + Constants.name + " TEXT, "
                + Constants.address + " TEXT, "
                + Constants.zipCode + " TEXT, "
                + Constants.phone + " TEXT,"
                + Constants.task_type + " INTEGER,"
                + Constants.pickup_qty + " INTEGER, "
                + Constants.reason + " TEXT, "
                + Constants.delievery_date_time + " TEXT, "
                + Constants.comments + " TEXT,"
                + Constants.payment_mode + " TEXT,"
                + Constants.basket_id + " TEXT,"
                + Constants.consignee_number + " TEXT,"
                + Constants.consignee_name + " TEXT,"
                + Constants.task_pinno + " TEXT,"
                + Constants.task_amount + " TEXT,"
                + Constants.mandatoryPhotocount + " TEXT,"
                + Constants.optionPhotocount + " TEXT,"
                + Constants.task_codamount + " TEXT,"
                + Constants.task_retry_count + " INTEGER,"
                + Constants.task_status + " INTEGER)"; //0 means pending 1 means Active 2 means Complete
        db.execSQL(CREATE_TABLE_TASK);

        //create trip table
        String CREATE_TABLE_EVENTS = "CREATE TABLE " + Constants.event_table  + "("
                + Constants.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Constants.event_tripId + " INTEGER, "
                + Constants.event_type + " TEXT, "
                + Constants.latitude + " TEXT,"
                + Constants.longitude + " TEXT,"
                + Constants.altitude + " TEXT, "
                + Constants.accuracy + " TEXT, "
                + Constants.bearing + " TEXT, "
                + Constants.speed + " TEXT,"
                + Constants.battery + " TEXT,"
                + Constants.signal_status + " TEXT,"
                + Constants.ts + " TEXT, "
                + Constants.reqId + " TEXT, "
                + Constants.params + " TEXT, "
                + Constants.event_date + " TEXT, "
                + Constants.event_tripExpiryDateTime + " TEXT, "
                + Constants.event_status + " INTEGER)"; //0 means pending 1 means updated
        db.execSQL(CREATE_TABLE_EVENTS);

        //create basket table
        String CREATE_TABLE_BASKET = "CREATE TABLE " + Constants.basket_table  + "("
                + Constants.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Constants.basket_server_id + " INTEGER, "
                + Constants.basket_trip_id + " INTEGER, "
                + Constants.basket_trip_type + " TEXT, "
                + Constants.basket_seller_name + " TEXT,"
                + Constants.basket_seller_address + " TEXT,"
                + Constants.basket_seller_zipcode + " TEXT, "
                + Constants.basket_eqty + " INTEGER, "
                + Constants.basket_picked_qty + " INTEGER, "
                + Constants.basket_status + " INTEGER)"; //0 means pending 1 means updated
        db.execSQL(CREATE_TABLE_BASKET);

        //create Hyper basket table
        String CREATE_TABLE_HYPER_BASKET = "CREATE TABLE " + Constants.hyper_basket_table  + "("
                + Constants.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Constants.hyper_basket_ref_no + " INTEGER, "
                + Constants.hyper_basket_trip_id + " INTEGER)"; //0 means pending 1 means updated
        db.execSQL(CREATE_TABLE_HYPER_BASKET);


        //create trip table
        String CREATE_TABLE_UPDATE_FAILED = "CREATE TABLE " + Constants.update_transfer_results_table  + "("
                + Constants.update_transfer_results_count + " TEXT, "
                + Constants.update_transfer_results_type + " TEXT,"
                + Constants.update_transfer_results_date+ " TEXT)"; //0 means pending 1 means Active 2 means Complete
        db.execSQL(CREATE_TABLE_UPDATE_FAILED);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed, all data will be gone
        db.execSQL("DROP TABLE IF EXISTS " + Constants.notification_table);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.disposition_table);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.trip_table);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.task_table);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.event_table);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.update_transfer_results_table);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.basket_table);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.hyper_basket_table);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.persistance_notification_table);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.question_table);
        // Create tables again
        onCreate(db);
    }

}
