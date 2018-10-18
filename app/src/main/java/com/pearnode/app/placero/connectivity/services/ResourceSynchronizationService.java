package com.pearnode.app.placero.connectivity.services;

import android.app.IntentService;
import android.content.Intent;

import java.util.ArrayList;

import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.custom.GlobalContext;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.drive.Resource;
import com.pearnode.app.placero.position.PositionsDBHelper;

public class ResourceSynchronizationService extends IntentService {

    public ResourceSynchronizationService() {
        super(ResourceSynchronizationService.class.getSimpleName());
    }

    public ResourceSynchronizationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
        final ArrayList<Resource> dirtyResources = ddh.getDirtyResources();
        for (Resource resource : dirtyResources) {
            String dirtyAction = resource.getDirtyAction();
            if (dirtyAction.equalsIgnoreCase("insert")) {
                if (ddh.insertResourceToServer(resource)) {
                    resource.setDirty(0);
                    ddh.updateResourceLocally(resource);
                }
            } else if (dirtyAction.equalsIgnoreCase("update")) {
                if (ddh.updateResourceToServer(resource)) {
                    resource.setDirty(0);
                    ddh.updateResourceLocally(resource);
                }
            } else if (dirtyAction.equalsIgnoreCase("delete")) {
                ddh.deleteResourceByGlobally(resource);
            }
        }

        AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
        PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
        if(adh.getDirtyAreas().size() == 0
                && pdh.getDirtyPositions().size() == 0
                && ddh.getDirtyResources().size() == 0){
            GlobalContext.INSTANCE.put(GlobalContext.SYNCHRONIZING_OFFLINE, new Boolean(false).toString());
        }

    }

}