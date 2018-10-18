package com.pearnode.app.placero.media;

import android.content.Context;
import android.os.AsyncTask;

import com.iceteck.silicompressorr.SiliCompressor;
import com.pearnode.app.placero.media.db.PlaceMediaDataBaseHandler;
import com.pearnode.app.placero.media.model.Media;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.sync.LocalFolderStructureManager;
import com.pearnode.common.TaskFinishedListener;
import com.pearnode.constants.FixedValuesRegistry;

import java.io.File;

/**
 * Created by USER on 11/5/2015.
 */
public class MediaHandlerTask extends AsyncTask<Object, String, String> {

    private final TaskFinishedListener finishedListener;
    private Context context = null;
    private Position pe = null;

    public MediaHandlerTask(Context appContext, Position pe, TaskFinishedListener listener) {
        this.finishedListener = listener;
        this.context = appContext;
        this.pe = pe;
    }

    @Override
    protected String doInBackground(Object[] params) {
        String result = "SUCCESS";
        try {
            String mediaType = (String) params[0];
            File mediaFile = (File) params[2];
            File thumbnailFile = (File) params[3];

            if(mediaType.equalsIgnoreCase("picture")){
                SiliCompressor compressor = SiliCompressor.with(context);
                String compressedFilePath = compressor.compress(mediaFile.getAbsolutePath(),
                        LocalFolderStructureManager.getImageStorageDir(), true);
                File compressedFile = new File(compressedFilePath);

                AsyncTask mediaUploadTask = new MediaUploadTask(context, null);
                mediaUploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        "picture", compressedFile.getName(), compressedFile);

                if(thumbnailFile != null && thumbnailFile.exists()){
                    AsyncTask thumbnailUploadTask = new MediaUploadTask(context, null);
                    thumbnailUploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            "thumbnail", thumbnailFile.getName(), thumbnailFile);
                }

                // Add the picture
                Media mediaP = new Media();
                mediaP.setPlaceRef(1L);
                mediaP.setName(compressedFile.getName());
                mediaP.setTfName(thumbnailFile.getName());
                mediaP.setTfPath(FixedValuesRegistry.MEDIA_ACCESS_URL + "/thumbnails/" + thumbnailFile.getName());
                mediaP.setRfName(compressedFile.getName());
                mediaP.setRfPath(FixedValuesRegistry.MEDIA_ACCESS_URL + "/pictures/" + compressedFile.getName());
                mediaP.setType("picture");
                mediaP.setLat(pe.getLat() + "");
                mediaP.setLng(pe.getLon() + "");
                mediaP.setCreatedOn(System.currentTimeMillis());
                mediaP.setFetchedOn(System.currentTimeMillis());

                OrgMediaCreateListener mediaCreateListenerP = new OrgMediaCreateListener();
                mediaCreateListenerP.setMedia(mediaP);

                OrgMediaCreationTask mediaCreationTaskP = new OrgMediaCreationTask(mediaCreateListenerP);
                mediaCreationTaskP.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediaP);

            } else if(mediaType.equalsIgnoreCase("video")){
                SiliCompressor compressor = SiliCompressor.with(context);
                try{
                    String compressedFilePath = compressor.compressVideo(mediaFile.getAbsolutePath(),
                            LocalFolderStructureManager.getVideoStorageDir().getAbsolutePath());

                    File compressedFile = new File(compressedFilePath);
                    AsyncTask videoUploadTask = new MediaUploadTask(context, null);
                    videoUploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            "video", compressedFile.getName(), compressedFile);

                    AsyncTask thumbnailUploadTask = new MediaUploadTask(context, null);
                    thumbnailUploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            "thumbnail", thumbnailFile.getName(), thumbnailFile);

                    // Add the video
                    Media mediaV = new Media();
                    mediaV.setPlaceRef(1L);
                    mediaV.setName(compressedFile.getName());
                    mediaV.setTfName(thumbnailFile.getName());
                    mediaV.setTfPath(FixedValuesRegistry.MEDIA_ACCESS_URL + "/thumbnails/" + thumbnailFile.getName());
                    mediaV.setRfName(compressedFile.getName());
                    mediaV.setRfPath(FixedValuesRegistry.MEDIA_ACCESS_URL + "/videos/" + compressedFile.getName());
                    mediaV.setType("video");
                    mediaV.setLat(pe.getLat() + "");
                    mediaV.setLng(pe.getLon() + "");
                    mediaV.setCreatedOn(System.currentTimeMillis());
                    mediaV.setFetchedOn(System.currentTimeMillis());

                    OrgMediaCreateListener mediaCreateListenerV = new OrgMediaCreateListener();
                    mediaCreateListenerV.setMedia(mediaV);

                    OrgMediaCreationTask mediaCreationTaskP = new OrgMediaCreationTask(mediaCreateListenerV);
                    mediaCreationTaskP.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediaV);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            result = "Failure [" + e.getMessage() + "]";
        }
        return result;
    }

    class OrgMediaCreateListener implements TaskFinishedListener {
        private Media media;
        public void setMedia(Media media) {
            this.media = media;
        }
        @Override
        public void onTaskFinished(String response) {
            PlaceMediaDataBaseHandler mdh = new PlaceMediaDataBaseHandler(context);
            mdh.addMedia(media);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(finishedListener != null){
            finishedListener.onTaskFinished(result); // Tell whoever was listening we have finished
        }
    }

}