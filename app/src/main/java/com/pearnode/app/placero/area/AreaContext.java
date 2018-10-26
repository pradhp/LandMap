package com.pearnode.app.placero.area;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.media.model.Media;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.sync.LocalFolderStructureManager;

/**
 * Created by USER on 10/24/2017.
 */
public class AreaContext {

    public static final AreaContext INSTANCE = new AreaContext();

    private AreaContext() {
    }

    private Area currentArea;
    private Context context;
    private Bitmap displayBMap;
    private List<Bitmap> viewBitmaps = new ArrayList<>();
    private final ArrayList<Media> mediaQueue = new ArrayList<>();

    public Area getAreaElement() {
        return this.currentArea;
    }

    public void setAreaElement(Area area, Context context) {
        clearContext();

        this.context = context;
        currentArea = area;
        mediaQueue.clear();

        PositionsDBHelper pdb = new PositionsDBHelper(context);
        currentArea.setPositions(pdb.getPositionsForArea(currentArea));
        deriveCenter(currentArea);

        PermissionsDBHelper pdh = new PermissionsDBHelper(context);
        currentArea.setPermissions(pdh.fetchPermissionsByAreaId(currentArea.getId()));
    }


    public void clearContext(){
        if(currentArea != null){
            currentArea.getPositions().clear();
            currentArea.getPictures().clear();
            currentArea.getVideos().clear();
            currentArea.getDocuments().clear();
            currentArea.getPermissions().clear();

            currentArea = null;
            context = null;
            mediaQueue.clear();

            if(displayBMap != null){
                displayBMap.recycle();
            }
            Iterator<Bitmap> iterator = viewBitmaps.iterator();
            while (iterator.hasNext()){
                Bitmap bitmap = iterator.next();
                if(bitmap != null){
                    bitmap.recycle();
                }
            }
            displayBMap = null;
            System.gc();
        }
    }

    public void deriveCenter(Area area) {
        double latSum = 0.0;
        double longSum = 0.0;

        double latAvg = 0.0;
        double lonAvg = 0.0;

        List<Position> positions = area.getPositions();
        int noOfPositions = positions.size();
        int boundaryCtr = 0;
        if (noOfPositions != 0) {
            for (int i = 0; i < noOfPositions; i++) {
                Position pe = positions.get(i);
                if(!pe.getType().equalsIgnoreCase("boundary")){
                    continue;
                }else {
                    boundaryCtr ++;
                }
                latSum += pe.getLat();
                longSum += pe.getLng();
            }
            if(boundaryCtr > 0){
                latAvg = latSum / boundaryCtr;
                lonAvg = longSum / boundaryCtr;
            }
        }
        Position centerPosition = area.getCenterPosition();
        centerPosition.setLat(latAvg);
        centerPosition.setLng(lonAvg);
    }

    // Drive specific resources.
    public void addMediaToQueue(Media dr) {
        this.mediaQueue.add(dr);
    }

    public List<Media> getUploadedQueue() {
        return this.mediaQueue;
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

    public File getAreaLocalPictureThumbnailRoot(String areaId) {
        String localImageRootPath = this.getAreaLocalImageRoot(areaId).getAbsolutePath();
        String pictureThumbnailRoot = localImageRootPath + File.separatorChar + "thumb" + File.separatorChar;
        File pictureThumbnailFolder = new File(pictureThumbnailRoot);
        if (pictureThumbnailFolder.exists()) {
            return pictureThumbnailFolder;
        } else {
            pictureThumbnailFolder.mkdirs();
        }
        return pictureThumbnailFolder;
    }

    public File getAreaLocalVideoThumbnailRoot(String areaId) {
        String localVideoRootPath = this.getAreaLocalVideoRoot(areaId).getAbsolutePath();
        String videoThumbnailRoot = localVideoRootPath + File.separatorChar + "thumb" + File.separatorChar;
        File videoThumbnailFolder = new File(videoThumbnailRoot);
        if (videoThumbnailFolder.exists()) {
            return videoThumbnailFolder;
        } else {
            videoThumbnailFolder.mkdirs();
        }
        return videoThumbnailFolder;
    }

    public File getAreaLocalDocumentThumbnailRoot(String areaId) {
        String localDocumentRootPath = this.getAreaLocalDocumentRoot(areaId).getAbsolutePath();
        String documentThumbnailRoot = localDocumentRootPath + File.separatorChar + "thumb" + File.separatorChar;
        File documentThumbnailFolder = new File(documentThumbnailRoot);
        if (documentThumbnailFolder.exists()) {
            return documentThumbnailFolder;
        } else {
            documentThumbnailFolder.mkdirs();
        }
        return documentThumbnailFolder;
    }

    public Bitmap getDisplayBMap() {
        return this.displayBMap;
    }

    public void setDisplayBMap(Bitmap displayBMap) {
        this.displayBMap = displayBMap;
    }

    public List<Bitmap> getViewBitmaps() {
        return this.viewBitmaps;
    }
}
