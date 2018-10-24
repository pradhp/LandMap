package com.pearnode.app.placero;

/**
 * Created by USER on 10/27/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.connectivity.ConnectivityChangeReceiver;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.sync.LocalDataRefresher;
import com.pearnode.app.placero.sync.LocalFolderStructureManager;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        setContentView(layout.activity_splash);

        if (ConnectivityChangeReceiver.isConnected(this)) {
            new LocalDataRefresher(getApplicationContext(), new DataReloadCallback()).refreshLocalData();
        } else {
            Intent dashboardIntent = new Intent(this, AreaDashboardActivity.class);
            startActivity(dashboardIntent);
            finish();
        }
        LocalFolderStructureManager.create();
    }

    private class DataReloadCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            Intent dashboardIntent = new Intent(SplashActivity.this, AreaDashboardActivity.class);
            startActivity(dashboardIntent);
            finish();
        }
    }

}