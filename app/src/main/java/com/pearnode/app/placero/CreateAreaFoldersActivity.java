package com.pearnode.app.placero;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.MetadataChangeSet.Builder;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import com.pearnode.app.placero.area.FileStorageConstants;
import com.pearnode.app.placero.connectivity.ConnectivityChangeReceiver;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.drive.DriveResource;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.UserElement;

public class CreateAreaFoldersActivity extends BaseDriveActivity {

    private boolean online = true;
    private boolean synchronizing = false;
    private String areaId = null;
    private ArrayList<String> areaIdList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronize_folders);

        Bundle extras = getIntent().getExtras();
        areaId = extras.getString("area_id");
        areaIdList = extras.getStringArrayList("area_id_list");

        String synchronizing = extras.getString("synchronizing");
        if(synchronizing != null){
            this.synchronizing = new Boolean(synchronizing);
        }
        online = ConnectivityChangeReceiver.isConnected(this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        new FileProcessingTask().execute();
    }

    @Override
    protected void handleConnectionIssues() {
        Intent intent = new Intent(getApplicationContext(), AreaDashboardActivity.class);
        intent.putExtra("action", "Drive Connection Error");
        intent.putExtra("outcome_type", "error");
        intent.putExtra("outcome", "Folder creation failed.");
        startActivity(intent);
    }

    private class FileProcessingTask extends AsyncTask {

        private final Stack<DriveResource> createStack = new Stack<>();

        @Override
        protected Object doInBackground(Object[] params) {
            if(areaId != null){
                buildResourcesForArea(areaId);
            }else {
                for (int i = 0; i < areaIdList.size(); i++) {
                    String fetchedAreaId = areaIdList.get(i);
                    buildResourcesForArea(fetchedAreaId);
                }
            }
            processCreateStack();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }


        private void buildResourcesForArea(String areaId) {
            DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
            // Check if there are common folders defined in database. //content_type = folder
            Map<String, DriveResource> commonResourceMap = ddh.getCommonResourcesByName();

            DriveResource imagesFolder = commonResourceMap.get(FileStorageConstants.IMAGE_ROOT_FOLDER_NAME);
            DriveResource folderResource = createFolderResource(areaId, imagesFolder,
                    FileStorageConstants.IMAGES_CONTENT_TYPE);
            createStack.push(folderResource);

            DriveResource videosFolder = commonResourceMap.get(FileStorageConstants.VIDEO_ROOT_FOLDER_NAME);
            folderResource = createFolderResource(areaId, videosFolder,
                    FileStorageConstants.VIDEOS_CONTENT_TYPE);
            createStack.push(folderResource);

            DriveResource docsFolder = commonResourceMap.get(FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME);
            folderResource = createFolderResource(areaId, docsFolder,
                    FileStorageConstants.DOCUMENTS_CONTENT_TYPE);
            createStack.push(folderResource);
        }

        public void processCreateStack() {
            DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
            if (createStack.isEmpty()) {
                getGoogleApiClient().disconnect();
                Intent intent = null;
                if(synchronizing){
                    intent = new Intent(getApplicationContext(), AreaDashboardActivity.class);
                    intent.putExtra("action", "Synchronizing Offline");
                    intent.putExtra("outcome_type", "info");
                    intent.putExtra("outcome", "Complete.");
                }else {
                    intent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
                }
                startActivity(intent);
            } else {
                DriveResource resource = createStack.pop();
                if(online){
                    Drive.DriveApi.fetchDriveId(getGoogleApiClient(), resource.getContainerId())
                            .setResultCallback(new DriveIdResultCallback(resource));
                }else {
                    ddh.insertResourceLocally(resource);
                }
            }
        }

        private class DriveIdResultCallback implements ResultCallback<DriveIdResult> {

            private final DriveResource resource;

            public DriveIdResultCallback(DriveResource resource) {
                this.resource = resource;
            }

            @Override
            public void onResult(DriveIdResult driveIdResult) {
                DriveFolder parentFolder = driveIdResult.getDriveId().asDriveFolder();
                MetadataChangeSet changeSet
                        = new Builder().setTitle(resource.getName())
                        .setMimeType(resource.getMimeType()).build();
                parentFolder.createFolder(getGoogleApiClient(), changeSet)
                        .setResultCallback(new CreateFolderCallback(resource));
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
                MetadataChangeSet changeSet = new Builder()
                        .setTitle(resource.getName())
                        .build();
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
                    resource.setResourceId(resourceId);

                    DriveResourceInsertCallback callback = new DriveResourceInsertCallback(this.resource);
                    DriveDBHelper ddh = new DriveDBHelper(getApplicationContext(), callback);
                    // This is to handle offline insertion
                    ddh.deleteResourceLocally(resource);
                    ddh.insertResourceLocally(resource);
                    ddh.insertResourceToServer(resource);
                }
            }

            private class DriveResourceInsertCallback implements AsyncTaskCallback {

                private final DriveResource resource;

                public DriveResourceInsertCallback(DriveResource resource) {
                    this.resource = resource;
                }

                @Override
                public void taskCompleted(Object result) {
                    processCreateStack();
                }
            }
        }

    }

    private DriveResource createFolderResource(String areaId, DriveResource parent, String contentType) {
        DriveResource resource = new DriveResource();
        UserElement userElement = UserContext.getInstance().getUserElement();
        resource.setUniqueId(UUID.randomUUID().toString());
        resource.setName(areaId);
        resource.setType("folder");
        resource.setUserId(userElement.getEmail());
        resource.setSize("0");
        resource.setAreaId(areaId);
        resource.setContainerId(parent.getResourceId());
        resource.setContentType(contentType);
        resource.setMimeType("application/vnd.google-apps.folder");
        resource.setResourceId(null);
        return resource;
    }

}
