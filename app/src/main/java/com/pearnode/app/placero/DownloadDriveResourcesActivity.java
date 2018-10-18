package com.pearnode.app.placero;

import android.Manifest.permission;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Builder;
import com.google.api.services.drive.DriveScopes;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.custom.ThumbnailCreator;
import com.pearnode.app.placero.drive.Resource;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks;

public class DownloadDriveResourcesActivity extends Activity implements PermissionCallbacks {

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {DriveScopes.DRIVE_METADATA_READONLY,
            DriveScopes.DRIVE, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(layout.activity_generic_wait);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Downloading shared files ...");
        mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(),
                Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
        downloadResources();
    }


    private void downloadResources() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                downloadResources();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account.",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    downloadResources();
                } else {
                    backToDetails("error", "Play services unavailable.");
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        downloadResources();
                    }
                } else {
                    backToDetails("error", "Could not choose account.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    downloadResources();
                } else {
                    backToDetails("error", "Authorization failed.");
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    void showGooglePlayServicesAvailabilityErrorDialog(
            int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(this, connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, Boolean> {
        private Drive mService;
        private Exception mLastError;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new Builder(transport, jsonFactory, credential)
                    .setApplicationName("LMS").build();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                AreaContext ac = AreaContext.INSTANCE;
                Area ae = ac.getAreaElement();
                List<Resource> resources = ae.getMediaResources();
                for (int j = 0; j < resources.size(); j++) {
                    Resource resource = resources.get(j);
                    String contentType = resource.getContentType();
                    if (!resource.getType().equalsIgnoreCase("folder")) {
                        File storeRoot = ac.getLocalStoreLocationForDriveResource(resource);
                        if (!storeRoot.exists()) {
                            storeRoot.mkdirs();
                        }

                        String resourceId = resource.getResourceId();
                        if (resourceId.equalsIgnoreCase("")
                                || resourceId.equalsIgnoreCase("1")
                                || resourceId.equalsIgnoreCase("2")) {
                            continue;
                        }

                        File storeFile = new File(storeRoot.getAbsolutePath() + File.separatorChar + resource.getName());
                        if (storeFile.exists()) {
                            long storeFileSize = storeFile.length();
                            if(!(storeFileSize == Long.parseLong(resource.getSize()))){
                                storeFile.delete();
                            }else {
                                continue;
                            }
                        }
                        storeFile.createNewFile();

                        OutputStream outputStream = new FileOutputStream(storeFile);
                        InputStream remoteStream = null;
                        try {
                            Drive.Files.Get get = mService.files().get(resourceId);
                            remoteStream = get.executeMediaAsInputStream();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        if(remoteStream == null){
                            outputStream.close();
                            continue;
                        }

                        IOUtils.copyLarge(remoteStream, outputStream);
                        outputStream.flush();
                        outputStream.close();

                        ThumbnailCreator tCreator = new ThumbnailCreator(getApplicationContext());
                        if (contentType.equalsIgnoreCase("Image")) {
                            tCreator.createImageThumbnail(storeFile, ae.getUniqueId());
                        } else if (contentType.equalsIgnoreCase("Video")) {
                            tCreator.createVideoThumbnail(storeFile, ae.getUniqueId());
                        } else {
                            tCreator.createDocumentThumbnail(storeFile, ae.getUniqueId());
                        }
                    }
                }
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Intent displayIntent = new Intent(getApplicationContext(), AreaResourceDisplayActivity.class);
            startActivity(displayIntent);
            finish();
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                }
            }
            finish();
        }
    }

    private void backToDetails(String code, String message) {
        Intent detailsIntent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
        detailsIntent.putExtra("outcome_type", code);
        detailsIntent.putExtra("outcome", message);
        detailsIntent.putExtra("action", "Download Resources");
        startActivity(detailsIntent);
        finish();
    }
}