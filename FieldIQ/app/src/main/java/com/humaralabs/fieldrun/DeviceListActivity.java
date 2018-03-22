package com.humaralabs.fieldrun;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by adl on 1/5/2016.
 */
public class DeviceListActivity extends Activity {
    // Debugging
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    Button scanButton;
    String scanButtonTextScan = "Scan";
    String scanButtonTextStop = "STOP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //addLog("+++OnCreate+++");
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBtAdapter.cancelDiscovery();

                // Get the device MAC address, which is the last 17 chars in the View
                String info = ((TextView) view).getText().toString();
                TaskPrintActivity.address = info.substring(info.length() - 17);

                // Create the result Intent and include the MAC address
               Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, TaskPrintActivity.address);

                //addLog("setResult=OK");
                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                finish();

            }
        });

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBtAdapter.cancelDiscovery();

                // Get the device MAC address, which is the last 17 chars in the View
                String info = ((TextView) view).getText().toString();
                TaskPrintActivity.address = info.substring(info.length() - 17);

                // Create the result Intent and include the MAC address
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, TaskPrintActivity.address);

                //addLog("setResult=OK");
                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                finish();

            }
        });

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        //register trhe broadcast when bluetooth is connected
        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.registerReceiver(mReceiver, filter);

        //register trhe broadcast when bluetooth is disconnected
        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        initDeviceList();

        // Initialize the button to perform device discovery
        scanButtonTextScan = getResources().getString(R.string.devicelist_action_button_text_scan);
        scanButtonTextStop = getResources().getString(R.string.devicelist_action_button_text_cancel);
        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setText(scanButtonTextScan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                //v.setVisibility(View.GONE);
            }
        });


        // addLog("+++OnCreate+++ DONE");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //addLog("+++OnDestroy+++");

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    void initDeviceList() {
        //empty list first?
        if (mPairedDevicesArrayAdapter.getCount() > 0)
            mPairedDevicesArrayAdapter.clear();
        if (mNewDevicesArrayAdapter.getCount() > 0)
            mNewDevicesArrayAdapter.clear();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        //addLog("doDiscovery()");
        initDeviceList();

        if (scanButton.getText().toString().equals(scanButtonTextStop)) {
            if (mBtAdapter != null) {
                if (mBtAdapter.isDiscovering()) {
                   // addLog("stop discovery requested");
                    mBtAdapter.cancelDiscovery();
                    scanButton.setText(scanButtonTextScan);
                    return;
                }
            }
        }
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle("scaning...");

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
        scanButton.setText("STOP");
        //addLog("Discovery() started");
    }

    // The on-click listener for all devices in the ListViews
    private AdapterViewCompat.OnItemClickListener mDeviceClickListener = new AdapterViewCompat.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterViewCompat<?> parent, View view, int position, long id) {
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            //addLog("setResult=OK");
            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();

        }


    };



    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //addLog("Discovery BroadcastReceiver()");
            //check device is connected or not
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equalsIgnoreCase( action ))
            {
                TaskPrintActivity.deviceConnected=true;
            }
            //check device is connected or not
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equalsIgnoreCase(action))
            {
                TaskPrintActivity.deviceConnected=false;
            }
            // When discovery finds a device
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // addLog("ACTION_FOUND");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // addLog("adding new bonded device: " + device.getName() + " " + device.getAddress());
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle("select_device");
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
                scanButton.setText(scanButtonTextScan);
            }
        }
    };
}