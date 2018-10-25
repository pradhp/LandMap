package com.pearnode.app.placero.connectivity.services;

import android.app.IntentService;
import android.content.Intent;

import java.util.List;

import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.media.model.Media;

public class ResourceSynchronizationService extends IntentService {

    public ResourceSynchronizationService() {
        super(ResourceSynchronizationService.class.getSimpleName());
    }

    public ResourceSynchronizationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MediaDataBaseHandler ddh = new MediaDataBaseHandler(getApplicationContext());
        List<Media> dirtyMedia = ddh.getDirtyMedia();
        for (Media resource : dirtyMedia) {
            String dirtyAction = resource.getDirtyAction();
            if (dirtyAction.equalsIgnoreCase("insert")) {
                    // TODO : Invoke the media insert code here.
            } else if (dirtyAction.equalsIgnoreCase("update")) {
                // TODO : Invoke the media update code here.
            } else if (dirtyAction.equalsIgnoreCase("delete")) {
                // TODO : Invoke the media remove code here.
            }
        }
    }

}