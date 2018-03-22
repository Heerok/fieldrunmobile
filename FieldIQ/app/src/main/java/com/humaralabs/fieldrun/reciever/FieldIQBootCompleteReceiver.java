package com.humaralabs.fieldrun.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.humaralabs.fieldrun.CommonFunctions;

/**
 * Created by pc1 on 17-10-2015.
 */
public class FieldIQBootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //start service
        CommonFunctions.StartSevice(context);

    }
}
