package com.pearnode.app.placero.connectivity.services;

import android.app.IntentService;
import android.content.Intent;

import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.tasks.CreateAreaTask;
import com.pearnode.app.placero.area.tasks.RemoveAreaTask;
import com.pearnode.app.placero.area.tasks.UpdateAreaTask;
import com.pearnode.app.placero.custom.GlobalContext;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.position.PositionsDBHelper;

import java.util.ArrayList;

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
        final ArrayList<Area> dirtyAreas = adh.getDirtyAreas();
        String[] areaIds = new String[dirtyAreas.size()];
        for (Area area : dirtyAreas) {
            String dirtyAction = area.getDirtyAction();
            if (dirtyAction.equalsIgnoreCase("insert")) {
                CreateAreaTask createAreaTask = new CreateAreaTask(getApplicationContext(), null);
                createAreaTask.execute(area);
            } else if (dirtyAction.equalsIgnoreCase("update")) {
                UpdateAreaTask updateAreaTask = new UpdateAreaTask(getApplicationContext(), null);
                updateAreaTask.execute(area);
            } else if (dirtyAction.equalsIgnoreCase("delete")) {
                RemoveAreaTask removeAreaTask = new RemoveAreaTask(getApplicationContext(), null);
                removeAreaTask.execute(area);
            }
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