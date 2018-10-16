package com.pearnode.app.placero.connectivity.services;

import android.app.IntentService;
import android.content.Intent;

import java.util.ArrayList;

import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.custom.GlobalContext;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.position.PositionElement;
import com.pearnode.app.placero.position.PositionsDBHelper;

public class PositionSynchronizationService extends IntentService {

    public PositionSynchronizationService() {
        super(PositionSynchronizationService.class.getSimpleName());
    }

    public PositionSynchronizationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
        final ArrayList<PositionElement> dirtyPositions = pdh.getDirtyPositions();
        for (PositionElement position : dirtyPositions) {
            String dirtyAction = position.getDirtyAction();
            if (dirtyAction.equalsIgnoreCase("insert")) {
                if (pdh.insertPositionToServer(position)) {
                    position.setDirty(0);
                    pdh.updatePositionLocally(position);
                }
            } else if (dirtyAction.equalsIgnoreCase("update")) {
                if (pdh.updatePositionToServer(position)) {
                    position.setDirty(0);
                    pdh.updatePositionLocally(position);
                }
            } else if (dirtyAction.equalsIgnoreCase("delete")) {
                pdh.deletePositionLocally(position);
            }
        }

        AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
        DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
        if(adh.getDirtyAreas().size() == 0
                && pdh.getDirtyPositions().size() == 0
                && ddh.getDirtyResources().size() == 0){
            GlobalContext.INSTANCE.put(GlobalContext.SYNCHRONIZING_OFFLINE, new Boolean(false).toString());
        }

    }

}