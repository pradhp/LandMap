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
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Builder;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.AreaElement;
import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks;

public class ShareUploadedResourcesActivity extends Activity implements PermissionCallbacks {

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {DriveScopes.DRIVE_METADATA_READONLY};

    private final String shareRole = "reader";
    private List<String> usersToShare = new ArrayList<>();
    private List<String> resourceIdsToShare = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(layout.activity_share_area_resources);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            Object[] uploaded_resource_ids = (Object[]) extras.get("uploaded_resource_ids");
            for (int i = 0; i < uploaded_resource_ids.length; i++) {
                resourceIdsToShare.add(uploaded_resource_ids[i].toString());
            }
        }
        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        AreaDBHelper adh = new AreaDBHelper(getApplicationContext(), new ShareHistoryCallback());
        adh.fetchShareHistory(areaElement);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Sharing Area ...");
        mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
    }

    private class ShareHistoryCallback implements AsyncTaskCallback{

        @Override
        public void taskCompleted(Object result) {
            String response = result.toString();
            try {
                JSONArray responseArr = new JSONArray(response);
                for (int i = 0; i < responseArr.length(); i++) {
                    JSONObject shareObj = responseArr.getJSONObject(i);
                    String targetUser = shareObj.getString("target_user");
                    // TODO get restricted share permissions and set on resource.
                    // TODO for now going with read_only
                    usersToShare.add(targetUser);
                }
                if(usersToShare.size() > 0){
                    shareResources();
                }else {
                    Intent areaDetailsIntent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
                    areaDetailsIntent.putExtra("action", "Upload");
                    areaDetailsIntent.putExtra("outcome_type", "info");
                    areaDetailsIntent.putExtra("outcome", "Completed upload");
                    startActivity(areaDetailsIntent);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void shareResources() {
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
                shareResources();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
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
                    shareResources();
                } else {
                    // TODO Share the error with the user
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
                        shareResources();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    shareResources();
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

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private Drive mService;
        private Exception mLastError;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new Builder(transport, jsonFactory, credential).setApplicationName("LMS").build();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                final List<String> shareStatusInfo = new ArrayList<String>();
                for (int i = 0; i < usersToShare.size(); i++) {
                    String targetUser = usersToShare.get(i);
                    for (int j = 0; j < resourceIdsToShare.size(); j++) {
                        String resourceId = resourceIdsToShare.get(j);
                        BatchRequest batch = mService.batch();

                        Permission userPermission = new Permission();
                        userPermission.setType("user");
                        userPermission.setRole(shareRole);
                        userPermission.setEmailAddress(targetUser);
                        mService.permissions().create(resourceId, userPermission)
                                .setFields("id")
                                .queue(batch, new JsonBatchCallback<Permission>() {
                                    @Override
                                    public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
                                    }
                                    @Override
                                    public void onSuccess(Permission permission, HttpHeaders responseHeaders) {
                                    }
                                });
                        batch.execute();
                    }
                }
                return shareStatusInfo;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            finish();
            Intent areaDetailsIntent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
            areaDetailsIntent.putExtra("action", "Upload");
            areaDetailsIntent.putExtra("outcome_type", "info");
            areaDetailsIntent.putExtra("outcome", "Completed Upload and Share.");
            startActivity(areaDetailsIntent);
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
}