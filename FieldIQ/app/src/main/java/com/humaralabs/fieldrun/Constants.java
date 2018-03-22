package com.humaralabs.fieldrun;

public class Constants {
    public static String ServerUrl ="http://fieldrundev.ap-south-1.elasticbeanstalk.com";//"http://fieldrun.humaralabs.com";//"http://192.169.3.113:8080";//;


    public static String ServerApiUrl = ServerUrl + "/api/";

    public static String merataskurl="http://meratask.fieldrun.in";
    public static String streetwiseurl="http://streetwise.fieldrun.in";
    public static String bulkvanurl="http://bulkvan.fieldrun.in";
    public static String daakuaturl="http://daakuat.fieldrun.in";
    public static String claexpressurl="http://claexpress.fieldrun.in";
    public static String deliverysolutionsurl="http://deliverysolutions.fieldrun.in";
    public static String bvclogistic="http://bvclogistics.fieldrun.in";
    public static String taskmasterurl  ="http://taskmasters.fieldrun.in";

    public static String loginToken = "";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static int trip_status_pending_code = 0;
    public static int trip_status_active_code = 1;
    public static int trip_status_complete_code = 2;


    public static int basket_status_pending_code = 0;
    public static int basket_status_complete_code = 1;
    public static int basket_status_failed_code = 2;


    public static int task_status_pending_code = 0;
    public static int task_status_done_code = 1;
    public static int task_status_failed_code = 2;
    public static int task_status_start_code=3;
    public static int task_status_doorstep_code=4;
    public static int task_status_qc_fail_code=5;

    public static int event_status_pending_code = 0;
    public static int event_status_done_code = 1;

    public static final String KEY_ID = "id";

    //dispostion data table constant
    public static String disposition_table = "disposition_table";
    public static final String disposition_name = "disposition_name";
    public static final String disposition_value = "disposition_value";
    public static final String disposition_type = "disposition_type";
    public static final String disposition_actiontype = "disposition_actiontype";

    // Question set table constent
    public static String question_table = "question_table";
    public static String questionId = "questionId";
    public static String plateform_id = "plateform_id";
    public static String question= "question";
    public static String answer_type = "answer_type";
    public static String noEffect = "noEffect";

    //trip data table constant
    public static String trip_table = "trip_table";
    public static final String trip_id = "trip_id";
    public static final String trip_date = "trip_date";
    public static String trip_expiryDateTime = "trip_expiryDateTime";
    public static final String trip_zipcode = "trip_zipcode";
    public static final String trip_num_task = "trip_num_task";
    public static final String trip_origin = "trip_origin";
    public static final String trip_facility = "trip_facility";
    public static final String trip_type = "trip_type";
    public static final String trip_status = "trip_status";


    //task data table constant
    public static String task_table = "task_table";
    public static final String task_tripid = "task_tripid";
    public static final String task_id = "task_id";
    public static final String task_ref = "task_ref";
    public static final String platformId = "platformId";
    public static final String pickups = "pickups";
    public static final String name = "name";
    public static final String address = "address";
    public static final String zipCode = "zipCode";
    public static final String phone = "phone";
    public static final String task_type = "task_type";
    public static final String pickup_qty = "pickup_qty";
    public static final String reason = "reason";
    public static final String delievery_date_time = "delievery_date_time";
    public static final String comments = "comments";
    public static final String payment_mode="payment_mode";
    public static final String basket_id="basket_id";
    public static final String consignee_number="consignee_number";
    public static final String consignee_name="consignee_name";
    public static final String task_pinno="task_pinno";
    public static final String task_amount="task_amount";
    public static final String task_codamount="task_codamount";
    public static final String task_retry_count="task_retry_count";
    public static final String itemCategory="itemCategory";
    public static final String itemDescription="itemDescription";
    public static final String mandatoryPhotocount="mandatoryPhotocount";
    public static final String optionPhotocount="optionPhotocount";
    public static final String task_status = "task_status";//0 means pending 1 means done 2 means failed


    //field event table
    public static String event_table = "event_table";
    public static String event_tripId = "event_tripId";
    public static String event_type = "event_type";
    public static String latitude = "latitude";
    public static String longitude = "longitude";
    public static String accuracy = "accuracy";
    public static String altitude = "altitude";
    public static String bearing = "bearing";
    public static String speed = "speed";
    public static String battery = "battery";
    public static String signal_status = "signal_status";
    public static String ts = "ts";
    public static String event_status = "event_status";
    public static String params = "params";
    public static String event_date = "event_date";
    public static String event_tripExpiryDateTime = "event_tripExpiryDateTime";
    public static String reqId = "reqId";

    //notification table
    public static String notification_table = "notification_table";
    public static String noti_description = "noti_description";
    public static String noti_date = "noti_date";
    public static String noti_status = "noti_status";

    //notification table
    public static String persistance_notification_table = "persistance_notification_table";
    public static String per_noti_description = "per_noti_description";
    public static String per_noti_status = "per_noti_status";


    //notification table
    public static String update_transfer_results_table = "update_transfer_results_table";
    public static String update_transfer_results_count = "update_transfer_results_count";
    public static String update_transfer_results_type = "update_transfer_results_type";
    public static String update_transfer_results_date = "update_transfer_results_date";

    //basket table
    public static String basket_table = "basket_table";
    public static String basket_server_id = "basket_server_id";
    public static String basket_trip_id = "basket_trip_id";
    public static String basket_trip_type="basket_trip_type";
    public static String basket_eqty = "basket_eqty";
    public static String basket_seller_name = "basket_seller_name";
    public static String basket_seller_zipcode = "basket_seller_zipcode";
    public static String basket_seller_address = "basket_seller_address";
    public static String basket_picked_qty = "basket_picked_qty";
    public static String basket_status = "basket_status";

    public static String hyper_basket_table="hyper_basket_table";
    public static String hyper_basket_ref_no="hyper_basket_ref_no";
    public static String hyper_basket_trip_id="hyper_basket_trip_id";

    public static final String FIREBASE_URL = "https://mytestproject-e7bad.firebaseio.com/";

}
