package com.pearnode.app.placero.area.reporting;

import android.content.Context;

import java.io.File;
import java.util.List;

import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.FileStorageConstants;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.drive.Resource;
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
        this.reCenter(this.currentArea);

        DriveDBHelper ddh = new DriveDBHelper(context);
        this.currentArea.setResources(ddh.getDriveResourcesByAreaId(this.currentArea.getUniqueId()));

        PermissionsDBHelper pdh = new PermissionsDBHelper(context);
        currentArea.setPermissions(pdh.fetchPermissionsByAreaId(currentArea.getUniqueId()));
    }

    public void reCenter(Area area) {
        double latSum = 0.0;
        double longSum = 0.0;
        String positionId = null;

        double latAvg = 0.0;
        double lonAvg = 0.0;

        List<Position> positions = area.getPositions();
        int noOfPositions = positions.size();
        if (noOfPositions != 0) {
            for (int i = 0; i < noOfPositions; i++) {
                Position pe = positions.get(i);
                if (positionId == null) {
                    positionId = pe.getUniqueId();
                }
                latSum += pe.getLat();
                longSum += pe.getLng();
            }
            latAvg = latSum / noOfPositions;
            lonAvg = longSum / noOfPositions;
        }

        Position centerPosition = area.getCenterPosition();
        centerPosition.setLat(latAvg);
        centerPosition.setLng(lonAvg);
        centerPosition.setUniqueId(positionId);
    }

    private Resource documentsResourceRoot = null;
    public Resource getDocumentRootDriveResource() {
        if(documentsResourceRoot == null){
            DriveDBHelper ddh = new DriveDBHelper(context);
            documentsResourceRoot
                    = ddh.getDriveResourceRoot(FileStorageConstants.DOCUMENTS_CONTENT_TYPE, currentArea);
        }
        return documentsResourceRoot;
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

    public File getAreaLocalVideoRoot(String areaId) {
        String areaVideosRoot = LocalFolderStructureManager.getVideoStorageDir().getAbsolutePath()
                + File.separatorChar + areaId;
        File areaVideosFolder = new File(areaVideosRoot);
        if (areaVideosFolder.exists()) {
            return areaVideosFolder;
        } else {
            areaVideosFolder.mkdirs();
        }
        return areaVideosFolder;
    }

    public File getAreaLocalDocumentRoot(String areaId) {
        String areaDocumentsRoot = LocalFolderStructureManager.getDocsStorageDir().getAbsolutePath()
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
