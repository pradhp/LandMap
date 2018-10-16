package com.pearnode.app.placero.custom;

import java.util.ArrayList;

import com.pearnode.app.placero.area.FileStorageConstants;
import com.pearnode.app.placero.area.res.disp.FileDisplayElement;

/**
 * Created by USER on 11/7/2017.
 */
public class PermittedFileArrayList<E> extends ArrayList<E> {

    @Override
    public boolean add(E object) {
        if (object instanceof FileDisplayElement) {
            FileDisplayElement item = (FileDisplayElement) object;
            String itemPath = item.getPath();
            if (itemPath.contains(FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME)) {
                return false;
            }
            String itemName = item.getName();
            if (!itemName.contains(".")) {
                return super.add(object);
            }
            if (itemName.endsWith(".pdf")) {
                return super.add(object);
            }
            if (itemName.endsWith(".jpg")) {
                return super.add(object);
            }
            if (itemName.endsWith(".mp4")) {
                return super.add(object);
            }
        }
        return false;
    }
}
