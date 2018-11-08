package com.pearnode.app.placero.connectivity.services;

import android.app.IntentService;
import android.content.Intent;

import java.util.ArrayList;

import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionDatabaseHandler;

public class PositionSynchronizationService extends IntentService {

    public PositionSynchronizationService() {
        super(PositionSynchronizationService.class.getSimpleName());
    }

    public PositionSynchronizationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PositionDatabaseHandler pdh = new PositionDatabaseHandler(getApplicationContext());
        final ArrayList<Position> dirtyPositions = pdh.getDirtyPositions();
        for (Position position : dirtyPositions) {
            String dirtyAction = position.getDirtyAction();
            if (dirtyAction.equalsIgnoreCase("insert")) {
            } else if (dirtyAction.equalsIgnoreCase("update")) {
            } else if (dirtyAction.equalsIgnoreCase("delete")) {
            }
        }
    }

}