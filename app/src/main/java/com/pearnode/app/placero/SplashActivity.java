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

import com.pearnode.app.placero.area.db.AreaDatabaseHandler;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.permission.PermissionDatabaseHandler;
import com.pearnode.app.placero.position.PositionDatabaseHandler;
import com.pearnode.app.placero.sync.LocalDataRefresher;
import com.pearnode.app.placero.sync.LocalFolderStructureManager;
import com.pearnode.app.placero.tags.TagDatabaseHandler;
import com.pearnode.app.placero.user.UserDatabaseHandler;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        setContentView(R.layout.activity_splash);

        new UserDatabaseHandler(getApplicationContext()).dryRun();
        new AreaDatabaseHandler(getApplicationContext()).dryRun();
        new PositionDatabaseHandler(getApplicationContext()).dryRun();
        new PermissionDatabaseHandler(getApplicationContext()).dryRun();
        new MediaDataBaseHandler(getApplicationContext()).dryRun();
        new TagDatabaseHandler(getApplicationContext()).dryRun();

        askForPermissions();
    }

    public  boolean askForPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.GET_ACCOUNTS

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
            System.out.println("Permission :" + permissions[i] + " Result - " + grantResults[i]);
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