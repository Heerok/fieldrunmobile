package com.humaralabs.fieldrun.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tscdll.TSCActivity;
import com.humaralabs.fieldrun.DeviceListActivity;
import com.humaralabs.fieldrun.MainActivity;
import com.humaralabs.fieldrun.R;
import com.humaralabs.fieldrun.TaskPrintActivity;

/**
 * Created by adl on 1/6/2016.
 */
public class BarcodeFragment extends Fragment {
    BluetoothAdapter mBluetoothAdapter = null;
    EditText product,quantity;
    String status="";
    Button connect,print;
    TSCActivity TscDll;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    String product_name, quantity_no = "1";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_barcode, container, false);
        product=(EditText)v.findViewById(R.id.productid);
        quantity=(EditText)v.findViewById(R.id.no_copy);
        print = (Button)v.findViewById(R.id.btn_print);
        connect = (Button)v.findViewById(R.id.btn_connect);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //tsc printer
        TscDll = new TSCActivity();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(MainActivity.mcontext, "Bluetooth is not available", Toast.LENGTH_LONG).show();

        }
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscovery();

            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                     product_name=product.getText().toString();
                     quantity_no=quantity.getText().toString();
                    if(product_name!=null&&!product_name.equals(""))
                        printbar();

                    else
                    {
                        Toast.makeText(MainActivity.mcontext,"Please enter product name.",Toast.LENGTH_LONG).show();
                    }
            }
        });
        return v;
    }

    @Override
    public void onPause() {
       // TscDll.closeport();
        super.onPause();
    }

    private void printbar() {
        {  if(TaskPrintActivity.address!=null&&!TaskPrintActivity.address.equals("")) {
            try {


                TscDll.openport(TaskPrintActivity.address);
                // TscDll.openport(address);
                TscDll.downloadpcx("UL.PCX");
                TscDll.downloadbmp("Triangle.bmp");
                TscDll.downloadttf("ARIAL.TTF");
                TscDll.setup(77, 53, 4, 4, 0, 0, 0);
                TscDll.clearbuffer();
                TscDll.sendcommand("SET TEAR ON\n");
                TscDll.sendcommand("SET COUNTER @1 1\n");
               // TscDll.sendcommand("@1 = \"0001\"\n");
                TscDll.sendcommand("TEXT 100,300,\"3\",0,1,1,@1\n");
                TscDll.sendcommand("PUTPCX 100,300,\"UL.PCX\"\n");
                //TscDll.sendcommand("PUTBMP 100,520,\"Triangle.bmp\"\n");
                TscDll.sendcommand("TEXT 100,760,\"ARIAL.TTF\",0,15,15,\"THIS IS ARIAL FONT\"\n");
                TscDll.barcode(100, 100, "128", 200, 1, 0, 3, 3, product_name);
                //TscDll.printerfont(100, 250, "4", 0, 1, 1, product_name);
                //Toast.makeText(this,status,Toast.LENGTH_LONG);
                TscDll.printlabel(1, Integer.parseInt(quantity_no));
                status = TscDll.status();
            }
            catch (Exception e)
            {}
            // TscDll.sendfile("zpl.txt");
            //TscDll.closeport();
        }
            else
        {
            Toast.makeText(MainActivity.mcontext,"Please connect to printer first.",Toast.LENGTH_LONG).show();
        }
        }

    }

    boolean bDiscoveryStarted = false;
    void startDiscovery() {
        if (bDiscoveryStarted)
            return;
        bDiscoveryStarted = true;
        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==REQUEST_CONNECT_DEVICE) {

            //addLog("onActivityResult: requestCode==REQUEST_CONNECT_DEVICE");
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                //addLog("resultCode==OK");
                // Get the device MAC address
                String addres = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                //addLog("onActivityResult: got device=" + address);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(addres);

                TscDll.openport(addres);
            }

            bDiscoveryStarted = false;

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        try
        {
            TscDll.closeport();
            TscDll=null;

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onStart() {
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
