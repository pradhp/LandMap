package com.pearnode.app.placero.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.pearnode.app.placero.custom.GlobalContext;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connected = isConnected(context);
        // For testing
        //connected = false;
        if(!connected){
            context.sendBroadcast(new Intent("INTERNET_LOST"));
        }else {
            context.sendBroadcast(new Intent("INTERNET_AVAILABLE"));
            GlobalContext.INSTANCE.put(GlobalContext.INTERNET_AVAILABLE, new Boolean(connected).toString());
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager
                = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

}