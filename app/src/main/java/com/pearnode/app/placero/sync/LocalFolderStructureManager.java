package com.pearnode.app.placero.sync;

import android.os.Environment;

import java.io.File;

import com.pearnode.app.placero.area.FileStorageConstants;

/**
 * Created by USER on 11/5/2017.
 */
public class LocalFolderStructureManager {

    private static File tempStorageDir;
    private static File imageStorageDir;
    private static File thumbnailStorageDir;
    private static File videoStorageDir;
    private static File docsStorageDir;

    public static void create() {
        createTempFolder();
        createThumbnailFolder();
        createImagesFolder();
        createVideosFolder();
        createDocsFolder();
    }

    private static void createTempFolder() {
        // External sdcard location
        tempStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                FileStorageConstants.TEMP_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!tempStorageDir.exists()) {
            if (!tempStorageDir.mkdirs()) {
                System.out.println("Temporary directory [" + tempStorageDir + "] could not be created");
            }else {
                System.out.println("Temporary directory [" + tempStorageDir + "] can be used");
            }
        }

    }


    private static void createImagesFolder() {
        // External sdcard location
        imageStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                FileStorageConstants.PICTURES_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!imageStorageDir.exists()) {
            if (!imageStorageDir.mkdirs()) {
                System.out.println("Images directory [" + imageStorageDir + "] could not be created");
            }else {
                System.out.println("Images directory [" + imageStorageDir + "] can be used");
            }
        }
    }

    private static void createThumbnailFolder() {
        // External sdcard location
        thumbnailStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                FileStorageConstants.THUMBNAIL_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!thumbnailStorageDir.exists()) {
            if (!thumbnailStorageDir.mkdirs()) {
                System.out.println("Thumbnails directory [" + thumbnailStorageDir + "] could not be created");
            }else {
                System.out.println("Thumbnails directory [" + thumbnailStorageDir + "] can be used");
            }
        }
    }

    private static void createVideosFolder() {
        // External sdcard location
        videoStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                FileStorageConstants.VIDEOS_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!videoStorageDir.exists()) {
            if (!videoStorageDir.mkdirs()) {
                System.out.println("Videos directory [" + videoStorageDir + "] could not be created");
            }else {
                System.out.println("Videos directory [" + videoStorageDir + "] can be used");
            }
        }
    }

    private static void createDocsFolder() {
        // External sdcard location
        docsStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                FileStorageConstants.DOCUMENTS_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!docsStorageDir.exists()) {
            if (!docsStorageDir.mkdirs()) {
                System.out.println("Documents directory [" + docsStorageDir.getAbsolutePath() + "] could not be created");
            }else {
                System.out.println("Documents directory [" + docsStorageDir.getAbsolutePath() + "] can be used");
            }
        }
    }

    public static File getTempStorageDir() {
        return tempStorageDir;
    }

    public static File getImageStorageDir() {
        return imageStorageDir;
    }

    public static File getThumbnailStorageDir() {
        return thumbnailStorageDir;
    }

    public static File getVideoStorageDir() {
        return videoStorageDir;
    }

    public static File getDocumentsStorageDir() {
        return docsStorageDir;
    }
}
