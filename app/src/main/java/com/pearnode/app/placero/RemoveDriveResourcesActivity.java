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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.AreaElement;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.drive.DriveResource;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks;

public class RemoveDriveResourcesActivity extends Activity implements PermissionCallbacks {

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {DriveScopes.DRIVE_METADATA_READONLY,
            DriveScopes.DRIVE, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA};

    private String resourceIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(layout.activity_remove_resources);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Removing area files ...");
        mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(),
                Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            removeResources(resourceIds);
        } else {
            String resourceIds = extras.getString("resource_ids");
            if (resourceIds != null) {
                this.resourceIds = resourceIds;
            }
            removeResources(resourceIds);
        }
    }

    private void removeResources(String resourceIds) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (resourceIds == null) {
                new RemoveRequestTask(mCredential).execute();
            } else {
                new RemoveRequestTask(mCredential, resourceIds).execute();
            }
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
                removeResources(resourceIds);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(this.mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
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
                    removeResources(null);
                } else {
                    // TODO Share the error with the user
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = this.getPreferences(Context.MODE_PRIVATE);
                        Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        removeResources(resourceIds);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    removeResources(resourceIds);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
        Dialog dialog = apiAvailability.getErrorDialog(this, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class RemoveRequestTask extends AsyncTask<Void, Void, Boolean> {
        private Drive mService;
        private Exception mLastError;
        private String resourceIds;

        private RemoveRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new Builder(transport, jsonFactory, credential)
                    .setApplicationName("LMS").build();
        }

        private RemoveRequestTask(GoogleAccountCredential credential, String resourceIds) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new Builder(transport, jsonFactory, credential)
                    .setApplicationName("LMS").build();
            this.resourceIds = resourceIds;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                AreaContext ac = AreaContext.INSTANCE;
                AreaElement ae = ac.getAreaElement();
                DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
                if (this.resourceIds != null) {
                    String[] resourceArr = this.resourceIds.split(",");
                    for (int i = 0; i < resourceArr.length; i++) {
                        String resourceID = resourceArr[i];
                        DriveResource fetchedResource = ddh.getDriveResourceByResourceId(resourceID);
                        ddh.deleteResourceByGlobally(fetchedResource);
                        ddh.deleteResourceFromServer(fetchedResource);
                        ae.getMediaResources().remove(fetchedResource);

                        String contentType = fetchedResource.getContentType();
                        String resourceRootPath = null;
                        String thumbnailRootPath = null;
                        if (contentType.equalsIgnoreCase("Image")) {
                            resourceRootPath = ac.getAreaLocalImageRoot(ae.getUniqueId()).getAbsolutePath();
                            thumbnailRootPath = ac.getAreaLocalPictureThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
                        } else if (contentType.equalsIgnoreCase("Video")) {
                            resourceRootPath = ac.getAreaLocalVideoRoot(ae.getUniqueId()).getAbsolutePath();
                            thumbnailRootPath = ac.getAreaLocalVideoThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
                        } else {
                            resourceRootPath = ac.getAreaLocalDocumentRoot(ae.getUniqueId()).getAbsolutePath();
                            thumbnailRootPath = ac.getAreaLocalDocumentThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
                        }

                        File localFile = new File(resourceRootPath + File.separatorChar + fetchedResource.getName());
                        if (localFile.exists()) {
                            localFile.delete();
                        }

                        File thumbFile = new File(thumbnailRootPath + File.separatorChar + fetchedResource.getName());
                        if (thumbFile.exists()) {
                            thumbFile.delete();
                        }
                        try {
                            this.mService.files().delete(resourceID).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    List<DriveResource> resources = ae.getMediaResources();
                    for (int j = 0; j < resources.size(); j++) {
                        DriveResource resource = resources.get(j);
                        if (resource.getType().equalsIgnoreCase("File")) {
                            File storeRoot = ac.getLocalStoreLocationForDriveResource(resource);
                            if (storeRoot != null) {
                                if (storeRoot.exists()) {
                                    FileUtils.deleteDirectory(storeRoot);
                                }
                            }
                            continue;
                        }
                        if (!resource.getContainerId().trim().equalsIgnoreCase("")) {
                            // Delete only the area specific folders and not the common folders.
                            try {
                                this.mService.files().delete(resource.getResourceId()).execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
            Intent displayIntent = null;
            if (resourceIds != null) {
                displayIntent = new Intent(getApplicationContext(), AreaResourceDisplayActivity.class);
                displayIntent.putExtras(getIntent().getExtras());
            } else {
                displayIntent = new Intent(getApplicationContext(), AreaDashboardActivity.class);
            }
            startActivity(displayIntent);
            finish();
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (this.mLastError != null) {
                if (this.mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) this.mLastError)
                                    .getConnectionStatusCode());
                } else if (this.mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) this.mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                }
            }
            finish();
        }
    }
}