package com.pearnode.app.placero;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.R.id;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.permission.PermissionConstants;
import com.pearnode.app.placero.permission.PermissionManager;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.UserInfoSearchAsyncTask;
import com.pearnode.app.placero.util.AreaPopulationUtil;
import com.pearnode.app.placero.util.ColorProvider;
import com.pearnode.app.placero.util.GeneralUtil;

public class AreaShareActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private String targetUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        this.setContentView(R.layout.activity_area_share);

        Area area = AreaContext.INSTANCE.getAreaElement();
        ActionBar ab = this.getSupportActionBar();
        ab.setHomeButtonEnabled(false);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setBackgroundDrawable(new ColorDrawable(ColorProvider.getAreaToolBarColor(area)));
        ab.show();

        View includedView = this.findViewById(R.id.selected_area_include);
        AreaPopulationUtil.INSTANCE.populateAreaElement(includedView);

        this.adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{});
        AutoCompleteTextView userIdView = (AutoCompleteTextView) this.findViewById(R.id.user_search_text);
        userIdView.setAdapter(this.adapter);

        userIdView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                UserInfoSearchAsyncTask searcherTask = new UserInfoSearchAsyncTask();
                JSONObject searchParams = new JSONObject();
                try {
                    searchParams.put("ss", s.toString());
                    searchParams.put("sf", "email");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                searcherTask.setCompletionCallback(new UserInfoCallBack());
                searcherTask.execute(searchParams);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        RadioButton shareFullRadio = (RadioButton) this.findViewById(R.id.share_full_radio);
        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.FULL_CONTROL)) {
            shareFullRadio.setEnabled(true);
        }

        RadioButton shareRestrictedRadio = (RadioButton) this.findViewById(R.id.share_restricted_radio);
        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.SHARE_READ_WRITE)) {
            shareRestrictedRadio.setEnabled(true);
        }

        final RadioGroup roleGroup = (RadioGroup) this.findViewById(R.id.area_share_role_group);
        roleGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                ViewStub stub = (ViewStub) findViewById(R.id.share_details_stub);
                if (stub == null) {
                    // View stub is already inflated
                    View inflatedStub = findViewById(R.id.share_details_stub_restricted);
                    if (inflatedStub != null) {
                        if (inflatedStub.getVisibility() == View.VISIBLE) {
                            inflatedStub.setVisibility(View.GONE);
                        } else {
                            if (checkedId == R.id.share_restricted_radio) {
                                inflatedStub.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                } else {
                    if (checkedId == R.id.share_restricted_radio) {
                        stub.setLayoutResource(layout.area_share_restricted);
                        stub.inflate();
                    }
                }
            }
        });

        Button saveButton = (Button) this.findViewById(R.id.area_share_save_btn);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);

                AutoCompleteTextView userIdView = (AutoCompleteTextView) findViewById(R.id.user_search_text);
                targetUser = userIdView.getText().toString();
                // target user should be a valid email.
                if (!GeneralUtil.isValidEmail(targetUser)) {
                    showErrorMessage("Please enter a valid email");
                    findViewById(R.id.splash_panel).setVisibility(View.GONE);
                    return;
                }

                int radioButtonID = roleGroup.getCheckedRadioButtonId();
                View radioButton = roleGroup.findViewById(radioButtonID);
                int idx = roleGroup.indexOfChild(radioButton);

                PermissionsDBHelper pmh = new PermissionsDBHelper(getApplicationContext(),
                        new DatabaseUpdateCallback());
                if (idx == 0) {
                    // For view insert view_only permission.
                    pmh.insertPermissionsToServer(targetUser, "view_only");
                } else if (idx == 1) {
                    // For Full control insert full_control permission.
                    pmh.insertPermissionsToServer(targetUser, "full_control");
                } else if (idx == 2) {
                    View inflatedStub = findViewById(R.id.share_details_stub_restricted);
                    // For restricted read all the values.
                    ArrayList<View> touchableViews = inflatedStub.getTouchables();
                    List<String> checkedFunctions = new ArrayList<String>();
                    for (int i = 0; i < touchableViews.size(); i++) {
                        View actualView = touchableViews.get(i);
                        if (actualView instanceof CheckBox) {
                            CheckBox checkBox = (CheckBox) actualView;
                            if (checkBox.isChecked()) {
                                checkedFunctions.add(checkBox.getTag().toString());
                            }
                        }
                    }
                    String joinedFunctions = TextUtils.join(",", checkedFunctions);
                    pmh.insertPermissionsToServer(targetUser, joinedFunctions);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent areaDetails = new Intent(this.getApplicationContext(), AreaDetailsActivity.class);
        this.startActivity(areaDetails);
        this.finish();
    }

    private class UserInfoCallBack implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            try {
                String userArray = result.toString();
                String currUserEmail = UserContext.getInstance().getUserElement().getEmail();
                adapter.clear();

                if (!userArray.trim().equalsIgnoreCase("[]")) {
                    JSONArray responseArr = new JSONArray(userArray);
                    for (int i = 0; i < responseArr.length(); i++) {
                        JSONObject responseObj = (JSONObject) responseArr.get(i);
                        String emailStr = responseObj.getString("email");
                        if (!currUserEmail.equalsIgnoreCase(emailStr)) {
                            adapter.add(emailStr);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class DatabaseUpdateCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            Intent shareResourcesIntent = new Intent(getApplicationContext(), ShareDriveResourcesActivity.class);
            shareResourcesIntent.putExtra("share_to_user", targetUser);
            startActivity(shareResourcesIntent);
            finish();
        }
    }

    private void showErrorMessage(String message) {
        Snackbar snackbar = Snackbar.make(this.getWindow().getDecorView(), message, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

}
