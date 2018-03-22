package com.humaralabs.fieldrun;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.humaralabs.fieldrun.database.DbAdapter;
import com.humaralabs.fieldrun.database.TinyDB;
import com.humaralabs.fieldrun.fragments.AboutFragment;
import com.humaralabs.fieldrun.fragments.ActiveTripsFragment;
import com.humaralabs.fieldrun.fragments.AllTripsFragment;
import com.humaralabs.fieldrun.fragments.BarcodeFragment;
import com.humaralabs.fieldrun.fragments.ChangePasswordFragment;
import com.humaralabs.fieldrun.fragments.FeedbackFragment;
import com.humaralabs.fieldrun.fragments.PendingFragement;
import com.humaralabs.fieldrun.fragments.ProfileFragment;
import com.humaralabs.fieldrun.fragments.RateusFragment;
import com.humaralabs.fieldrun.fragments.StatsFragment;
import com.humaralabs.fieldrun.fragments.SyncStatsFragment;
import com.humaralabs.fieldrun.fragments.TodayStatsFragment;
import com.humaralabs.fieldrun.server.ServerInterface;

import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";
    public static Context mcontext;
    public static Boolean syncOrNot = false;
    FloatingActionButton refreshbtn;
    TinyDB tiny;
    DbAdapter db;
    //Defining Variables
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ProgressDialog Dialog = null;

    public static Boolean alreadyLoadingAllTripFragment=false;
    public static Boolean needToRefresh=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       /* Firebase.setAndroidContext(this);
        Firebase ref = new Firebase(Constants.FIREBASE_URL);
        JSONObject gcm = new JSONObject();
        //Adding values
        try {
            gcm.put("Lat","24.89");
            gcm.put("Lon","71.89");
            gcm.put("Bearing","0");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //Storing values to firebase
        ref.child("GCM").setValue(gcm.toString());*/
        alreadyLoadingAllTripFragment=false;
        syncOrNot = false;
        mcontext = MainActivity.this;
        db = new DbAdapter(MainActivity.this);
        tiny = new TinyDB(MainActivity.this);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //checking gps active or not
        GPSChecker.GPSCheck(MainActivity.this, false);

        //refresh btn
        refreshbtn = (FloatingActionButton) findViewById(R.id.refreshbtn);
        refreshbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(alreadyLoadingAllTripFragment==false) {
                    alreadyLoadingAllTripFragment=true;
                    syncOrNot = true;
                    initAllTrips();
                }
            }
        });

        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.AllTrips:
                        syncOrNot = false;
                        initAllTrips();
                        return true;
                    case R.id.ChangePassword:
                        toolbar.setTitle("Change Password");
                        ChangePasswordFragment changefragment = new ChangePasswordFragment();
                        android.support.v4.app.FragmentTransaction ChangefragmentTransaction = getSupportFragmentManager().beginTransaction();
                        ChangefragmentTransaction.replace(R.id.frame, changefragment);
                        ChangefragmentTransaction.commit();
                        return true;
                    case R.id.ActiveTrips:
                        refreshbtn.setVisibility(View.VISIBLE);
                        toolbar.setTitle("Active Trip");
                        ActiveTripsFragment activefragment = new ActiveTripsFragment();
                        android.support.v4.app.FragmentTransaction ActivefragmentTransaction = getSupportFragmentManager().beginTransaction();
                        ActivefragmentTransaction.replace(R.id.frame, activefragment);
                        ActivefragmentTransaction.commit();
                        return true;
                    case R.id.TodayStats:
                        refreshbtn.setVisibility(View.GONE);
                        toolbar.setTitle("Today Stats");
                        TodayStatsFragment TodayStatsFragment = new TodayStatsFragment();
                        android.support.v4.app.FragmentTransaction TodayStatsFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        TodayStatsFragmentTransaction.replace(R.id.frame, TodayStatsFragment);
                        TodayStatsFragmentTransaction.commit();
                        return true;
                    case R.id.Stats:
                        refreshbtn.setVisibility(View.GONE);
                        toolbar.setTitle("Stats");
                        StatsFragment StatsFragment = new StatsFragment();
                        android.support.v4.app.FragmentTransaction StatsFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        StatsFragmentTransaction.replace(R.id.frame, StatsFragment);
                        StatsFragmentTransaction.commit();
                        return true;
                    case R.id.SyncStats:
                        refreshbtn.setVisibility(View.GONE);
                        toolbar.setTitle("Stats");
                        SyncStatsFragment SyncStatsFragment = new SyncStatsFragment();
                        android.support.v4.app.FragmentTransaction SyncStatsFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        SyncStatsFragmentTransaction.replace(R.id.frame, SyncStatsFragment);
                        SyncStatsFragmentTransaction.commit();
                        return true;
                    case R.id.MyProfile:
                        refreshbtn.setVisibility(View.GONE);
                        toolbar.setTitle("My Profile");
                        ProfileFragment ProfileFragment = new ProfileFragment();
                        android.support.v4.app.FragmentTransaction ProfileFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        ProfileFragmentTransaction.replace(R.id.frame, ProfileFragment);
                        ProfileFragmentTransaction.commit();
                        return true;
                    case R.id.About:
                        refreshbtn.setVisibility(View.GONE);
                        toolbar.setTitle("About Us");
                        AboutFragment AboutFragment = new AboutFragment();
                        android.support.v4.app.FragmentTransaction AboutFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        AboutFragmentTransaction.replace(R.id.frame, AboutFragment);
                        AboutFragmentTransaction.commit();
                        return true;
                    case R.id.SendFeedback:
                        refreshbtn.setVisibility(View.GONE);
                        toolbar.setTitle("Send Feedback");
                        FeedbackFragment FeedbackFragment = new FeedbackFragment();
                        android.support.v4.app.FragmentTransaction FeedbackFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        FeedbackFragmentTransaction.replace(R.id.frame, FeedbackFragment);
                        FeedbackFragmentTransaction.commit();
                        return true;
                    case R.id.Rateus:
                        refreshbtn.setVisibility(View.GONE);
                        toolbar.setTitle("Rate Us");
                        RateusFragment RateusFragment = new RateusFragment();
                        android.support.v4.app.FragmentTransaction RateusFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        RateusFragmentTransaction.replace(R.id.frame, RateusFragment);
                        RateusFragmentTransaction.commit();
                        return true;
                    case R.id.barcode:
                        refreshbtn.setVisibility(View.GONE);
                        toolbar.setTitle("Barcode Printer");
                        BarcodeFragment BarcodeFragment = new BarcodeFragment();
                        android.support.v4.app.FragmentTransaction BarcodeFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        BarcodeFragmentTransaction.replace(R.id.frame, BarcodeFragment);
                        BarcodeFragmentTransaction.commit();
                        return true;
                    case R.id.pendingeventmenu:
                        refreshbtn.setVisibility(View.GONE);
                        toolbar.setTitle("Pending Event");
                        PendingFragement  pendingFragement= new PendingFragement();
                        android.support.v4.app.FragmentTransaction PendingFragementTransaction = getSupportFragmentManager().beginTransaction();
                        PendingFragementTransaction.replace(R.id.frame, pendingFragement);
                        PendingFragementTransaction.commit();
                        return true;
                    case R.id.Signout:
                        new AlertDialog.Builder(MainActivity.this)
                                .setIcon(R.drawable.sign_out)
                                .setTitle("Log out")
                                .setMessage("Are you sure you want to LogOut from this app?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new SendTripEventsToServer().execute();//send previous events data if pending
                                    }

                                })
                                .setNegativeButton("No", null)
                                .show();
                        return true;
                    default:
                        initAllTrips();
                        return true;
                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        //initialize all trip fragment
        if(!tiny.getString("GcmTripMessage").equals("YES")) {
            initAllTrips();
        }

        //start service
        CommonFunctions.StartSevice(MainActivity.this);
    }


    private void Logout() {
        tiny.putString("loginToken", "");
        tiny.putString("gcmtoken", "");
        db.deletePreviousData();
        finish();
    }


    //initialize all trips fragment
    private void initAllTrips() {
        try {
            refreshbtn.setVisibility(View.VISIBLE);
            toolbar.setTitle("All Trips");
            AllTripsFragment allfragment = new AllTripsFragment();
            android.support.v4.app.FragmentTransaction AllfragmentTransaction = getSupportFragmentManager().beginTransaction();
            AllfragmentTransaction.replace(R.id.frame, allfragment);
            AllfragmentTransaction.commitAllowingStateLoss();
            AllfragmentTransaction.commit();
        } catch (IllegalStateException ignored) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        } catch (Exception ignored) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        }

    }

    //refreshing notification count
    private void refreshNotificationIcon() {
        RelativeLayout notiClick = (RelativeLayout) findViewById(R.id.notification);
        TextView noofnotil = (TextView) findViewById(R.id.noofnotil);
        int pendingNotiCount = db.getPendingNotificationCount();
        if (pendingNotiCount == 0) {
            noofnotil.setVisibility(View.GONE);
        } else {
            noofnotil.setVisibility(View.VISIBLE);
            noofnotil.setText(String.valueOf(pendingNotiCount));
        }
        notiClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Notification", Toast.LENGTH_LONG).show();
                db.updateAllNotificationStatus();
                Intent i = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });
    }

    //refresh user details in navigation drawer
    public void refreshUserProfileInfo() {
        try {
            //set username
            TextView username = (TextView) findViewById(R.id.username);
            String uname = tiny.getString("firstName") +" "+tiny.getString("lastName");
            if (uname.equals("")) {
                username.setText("User");
            } else {
                username.setText(uname);
            }
            //setting user profile pic
            String bitmap = tiny.getString("ProfileImageBitmap");
            if (!bitmap.equals("")) {
                ImageView profilepic = (ImageView) findViewById(R.id.profilepic);
                profilepic.setImageBitmap(CommonFunctions.StringToBitMap(bitmap));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.outerlogo)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to close this app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //start service
        CommonFunctions.StartSevice(MainActivity.this);
        GPSChecker.GPSCheck(MainActivity.this, false);
        /*syncOrNot = false;
        if(AllTripsFragment.alreadyServerCalling == false) {
            if (!(toolbar.getTitle().equals("My Profile")) && !(toolbar.getTitle().equals("Barcode Printer"))) {
                initAllTrips();
            }
        }*/
        db.updateAllPerNotificationStatus();
        if(needToRefresh)
        {
            syncOrNot=false;
            needToRefresh=false;
            initAllTrips();
        }
        if(alreadyLoadingAllTripFragment==false) {
            if(tiny.getString("GcmTripMessage").equals("YES")){//it means new trip added and gcm recieved
                syncOrNot=true;
                alreadyLoadingAllTripFragment=true;
                initAllTrips();
            }
        }

        refreshNotificationIcon();
        refreshUserProfileInfo();
    }

    //Async class to send field events data to server
    private class SendTripEventsToServer extends AsyncTask<Void, Void, Void> {
        long activetripid = 0;
        int requestId = 0;
        String Result = "NoEvents";

        protected void onPreExecute() {
            Dialog = new ProgressDialog(MainActivity.mcontext);
            Dialog.setMessage("Signing out. Please wait...");
            Dialog.show();
            Dialog.setCanceledOnTouchOutside(false);
            Dialog.setCancelable(false);
        }

        protected void onPostExecute(Void unused) {
            if (Dialog != null && Dialog.isShowing()) {
                Dialog.dismiss();
                if (activetripid == 0) {
                    new callserver_async().execute();
                    //Logout();
                } else if (activetripid != 0) {
                    if (Result.equals("NoEvents")) {
                        new callserver_async().execute();
                        //Logout();
                    } else if (ServerInterface.checkserver && !Result.equals("")) {
                        try {
                            JSONObject mydata = new JSONObject(Result);
                            Log.d(TAG, "bulk sync response" + mydata.toString());
                            String res = mydata.getString("Result");
                            Boolean status = mydata.getBoolean("status");
                            if (status && res.equals("OK")) {
                                JSONObject record = mydata.getJSONObject("Record");
                                String respid = record.getString("reqid");
                                //deleting updated record
                                db.deleteUpdatedEvents(Integer.parseInt(respid), "NOTALL");
                                new callserver_async().execute();
                                //Logout();
                            } else {
                                if (requestId != 0)
                                    db.updateEventStatus(Constants.event_status_pending_code, requestId);
                                CommonFunctions._messageToShow = "Something went wrong. Please try again later.";
                                CommonFunctions.sendMessageToActivity(1, MainActivity.this);
                            }
                        } catch (Exception e) {
                            if (requestId != 0)
                                db.updateEventStatus(Constants.event_status_pending_code, requestId);
                            CommonFunctions._messageToShow = "Something went wrong. Please try again later.";
                            CommonFunctions.sendMessageToActivity(1, MainActivity.this);
                            e.printStackTrace();
                        }
                    } else {
                        if (requestId != 0)
                            db.updateEventStatus(Constants.event_status_pending_code, requestId);
                        CommonFunctions._messageToShow = "Something went wrong. Please try again later.";
                        CommonFunctions.sendMessageToActivity(1, MainActivity.this);
                    }
                }
            }
        }

        protected Void doInBackground(Void... par) {
            try {
                activetripid = db.getActiveTripId();
                if (activetripid != 0) {
                    String url = Constants.ServerApiUrl + "mobile/TaskEventUpdate";
                    if (tiny == null)
                        tiny = new TinyDB(MainActivity.mcontext);
                    String loginToken = tiny.getString("loginToken");
                    JSONObject jo = db.getPendingEvents(100, loginToken, "ALLTASK");
                    JSONArray eventsUpdateArray = jo.getJSONArray("updates");
                    if (eventsUpdateArray.length() > 0) {
                        requestId = Integer.parseInt(jo.getString("reqId"));
                        String iemino = tiny.getString("iemino");
                        Result = ServerInterface.CallServerApi(jo, url, 180,tiny.getString("loginToken"),iemino);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }//end

    private class callserver_async extends AsyncTask<Void, Void, Void>
    {
        String Result="";
        protected void onPreExecute()
        {
            Dialog = new ProgressDialog(MainActivity.mcontext);
            Dialog.setMessage("Signing out. Please wait...");
            Dialog.show();
            Dialog.setCanceledOnTouchOutside(false);
            Dialog.setCancelable(false);
        }
        protected void onPostExecute(Void unused)
        {
            if(Dialog != null && Dialog.isShowing())
            {
                if (ServerInterface.checkserver) {
                    try {
                        JSONObject jobj=new JSONObject(Result);
                        Boolean res=jobj.getBoolean("status");
                        if(res) {
                           Logout();
                        }
                        else{
                            String msg=jobj.getString("Message");
                            CommonFunctions._messageToShow =msg;
                            CommonFunctions.sendMessageToActivity(1, MainActivity.this);
                        }
                    } catch (Exception e) {
                        CommonFunctions._messageToShow = "Invalid Response!! Please Try After Some Time.";
                        CommonFunctions.sendMessageToActivity(1, MainActivity.this);
                        e.printStackTrace();
                    }
                } else {
                    CommonFunctions._messageToShow = "Network Error!! Please Try After Some Time.";
                    CommonFunctions.sendMessageToActivity(1, MainActivity.this);
                }
                Dialog.dismiss();
            }
        }
        protected Void doInBackground(Void... par)
        {
            String uri = Constants.ServerApiUrl + "tokenlogout";

            JSONObject params = new JSONObject();
            try {
                params.put("token", tiny.getString("loginToken"));
                String iemino = tiny.getString("iemino");
                Result=ServerInterface.CallServerApi(params, uri, 55, tiny.getString("loginToken"),iemino);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }//end get trips class

}
