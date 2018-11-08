package com.pearnode.app.placero;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
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
import java.util.List;

import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.res.disp.AreaAddResourceAdaptor;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.media.tasks.MediaHandlerTask;
import com.pearnode.app.placero.media.model.Media;
import com.pearnode.app.placero.util.AreaPopulationUtil;
import com.pearnode.app.placero.util.ColorProvider;

public class AreaAddResourcesActivity extends AppCompatActivity {

    private AreaAddResourceAdaptor adaptor;
    private List<Media> resourceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_resource_main);

        Area area = AreaContext.INSTANCE.getArea();
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(false);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setBackgroundDrawable(new ColorDrawable(ColorProvider.getAreaToolBarColor(area)));
        ab.show();

        View includedView = findViewById(R.id.selected_area_include);
        AreaPopulationUtil.INSTANCE.populateAreaElement(includedView);

        List<Media> resources = AreaContext.INSTANCE.getUploadedQueue();
        resourceList.addAll(resources);

        ListView resListView = (ListView) findViewById(R.id.file_display_list);
        adaptor = new AreaAddResourceAdaptor(getApplicationContext(),resourceList);
        resListView.setAdapter(adaptor);

        Button takeSnapButton = (Button) findViewById(R.id.take_snap_button);
        takeSnapButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaPictureCaptureActivity.class);
                startActivity(i);
            }
        });

        Button captureVideoButton = (Button) findViewById(R.id.shoot_video_button);
        captureVideoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaVideoCaptureActivity.class);
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

        Button uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemCnt = adaptor.getCount();
                if(itemCnt == 0){
                    showMessage("Nothing to upload.", "error");
                    return;
                }else {
                    for (int i = 0; i < itemCnt; i++) {
                        Media media = adaptor.getItem(i);
                        AsyncTask mediaHandlerTask = new MediaHandlerTask(getApplicationContext(), null);
                        mediaHandlerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, media);
                        AreaContext.INSTANCE.getUploadedQueue().remove(media);
                    }
                    Intent intent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
                    intent.putExtra("action", "Upload Media");
                    intent.putExtra("outcome", "Media upload started in background");
                    intent.putExtra("outcome_type", "info");
                    startActivity(intent);
                }
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
