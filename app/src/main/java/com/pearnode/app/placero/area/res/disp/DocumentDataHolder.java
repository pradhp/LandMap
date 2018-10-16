package com.pearnode.app.placero.area.res.disp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.AreaElement;
import com.pearnode.app.placero.drive.DriveResource;

/**
 * Created by USER on 11/6/2017.
 */

final class DocumentDataHolder {

    public static final DocumentDataHolder INSTANCE = new DocumentDataHolder();

    public ArrayList<DocumentDisplayElement> getData() {
        ArrayList<DocumentDisplayElement> docItems = new ArrayList<>();
        AreaContext ac = AreaContext.INSTANCE;

        AreaElement ae = ac.getAreaElement();
        List<DriveResource> driveResources = ae.getMediaResources();
        String documentRootPath = ac.getAreaLocalDocumentRoot(ae.getUniqueId()).getAbsolutePath() + File.separatorChar;
        String thumbnailRoot = ac.getAreaLocalDocumentThumbnailRoot(ae.getUniqueId()).getAbsolutePath();

        for (int i = 0; i < driveResources.size(); i++) {
            DriveResource resource = driveResources.get(i);
            if (resource.getType().equals("file")) {
                if (resource.getContentType().equals("Document")) {
                    DocumentDisplayElement docDisplayElement = new DocumentDisplayElement();
                    docDisplayElement.setName(resource.getName());
                    docDisplayElement.setAbsPath(documentRootPath + resource.getName());
                    docDisplayElement.setResourceId(resource.getResourceId());
                    docDisplayElement.setThumbnailFile(new File(thumbnailRoot + File.separatorChar + resource.getName()));
                    docDisplayElement.setDocumentFile(new File(documentRootPath + File.separatorChar + resource.getName()));
                    docItems.add(docDisplayElement);
                }
            }
        }
        return docItems;
    }
}
