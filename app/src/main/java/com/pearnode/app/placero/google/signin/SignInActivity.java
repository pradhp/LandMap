package com.pearnode.app.placero.google.signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pearnode.app.placero.R;
import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.R.string;
import com.pearnode.app.placero.SplashActivity;
import com.pearnode.app.placero.connectivity.ConnectivityChangeReceiver;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.custom.GlobalContext;
import com.pearnode.app.placero.tags.TagElement;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.UserDBHelper;
import com.pearnode.app.placero.user.UserElement;
import com.pearnode.app.placero.user.UserInfoSearchAsyncTask;
import com.pearnode.app.placero.util.UserMappingUtil;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
public class SignInActivity extends AppCompatActivity implements
        OnConnectionFailedListener,
        OnClickListener, AsyncTaskCallback {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;
    private UserElement signedUser;
    private boolean offline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the database.
        new UserDBHelper(getApplicationContext()).dryRun();
        GlobalContext.INSTANCE.put(GlobalContext.APPLICATION_STARTED, "true");

        setContentView(R.layout.activity_google_signin_main);
        getSupportActionBar().hide();

        mStatusTextView = (TextView) findViewById(id.status);

        findViewById(id.sign_in_button).setOnClickListener(this);
        findViewById(id.sign_out_button).setOnClickListener(this);
        findViewById(id.disconnect_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        SignInButton signInButton = (SignInButton) findViewById(id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
    }

    @Override
    public void taskCompleted(Object result) {
        try {
            String userDetails = result.toString();
            UserDBHelper udh = new UserDBHelper(getApplicationContext());
            Intent spashIntent = new Intent(this, SplashActivity.class);
            if (userDetails.trim().equalsIgnoreCase("[]")) {
                udh.insertUserToServer(signedUser);
                spashIntent.putExtra("user_exists", "false");
            }else {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("user_details", userDetails);
                editor.commit();

                buildUserSelectionsFromDetails(userDetails);
                spashIntent.putExtra("user_exists", "true");
            }
            startActivity(spashIntent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideProgressDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        TextView debugText = (TextView) findViewById(R.id.debug_text);
        debugText.setVisibility(View.VISIBLE);
        debugText.setText("Signin Result:[ ReqC - " + requestCode + ", ResC - " + resultCode + " ]");

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            // Convert google specific user account to app specific
            signedUser = UserMappingUtil.convertGoogleAccountToLocalAccount(acct);
            // Set the user context.
            UserContext.getInstance().setUserElement(signedUser);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("user_email", signedUser.getEmail());
            editor.putString("user_display_name", signedUser.getDisplayName());
            editor.putString("user_photo_url", signedUser.getPhotoUrl());
            editor.commit();

            searchOnRemoteAndUpdate(signedUser);
            mGoogleApiClient.disconnect();
        } else {
            boolean online = ConnectivityChangeReceiver.isConnected(this);
            offline = !online;
            if(!offline){
                UserElement user = buildUserFromPreferences();
                if(user == null){
                    updateUI(false);
                }else {
                    Intent spashIntent = new Intent(this, SplashActivity.class);
                    spashIntent.putExtra("user_exists", "true");
                    startActivity(spashIntent);
                    finish();
                }
            }else {
                updateUI(false);
            }
        }
    }

    private UserElement buildUserFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String email = preferences.getString("user_email", null);
        if(email == null){
            return null;
        }
        UserElement user = new UserElement();
        user.setEmail(email);

        String displayName = preferences.getString("user_display_name", null);
        user.setDisplayName(displayName);

        String photoUrl = preferences.getString("user_photo_url", null);
        user.setPhotoUrl(photoUrl);

        UserContext.getInstance().setUserElement(user);
        String userDetails = preferences.getString("user_details", null);
        if(userDetails != null){
            buildUserSelectionsFromDetails(userDetails);
        }
        return user;
    }

    private void searchOnRemoteAndUpdate(UserElement ue) {
        UserInfoSearchAsyncTask searchUserTask = new UserInfoSearchAsyncTask();
        JSONObject params = new JSONObject();
        try {
            params.put("ss", ue.getEmail());
            params.put("sf", "email");
            searchUserTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        searchUserTask.setCompletionCallback(this);
    }

    private void buildUserSelectionsFromDetails(String userDetails) {
        try {
            JSONArray userArr = new JSONArray(userDetails);
            JSONObject userObj = (JSONObject) userArr.get(0);
            String tagsStr = userObj.getString("tags");
            String[] tagsArr = tagsStr.split(",");

            String tagTypes = userObj.getString("tag_types");
            String[] tagTypesArr = tagTypes.split(",");

            UserElement userElement = UserContext.getInstance().getUserElement();
            for (int i = 0; i < tagsArr.length; i++) {
                TagElement tagElement = new TagElement(tagsArr[i], tagTypesArr[i], "user");
                userElement.getSelections().getTags().add(tagElement);
            }
        }catch (Exception e){
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        TextView debugText = (TextView) findViewById(R.id.debug_text);
        debugText.setVisibility(View.VISIBLE);
        debugText.setText("onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(id.sign_in_button).setVisibility(View.GONE);
            findViewById(id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(string.signed_out);

            findViewById(id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case id.sign_in_button:
                signIn();
                break;
            case id.sign_out_button:
                signOut();
                break;
            case id.disconnect_button:
                revokeAccess();
                break;
        }
    }
}
