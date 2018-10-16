package com.pearnode.app.placero;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.AreaElement;
import com.pearnode.app.placero.area.res.disp.AreaAddResourceAdaptor;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.drive.DriveResource;
import com.pearnode.app.placero.util.AreaPopulationUtil;
import com.pearnode.app.placero.util.ColorProvider;

public class AreaAddResourcesActivity extends AppCompatActivity {

    private AreaAddResourceAdaptor adaptor;
    private ArrayList<DriveResource> resourceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_resource_main);

        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(false);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setBackgroundDrawable(new ColorDrawable(ColorProvider.getAreaToolBarColor(areaElement)));
        ab.show();

        View includedView = findViewById(R.id.selected_area_include);
        AreaPopulationUtil.INSTANCE.populateAreaElement(includedView);

        ArrayList<DriveResource> driveResources = AreaContext.INSTANCE.getUploadedQueue();
        resourceList.addAll(driveResources);

        ListView resListView = (ListView) findViewById(R.id.file_display_list);
        adaptor = new AreaAddResourceAdaptor(getApplicationContext(),resourceList);
        resListView.setAdapter(adaptor);
        adaptor.notifyDataSetChanged();

        Button takeSnapButton = (Button) findViewById(R.id.take_snap_button);
        takeSnapButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaCameraPictureActivity.class);
                startActivity(i);
            }
        });

        Button captureVideoButton = (Button) findViewById(R.id.shoot_video_button);
        captureVideoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaCameraVideoActivity.class);
                startActivity(i);
            }
        });

        Button chooseDocumentButton = (Button) findViewById(R.id.add_document_button);
        chooseDocumentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaDocumentChooserActivity.class);
                startActivity(i);
            }
        });

        Button driveUploadButton = (Button) findViewById(R.id.upload_to_drive_button);
        driveUploadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adaptor.getCount() == 0){
                    showMessage("Nothing to upload.", "error");
                    return;
                }
                Intent i = new Intent(AreaAddResourcesActivity.this, UploadResourcesActivity.class);
                startActivity(i);
            }
        });

        showErrorsIfAny();
    }

    private void showErrorsIfAny() {
        Bundle intentBundle = getIntent().getExtras();
        if (intentBundle != null) {
            String action = intentBundle.getString("action");
            String outcome = intentBundle.getString("outcome");
            String outcomeType = intentBundle.getString("outcome_type");
            showMessage(action + " " + outcomeType + ". " + outcome, outcomeType);
        }
    }

    @Override
    public void onBackPressed() {
        Intent detailsIntent = new Intent(this, AreaDetailsActivity.class);
        startActivity(detailsIntent);
        finish();
    }

    private void showMessage(String message, String type) {
        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView(),
                message + ".", Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        snackbar.getView().setBackgroundColor(Color.parseColor("#FAF7F6"));

        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        if (type.equalsIgnoreCase("info")) {
            textView.setTextColor(Color.parseColor("#30601F"));
        } else if (type.equalsIgnoreCase("error")) {
            textView.setTextColor(Color.RED);
        } else {
            textView.setTextColor(Color.DKGRAY);
        }
        textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        textView.setTextSize(15);
        textView.setMaxLines(3);

        snackbar.setAction("Dismiss", new OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

}
