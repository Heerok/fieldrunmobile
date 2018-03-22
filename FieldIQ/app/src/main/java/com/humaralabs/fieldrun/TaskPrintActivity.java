package com.humaralabs.fieldrun;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tscdll.TSCActivity;

public class TaskPrintActivity extends AppCompatActivity {

    TSCActivity TscDll;
    Button scan, connect, print;
    EditText order, quantity;
    BluetoothAdapter mBluetoothAdapter = null;
    //btPrintFile btPrintService = null;
    public static String address;
    public static boolean deviceConnected=false;
    public  boolean connected=false;
    String status="";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    String orderno,quantityno;
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_print);
            orderno=TaskDetailActivity.orderid;

        //button
        scan = (Button) findViewById(R.id.search);
        print = (Button) findViewById(R.id.task_button_print);
        connect = (Button) findViewById(R.id.connect);
        //Edittext
        order = (EditText) findViewById(R.id.orderno);
            quantity = (EditText) findViewById(R.id.qnty);
        order.setText(orderno);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //tsc printer
        TscDll = new TSCActivity();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        //scan button
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connected)
                {
                    Toast.makeText(MainActivity.mcontext,"Already Connected!",Toast.LENGTH_LONG).show();
                }
                else {
                    startDiscovery();
                }
            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderno=order.getText().toString();
                quantityno=quantity.getText().toString();
                if(orderno!=null&&!orderno.equals("")&&quantityno!=null)
                {
                    printbar();
                }
                else
                {
                    Toast.makeText(MainActivity.mcontext,"Order should be blank.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        try
        {   connected=false;
            TscDll.closeport();
            TscDll=null;

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        finish();

    }

    private void printbar() {
        if(address!=null&&!address.equals("")) {
            if(!connected) {

                TscDll.openport(address);
            }
                TscDll.downloadpcx("UL.PCX");
                //TscDll.downloadbmp("Triangle.bmp");
                TscDll.downloadttf("ARIAL.TTF");
                try {

                        TscDll.setup(77, 53, 4, 4, 0, 0, 0);
                        TscDll.clearbuffer();


                        TscDll.sendcommand("SET TEAR ON\n");
                        TscDll.sendcommand("SET COUNTER @1 1\n");
                       // TscDll.sendcommand("@1 = \"0001\"\n");
                        //TscDll.sendcommand("TEXT 100,300,\"3\",0,1,1,@1\n");
                        TscDll.sendcommand("PUTPCX 100,300,\"UL.PCX\"\n");
                        //TscDll.sendcommand("PUTBMP 100,520,\"Triangle.bmp\"\n");
                        TscDll.sendcommand("TEXT 100,760,\"ARIAL.TTF\",0,15,15,\"THIS IS ARIAL FONT\"\n");

                        TscDll.barcode(100, 100, "128", 200, 1, 0, 3, 3, orderno);
                        //TscDll.printerfont(100, 250, "4", 0, 1, 1, orderno);

                        TscDll.printlabel(1, Integer.parseInt(quantityno));
                        //TscDll.closeport();
                        //status = TscDll.status();
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }

            // TscDll.sendfile("zpl.txt");
            //TscDll.closeport();

        else
        {
            Toast.makeText(MainActivity.mcontext,"Please connect to printer first.",Toast.LENGTH_LONG).show();
        }


    }


    @Override
    public void onBackPressed() {
        if(mBluetoothAdapter.isEnabled())
        {

            try
            {   connected=false;
                TscDll.closeport();
                TscDll=null;

            }catch (Exception e)
            {
                e.printStackTrace();
            }
            mBluetoothAdapter.disable();
        }
                super.onBackPressed();
    }

    boolean bDiscoveryStarted = false;
    void startDiscovery() {
        if (bDiscoveryStarted)
            return;
        bDiscoveryStarted = true;
        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==REQUEST_CONNECT_DEVICE)
        {

            //addLog("onActivityResult: requestCode==REQUEST_CONNECT_DEVICE");
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                //addLog("resultCode==OK");

                // Get the device MAC address
                String addres = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                //addLog("onActivityResult: got device=" + address);

                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(addres);
                order.setText(orderno);


                if(addres!=null) {
                    TscDll.openport(address);
                    connected = true;
                }

                if(deviceConnected||addres!=null) {
                    scan.setText("Connected");
                    print.setVisibility(View.VISIBLE);
                }
                else {
                    scan.setText("Connect");
                    print.setVisibility(View.GONE);
                }
            }

            bDiscoveryStarted = false;

        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    protected void onStart() {
        if (mBluetoothAdapter != null) {
            // If BT is not on, request that it be enabled.
            // setupChat() will then be called during onActivityResult
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the comm session
            }
        }
        super.onStart();
    }
}