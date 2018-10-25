package com.pearnode.app.placero.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;

import com.iceteck.silicompressorr.SiliCompressor;
import com.pearnode.app.placero.media.model.Media;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;

import com.pearnode.app.placero.sync.LocalFolderStructureManager;

public class ThumbnailCreator {

    private final Context context;

    public ThumbnailCreator(Context context) {
        this.context = context;
    }

    public File createThumbnail(Media media){
        String type = media.getType();
        File resourceFile = new File(media.getRfPath());
        if(type.equalsIgnoreCase("video")){
            return createVideoThumbnail(resourceFile);
        }else if(type.equalsIgnoreCase("picture")){
            return createPictureThumbnail(resourceFile);
        }else if(type.equalsIgnoreCase("document")){
            return createDocumentThumbnail(resourceFile);
        }
        return null;
    }

    public File createPictureThumbnail(File resourceFile) {
        File thumbnailFile = null;
        Options bitmapOptions = new Options();
        bitmapOptions.inJustDecodeBounds = true; // obtain the size of the image, without loading it in memory

        // find the best scaling factor for the desired dimensions
        int desiredWidth = 300;
        int desiredHeight = 300;
        float widthScale = (float) bitmapOptions.outWidth / desiredWidth;
        float heightScale = (float) bitmapOptions.outHeight / desiredHeight;
        float scale = Math.min(widthScale, heightScale);

        int sampleSize = 1;
        while (sampleSize < scale) {
            sampleSize *= 2;
        }
        bitmapOptions.inSampleSize = sampleSize; // this value must be a power of 2,
        // this is why you can not have an image scaled as you would like
        bitmapOptions.inJustDecodeBounds = false; // now we want to search the image

        // Let's search just the part of the image necessary for creating the thumbBmap, not the whole image
        Bitmap thumbBmap = BitmapFactory.decodeFile(resourceFile.getAbsolutePath(), bitmapOptions);

        File thumbnailRoot = LocalFolderStructureManager.getThumbnailStorageDir();
        String thumbFilePath = thumbnailRoot.getAbsolutePath()
                + File.separatorChar + "TH_" + resourceFile.getName();
        // Save the thumbBmap
        FileOutputStream fos = null;
        try {
            thumbnailFile = new File(thumbFilePath);
            fos = new FileOutputStream(thumbnailFile);
            thumbBmap.compress(CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();

            thumbBmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thumbnailFile;
    }

    public File createVideoThumbnail(File resourceFile) {
        File thumbnailFile = null;
        FileOutputStream out = null;
        try {
            String filePath = resourceFile.getAbsolutePath();
            File resFile = new File(filePath);
            if(!resFile.exists()){
                return null;
            }

            Bitmap bMap = ThumbnailUtils.createVideoThumbnail(filePath,Thumbnails.MICRO_KIND);
            File thumbnailRoot = LocalFolderStructureManager.getThumbnailStorageDir();

            String fileName = resourceFile.getName();
            String trimmedName = FilenameUtils.removeExtension(fileName);

            String thumbFilePath = thumbnailRoot.getAbsolutePath()
                    + File.separatorChar + "TH_" + trimmedName + ".jpg";
            thumbnailFile = new File(thumbFilePath);
            out = new FileOutputStream(thumbFilePath);
            bMap.compress(CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            out.flush();
            out.close();

            bMap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thumbnailFile;
    }

    public File createDocumentThumbnail(File resourceFile) {
        File thumbnailFile = null;
        try {
            if (!PDFBoxResourceLoader.isReady()) {
                PDFBoxResourceLoader.init(this.context);
            }

            PDDocument document = PDDocument.load(resourceFile);
            PDFRenderer renderer = new PDFRenderer(document);
            Bitmap pageImage = renderer.renderImage(0, 1, Config.RGB_565);

            File thumbnailRoot = LocalFolderStructureManager.getThumbnailStorageDir();
            String thumbFilePath = thumbnailRoot.getAbsolutePath() + File.separatorChar + resourceFile.getName();
            thumbnailFile = new File(thumbFilePath);

            FileOutputStream fileOut = new FileOutputStream(thumbnailFile);
            pageImage.compress(CompressFormat.JPEG, 100, fileOut);

            fileOut.flush();
            fileOut.close();

            document.close();

            pageImage.recycle();
            SiliCompressor compressor = SiliCompressor.with(context);
            String compressedFilePath = compressor.compress(thumbnailFile.getAbsolutePath(),
                    LocalFolderStructureManager.getTempStorageDir(), true);
            File compressedFile = new File(compressedFilePath);
            FileUtils.copyFile(compressedFile, thumbnailFile);
            compressedFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return thumbnailFile;
    }
}
