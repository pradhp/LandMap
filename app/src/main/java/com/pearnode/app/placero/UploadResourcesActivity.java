package com.pearnode.app.placero;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.MetadataChangeSet.Builder;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.AreaElement;
import com.pearnode.app.placero.connectivity.ConnectivityChangeReceiver;
import com.pearnode.app.placero.custom.ApiClientAsyncTask;
import com.pearnode.app.placero.custom.ThumbnailCreator;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.drive.DriveResource;
import com.pearnode.app.placero.position.PositionElement;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.util.FileUtil;


/**
 * An activity to create a file inside a folder.
 */
public class UploadResourcesActivity extends BaseDriveActivity {

    private boolean online = true;
    private final Stack<DriveResource> processStack = new Stack<>();
    private List<String> uploadedResources = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_upload_area_resources);
        online = ConnectivityChangeReceiver.isConnected(this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        ArrayList<DriveResource> resources = AreaContext.INSTANCE.getUploadedQueue();
        for (int i = 0; i < resources.size(); i++) {
            processStack.push(resources.get(i));
        }
        processResources();
    }

    @Override
    protected void handleConnectionIssues() {
        ArrayList<DriveResource> resources = AreaContext.INSTANCE.getUploadedQueue();
        for (int i = 0; i < resources.size(); i++) {
            processStack.push(resources.get(i));
        }
        processResources();
    }

    private void processResources() {
        if (!processStack.isEmpty()) {
            processResource(processStack.pop());
        } else {
            if(online){
                Intent addResourcesIntent = new Intent(this, ShareUploadedResourcesActivity.class);
                addResourcesIntent.putExtra("uploaded_resource_ids", uploadedResources.toArray());
                getGoogleApiClient().disconnect();
                startActivity(addResourcesIntent);
            }else {
                Intent areaDetailsIntent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
                areaDetailsIntent.putExtra("action", "Upload");
                areaDetailsIntent.putExtra("outcome_type", "info");
                areaDetailsIntent.putExtra("outcome", "Completed offline upload");
                startActivity(areaDetailsIntent);
            }
            finish();
        }
    }

    private void processResource(DriveResource res) {
        DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
        PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
        if(online){
            new FileProcessingTask(res).execute();
        }else {
            ddh.deleteResourceLocally(res);
            ddh.insertResourceLocally(res);
            PositionElement position = res.getPosition();
            if(position != null){
                pdh.deletePositionLocally(position);
                pdh.insertPositionLocally(position);
            }
            AreaContext.INSTANCE.getUploadedQueue().remove(res);
            processResources();
        }
    }

    private class FileProcessingTask extends AsyncTask {

        private DriveResource resource;

        public FileProcessingTask(DriveResource dr) {
            this.resource = dr;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            DriveApi.DriveIdResult idResult
                    = Drive.DriveApi.fetchDriveId(getGoogleApiClient(), resource.getContainerId()).await();
            DriveId driveId = idResult.getDriveId();
            if(driveId == null){
                Intent addResourcesIntent = new Intent(getApplicationContext(), AreaAddResourcesActivity.class);
                addResourcesIntent.putExtra("action", "Upload");
                addResourcesIntent.putExtra("outcome_type", "error");
                addResourcesIntent.putExtra("outcome", "Upload location corrupted.");
                getGoogleApiClient().disconnect();
                startActivity(addResourcesIntent);
                finish();
            }else {
                DriveFolder folder = driveId.asDriveFolder();
                DriveApi.DriveContentsResult contentsResult
                        = Drive.DriveApi.newDriveContents(getGoogleApiClient()).await();
                DriveContents contents = contentsResult.getDriveContents();
                MetadataChangeSet changeSet = new Builder()
                        .setTitle(resource.getName())
                        .setMimeType(resource.getMimeType())
                        .build();
                DriveFolder.DriveFileResult driveFileResult
                        = folder.createFile(getGoogleApiClient(), changeSet, contents).await();
                DriveFile createdFile = driveFileResult.getDriveFile();
                createdFile.addChangeListener(getGoogleApiClient(),
                        new FileMetaChangeListener(resource, createdFile));
            }
            return null;
        }
    }

    private class FileMetaChangeListener implements ChangeListener {

        private DriveResource resource;
        private DriveFile driveFile;

        public FileMetaChangeListener(DriveResource res, DriveFile dFile) {
            this.resource = res;
            this.driveFile = dFile;
        }

        @Override
        public void onChange(ChangeEvent changeEvent) {
            if (changeEvent.hasMetadataChanged()) {
                DriveId driveId = changeEvent.getDriveId();
                resource.setResourceId(driveId.getResourceId());

                DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
                // Handling offline insertion
                resource.setDirty(0);
                resource.setDirtyAction("none");

                ddh.deleteResourceLocally(resource);
                ddh.insertResourceLocally(resource);
                ddh.deleteResourceFromServer(resource);
                ddh.insertResourceToServer(resource);

                PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                PositionElement position = resource.getPosition();
                if(position != null){
                    position.setUniqueAreaId(resource.getAreaId());
                    // Handling offline insertion
                    position.setDirty(0);
                    position.setDirtyAction("none");

                    pdh.deletePositionLocally(position);
                    pdh.insertPositionLocally(position);
                    pdh.deletePositionFromServer(position);
                    pdh.insertPositionToServer(position);
                }
                driveFile.removeChangeListener(getGoogleApiClient(), this);
                new CopyContentsAsyncTask(getApplicationContext(), resource).execute(driveFile);
            }
        }
    }

    public class CopyContentsAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {
        private DriveResource resource;

        public CopyContentsAsyncTask(Context context, DriveResource resource) {
            super(context);
            this.resource = resource;
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveFile... args) {
            DriveFile file = args[0];
            DriveApi.DriveContentsResult driveContentsResult = file.open(
                    getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return false;
            }

            DriveContents driveContents = driveContentsResult.getDriveContents();
            OutputStream outputStream = driveContents.getOutputStream();
            try {
                File inputFile = new File(resource.getPath());
                FileInputStream fileInputStream = new FileInputStream(inputFile);
                IOUtils.copyLarge(fileInputStream, outputStream);
                IOUtils.closeQuietly(fileInputStream);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            driveContents.commit(this.getGoogleApiClient(), null).await();
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            AreaContext areaContext = AreaContext.INSTANCE;
            AreaElement areaElement = areaContext.getAreaElement();

            areaContext.removeResourceFromQueue(resource);

            // Create thumbnails of the uploaded files for display.
            String resourcePath = resource.getPath();
            File resourceFile = new File(resourcePath);

            ThumbnailCreator tCreator = new ThumbnailCreator(getApplicationContext());
            if(FileUtil.isImageFile(resourceFile)){
                tCreator.createImageThumbnail(resourceFile, areaElement.getUniqueId());
            }else if(FileUtil.isVideoFile(resourceFile)){
                tCreator.createVideoThumbnail(resourceFile, areaElement.getUniqueId());
            }else {
                tCreator.createDocumentThumbnail(resourceFile, areaElement.getUniqueId());
            }
            uploadedResources.add(resource.getResourceId());
            processResources();
        }
    }
}
