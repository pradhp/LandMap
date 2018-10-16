package com.pearnode.app.placero.connectivity.services;

import android.app.IntentService;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Arrays;

import com.pearnode.app.placero.CreateAreaFoldersActivity;
import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.model.AreaElement;
import com.pearnode.app.placero.custom.GlobalContext;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.position.PositionsDBHelper;

public class AreaSynchronizationService extends IntentService {

    public AreaSynchronizationService() {
        super(AreaSynchronizationService.class.getSimpleName());
    }

    public AreaSynchronizationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
        final ArrayList<AreaElement> dirtyAreas = adh.getDirtyAreas();
        String[] areaIds = new String[dirtyAreas.size()];
        int ctr = 0;
        for (AreaElement areaElement : dirtyAreas) {
            String dirtyAction = areaElement.getDirtyAction();
            if (dirtyAction.equalsIgnoreCase("insert")) {
                if (adh.insertAreaToServer(areaElement)) {
                    areaElement.setDirty(0);
                    adh.updateAreaLocally(areaElement);
                    areaIds[ctr] = areaElement.getUniqueId();
                    ctr++;
                }
            } else if (dirtyAction.equalsIgnoreCase("update")) {
                if (adh.updateAreaOnServer(areaElement)) {
                    areaElement.setDirty(0);
                    adh.updateAreaLocally(areaElement);
                }
            } else if (dirtyAction.equalsIgnoreCase("delete")) {
                adh.deleteAreaFromServer(areaElement);
            }
        }

        if(areaIds.length > 0){
            Intent createIntent = new Intent(this, CreateAreaFoldersActivity.class);
            createIntent.putStringArrayListExtra("area_id_list", new ArrayList<>(Arrays.asList(areaIds)));
            createIntent.putExtra("synchronizing", "true");
            createIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(createIntent);
        }

        PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
        DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
        if(adh.getDirtyAreas().size() == 0
                && pdh.getDirtyPositions().size() == 0
                && ddh.getDirtyResources().size() == 0){
            GlobalContext.INSTANCE.put(GlobalContext.SYNCHRONIZING_OFFLINE, new Boolean(false).toString());
        }

    }

}