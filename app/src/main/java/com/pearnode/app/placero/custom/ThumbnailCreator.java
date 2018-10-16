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
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;

import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.sync.LocalFolderStructureManager;

public class ThumbnailCreator {

    private final Context context;

    public ThumbnailCreator(Context context) {
        this.context = context;
    }

    public void createImageThumbnail(File resourceFile, String areaId) {
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
        bitmapOptions.inJustDecodeBounds = false; // now we want to load the image

        // Let's load just the part of the image necessary for creating the thumbBmap, not the whole image
        Bitmap thumbBmap = BitmapFactory.decodeFile(resourceFile.getAbsolutePath(), bitmapOptions);

        File thumbnailRoot = AreaContext.INSTANCE
                .getAreaLocalPictureThumbnailRoot(areaId);
        String thumbFilePath = thumbnailRoot.getAbsolutePath()
                + File.separatorChar + resourceFile.getName();
        // Save the thumbBmap
        FileOutputStream fos = null;
        try {
            File thumbnailFile = new File(thumbFilePath);
            if (thumbnailFile.exists()) {
                thumbnailFile.delete();
            }
            thumbnailFile.createNewFile();
            fos = new FileOutputStream(thumbnailFile);
            thumbBmap.compress(CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();

            thumbBmap.recycle();
            thumbBmap = null;

            SiliCompressor compressor = SiliCompressor.with(context);
            String compressedFilePath = compressor.compress(thumbnailFile.getAbsolutePath(),
                    LocalFolderStructureManager.getTempStorageDir(), true);
            File compressedFile = new File(compressedFilePath);
            FileUtils.copyFile(compressedFile, thumbnailFile);
            compressedFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createVideoThumbnail(File resourceFile, String areaId) {
        FileOutputStream out = null;
        try {
            String filePath = resourceFile.getAbsolutePath();
            File resFile = new File(filePath);
            if(!resFile.exists()){
                return;
            }
            Bitmap bMap = ThumbnailUtils.createVideoThumbnail(filePath,Thumbnails.MICRO_KIND);
            File thumbnailRoot = AreaContext.INSTANCE.getAreaLocalVideoThumbnailRoot(areaId);
            String thumbFilePath = thumbnailRoot.getAbsolutePath()
                    + File.separatorChar + resourceFile.getName();

            File thumbnailFile = new File(thumbFilePath);
            if (thumbnailFile.exists()) {
                thumbnailFile.delete();
            }
            thumbnailFile.createNewFile();

            out = new FileOutputStream(thumbFilePath);
            bMap.compress(CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            out.flush();
            out.close();

            bMap.recycle();
            bMap = null;

            SiliCompressor compressor = SiliCompressor.with(context);
            String compressedFilePath = compressor.compress(thumbnailFile.getAbsolutePath(),
                    LocalFolderStructureManager.getTempStorageDir(), true);
            File compressedFile = new File(compressedFilePath);
            FileUtils.copyFile(compressedFile, thumbnailFile);
            compressedFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createDocumentThumbnail(File resourceFile, String areaId) {
        try {
            if (!PDFBoxResourceLoader.isReady()) {
                PDFBoxResourceLoader.init(this.context);
            }

            PDDocument document = PDDocument.load(resourceFile);
            PDFRenderer renderer = new PDFRenderer(document);
            Bitmap pageImage = renderer.renderImage(0, 1, Config.RGB_565);

            String thumbRoot = AreaContext.INSTANCE
                    .getAreaLocalDocumentThumbnailRoot(areaId).getAbsolutePath();
            String thumbFilePath = thumbRoot + File.separatorChar + resourceFile.getName();
            File thumbFile = new File(thumbFilePath);
            if (!thumbFile.exists()) {
                thumbFile.createNewFile();
            }

            FileOutputStream fileOut = new FileOutputStream(thumbFile);
            pageImage.compress(CompressFormat.JPEG, 100, fileOut);

            fileOut.flush();
            fileOut.close();

            renderer = null;
            document.close();
            document = null;

            pageImage.recycle();
            SiliCompressor compressor = SiliCompressor.with(context);
            String compressedFilePath = compressor.compress(thumbFile.getAbsolutePath(),
                    LocalFolderStructureManager.getTempStorageDir(), true);
            File compressedFile = new File(compressedFilePath);
            FileUtils.copyFile(compressedFile, thumbFile);
            compressedFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
