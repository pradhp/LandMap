package com.pearnode.app.placero;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.MetadataChangeSet.Builder;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;

import java.util.Stack;
import java.util.UUID;

import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.area.FileStorageConstants;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.drive.DriveResource;
import com.pearnode.app.placero.user.UserContext;

public class CreateCommonFolderStructureActivity extends BaseDriveActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_synchronize_folders);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        new FileProcessingTask().execute();
    }

    @Override
    protected void handleConnectionIssues() {
        Intent areaDetailsIntent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
        areaDetailsIntent.putExtra("action", "Drive Error");
        areaDetailsIntent.putExtra("outcome_type", "error");
        areaDetailsIntent.putExtra("outcome", "Common folder creation failed.");
        startActivity(areaDetailsIntent);
    }

    private class FileProcessingTask extends AsyncTask {

        private final Stack<DriveResource> createStack = new Stack<>();

        @Override
        protected Object doInBackground(Object[] params) {
            this.fetchStatusAndAct();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }


        private void fetchStatusAndAct() {
            // If no create common folders in database.
            DriveResource commonImageFolder
                    = createCommonFolderResource(FileStorageConstants.IMAGE_ROOT_FOLDER_NAME);
            // If no create folder in drive.
            this.createStack.push(commonImageFolder);

            DriveResource commonVideosFolder
                    = createCommonFolderResource(FileStorageConstants.VIDEO_ROOT_FOLDER_NAME);
            // If no create folder in drive.
            this.createStack.push(commonVideosFolder);

            DriveResource commonDocsFolder
                    = createCommonFolderResource(FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME);
            // If no create folder in drive.
            this.createStack.push(commonDocsFolder);

            this.processCreateStack();
        }

        public void processCreateStack() {
            if (this.createStack.isEmpty()) {
                getGoogleApiClient().disconnect();
                Intent intent = new Intent(CreateCommonFolderStructureActivity.this, AreaDashboardActivity.class);
                startActivity(intent);
                finish();
            } else {
                DriveResource res = this.createStack.pop();
                MetadataChangeSet changeSet
                        = new Builder().setTitle(res.getName()).setMimeType(res.getMimeType()).build();
                Drive.DriveApi.getRootFolder(getGoogleApiClient())
                        .createFolder(getGoogleApiClient(), changeSet).setResultCallback(new CreateFolderCallback(res));
            }
        }

        private class CreateFolderCallback implements ResultCallback<DriveFolder.DriveFolderResult> {

            private DriveResource resource;

            public CreateFolderCallback(DriveResource resource) {
                this.resource = resource;
            }

            @Override
            public void onResult(DriveFolder.DriveFolderResult folderResult) {
                DriveFolder driveFolder = folderResult.getDriveFolder();
                driveFolder.addChangeListener(getGoogleApiClient(), new FolderMetaChangeListener(this.resource));
                MetadataChangeSet changeSet = new Builder().setStarred(true).build();
                driveFolder.updateMetadata(getGoogleApiClient(), changeSet);
            }
        }

        private class FolderMetaChangeListener implements ChangeListener {

            private DriveResource resource;

            public FolderMetaChangeListener(DriveResource res) {
                this.resource = res;
            }

            @Override
            public void onChange(ChangeEvent changeEvent) {
                DriveFolder eventFolder = changeEvent.getDriveId().asDriveFolder();
                DriveId driveId = eventFolder.getDriveId();
                String resourceId = driveId.getResourceId();
                if (resourceId != null && resource.getResourceId() == null) {
                    eventFolder.removeChangeListener(getGoogleApiClient(), this);

                    DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
                    resource.setResourceId(resourceId);
                    ddh.insertResourceLocally(resource);
                    ddh.insertResourceToServer(resource);

                    processCreateStack();
                }
            }
        }
    }

    private DriveResource createCommonFolderResource(String commonFolderName) {
        DriveResource resource = new DriveResource();
        resource.setUniqueId(UUID.randomUUID().toString());
        resource.setName(commonFolderName);
        resource.setType("folder");
        resource.setUserId(UserContext.getInstance().getUserElement().getEmail());
        resource.setSize("0");
        resource.setAreaId("");
        resource.setResourceId(null);
        resource.setContainerId("");
        resource.setContentType("folder");
        resource.setMimeType("application/vnd.google-apps.folder");
        return resource;
    }

}
