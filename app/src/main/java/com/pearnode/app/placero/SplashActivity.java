package com.pearnode.app.placero;

/**
 * Created by USER on 10/27/2017.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.connectivity.ConnectivityChangeReceiver;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.sync.LocalDataRefresher;
import com.pearnode.app.placero.sync.LocalFolderStructureManager;
import com.pearnode.app.placero.tags.TagsDBHelper;
import com.pearnode.app.placero.user.UserDBHelper;

import java.io.File;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        setContentView(R.layout.activity_splash);

        new UserDBHelper(getApplicationContext()).dryRun();
        new AreaDBHelper(getApplicationContext()).dryRun();
        new PositionsDBHelper(getApplicationContext()).dryRun();
        new PermissionsDBHelper(getApplicationContext()).dryRun();
        new MediaDataBaseHandler(getApplicationContext()).dryRun();
        new TagsDBHelper(getApplicationContext()).dryRun();

        isPermissionsGranted();
    }

    public  boolean isPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                    android.Manifest.permission.ACCESS_NETWORK_STATE,
                    android.Manifest.permission.CALL_PHONE,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.GET_ACCOUNTS

            }, 1);
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            System.out.println(grantResults[i]);
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission " + permissions[i] + " not granted. The app will now exit.", Toast.LENGTH_LONG);
                finish();
                System.exit(0);
            }
        }
        new LocalDataRefresher(getApplicationContext(), new DataReloadCallback()).refreshLocalData();
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