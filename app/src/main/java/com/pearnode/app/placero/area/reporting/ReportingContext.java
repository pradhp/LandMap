package com.pearnode.app.placero.area.reporting;

import android.content.Context;

import java.io.File;
import java.util.List;

import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.sync.LocalFolderStructureManager;

/**
 * Created by USER on 10/24/2017.
 */
public class ReportingContext {

    public static final ReportingContext INSTANCE = new ReportingContext();

    private ReportingContext() {
    }

    private Area currentArea;
    private Context context;
    private Boolean generatingReport = false;

    public Area getAreaElement() {
        return this.currentArea;
    }

    public void setAreaElement(Area area, Context context) {
        this.currentArea = area;
        this.context = context;

        PositionsDBHelper pdb = new PositionsDBHelper(context);
        this.currentArea.setPositions(pdb.getPositionsForArea(this.currentArea));

        MediaDataBaseHandler ddh = new MediaDataBaseHandler(context);
        this.currentArea.setPictures(ddh.getPlacePictures(this.currentArea.getId()));
        this.currentArea.setVideos(ddh.getPlaceVideos(this.currentArea.getId()));
        this.currentArea.setDocuments(ddh.getPlaceDocuments(this.currentArea.getId()));

        PermissionsDBHelper pdh = new PermissionsDBHelper(context);
        currentArea.setPermissions(pdh.fetchPermissionsByAreaId(currentArea.getId()));
    }

    public File getAreaLocalImageRoot(String areaId) {
        String areaImageRoot = LocalFolderStructureManager.getImageStorageDir().getAbsolutePath()
                + File.separatorChar + areaId;
        File areaImageFolder = new File(areaImageRoot);
        if (areaImageFolder.exists()) {
            return areaImageFolder;
        } else {
            areaImageFolder.mkdirs();
        }
        return areaImageFolder;
    }

    public File getAreaLocalDocumentRoot(String areaId) {
        String areaDocumentsRoot = LocalFolderStructureManager.getDocumentsStorageDir().getAbsolutePath()
                + File.separatorChar + areaId;
        File areaDocumentsFolder = new File(areaDocumentsRoot);
        if (areaDocumentsFolder.exists()) {
            return areaDocumentsFolder;
        } else {
            areaDocumentsFolder.mkdirs();
        }
        return areaDocumentsFolder;
    }

    public Boolean getGeneratingReport() {
        return this.generatingReport;
    }

    public void setGeneratingReport(Boolean generatingReport) {
        this.generatingReport = generatingReport;
    }

    public Context getActivityContext() {
        return this.context;
    }

}
