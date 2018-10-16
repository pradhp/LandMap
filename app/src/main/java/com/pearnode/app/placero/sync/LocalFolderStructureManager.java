package com.pearnode.app.placero.sync;

import android.os.Environment;

import java.io.File;

import com.pearnode.app.placero.area.FileStorageConstants;

/**
 * Created by USER on 11/5/2017.
 */
public class LocalFolderStructureManager {

    private static File tempStorageDir;
    private static File docsStorageDir;
    private static File videoStorageDir;
    private static File imageStorageDir;

    public static void create() {
        createImagesFolder();
        createVideosFolder();
        createDocumentsFolder();
        createTempFolder();
    }

    private static void createTempFolder() {
        // External sdcard location
        tempStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                FileStorageConstants.TEMP_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!tempStorageDir.exists()) {
            if (!tempStorageDir.mkdirs()) {
            }
        }

    }

    private static void createDocumentsFolder() {
        // External sdcard location
        docsStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!docsStorageDir.exists()) {
            if (!docsStorageDir.mkdirs()) {
            }
        }
    }

    private static void createVideosFolder() {
        // External sdcard location
        videoStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                FileStorageConstants.VIDEO_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!videoStorageDir.exists()) {
            if (!videoStorageDir.mkdirs()) {
            }
        }
    }

    private static void createImagesFolder() {
        // External sdcard location
        imageStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                FileStorageConstants.IMAGE_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!imageStorageDir.exists()) {
            if (!imageStorageDir.mkdirs()) {
            }
        }
    }

    public static File getTempStorageDir() {
        return tempStorageDir;
    }

    public static File getDocsStorageDir() {
        return docsStorageDir;
    }

    public static File getVideoStorageDir() {
        return videoStorageDir;
    }

    public static File getImageStorageDir() {
        return imageStorageDir;
    }
}
