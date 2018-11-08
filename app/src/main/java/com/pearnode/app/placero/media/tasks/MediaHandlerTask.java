package com.pearnode.app.placero.media.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.iceteck.silicompressorr.SiliCompressor;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.custom.ThumbnailCreator;
import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.media.model.Media;
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

    public MediaHandlerTask(Context appContext, TaskFinishedListener listener) {
        this.finishedListener = listener;
        this.context = appContext;
    }

    @Override
    protected String doInBackground(Object[] params) {
        String result = "SUCCESS";
        try {
            Media media = (Media) params[0];
            String mediaType = media.getType();
            File mediaFile = new File(media.getRfPath());
            Area area = AreaContext.INSTANCE.getArea();

            SiliCompressor compressor = SiliCompressor.with(context);

            ThumbnailCreator thumbnailCreator = new ThumbnailCreator(context);
            File tnFile = thumbnailCreator.createThumbnail(media);
            File tnRoot = LocalFolderStructureManager.getThumbnailStorageDir();
            String ctnPath = compressor.compress(tnFile.getAbsolutePath(), tnRoot, true);
            File ctnFile = new File(ctnPath);

            AsyncTask thumbnailUploadTask = new MediaUploadTask(context, null);
            thumbnailUploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    "thumbnail", ctnFile.getName(), ctnFile);

            if(mediaType.equalsIgnoreCase("picture")){
                String compressedFilePath = compressor.compress(mediaFile.getAbsolutePath(),
                        LocalFolderStructureManager.getImageStorageDir(), true);
                File compressedPictureFile = new File(compressedFilePath);

                AsyncTask mediaUploadTask = new MediaUploadTask(context, null);
                mediaUploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        "picture", compressedPictureFile.getName(), compressedPictureFile);

                // Add the picture
                Media mediaP = new Media();
                mediaP.setPlaceRef(area.getId());
                mediaP.setName(compressedPictureFile.getName());
                mediaP.setTfName(ctnFile.getName());
                mediaP.setTfPath(FixedValuesRegistry.MEDIA_ACCESS_URL + "/thumbnails/" + ctnFile.getName());
                mediaP.setTlPath(ctnFile.getAbsolutePath());
                mediaP.setRfName(compressedPictureFile.getName());
                mediaP.setRfPath(FixedValuesRegistry.MEDIA_ACCESS_URL + "/pictures/" + compressedPictureFile.getName());
                mediaP.setRlPath(compressedPictureFile.getAbsolutePath());
                mediaP.setType("picture");
                mediaP.setLat(media.getLat());
                mediaP.setLng(media.getLng());
                mediaP.setCreatedOn(System.currentTimeMillis());
                mediaP.setFetchedOn(System.currentTimeMillis());

                MediaCreateListener mediaCreateListenerP = new MediaCreateListener();
                mediaCreateListenerP.setMedia(mediaP);

                MediaCreationTask mediaCreationTaskP = new MediaCreationTask(mediaCreateListenerP);
                mediaCreationTaskP.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediaP);
            } else if(mediaType.equalsIgnoreCase("video")){
                try{
                    String compressedFilePath = compressor.compressVideo(mediaFile.getAbsolutePath(),
                            LocalFolderStructureManager.getVideoStorageDir().getAbsolutePath());

                    File compressedVideoFile = new File(compressedFilePath);
                    AsyncTask videoUploadTask = new MediaUploadTask(context, null);
                    videoUploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            "video", compressedVideoFile.getName(), compressedVideoFile);

                    // Add the video
                    Media mediaV = new Media();
                    mediaV.setPlaceRef(area.getId());
                    mediaV.setName(compressedVideoFile.getName());
                    mediaV.setTfName(ctnFile.getName());
                    mediaV.setTlPath(ctnFile.getAbsolutePath());
                    mediaV.setTfPath(FixedValuesRegistry.MEDIA_ACCESS_URL + "/thumbnails/" + ctnFile.getName());
                    mediaV.setRfName(compressedVideoFile.getName());
                    mediaV.setRfPath(FixedValuesRegistry.MEDIA_ACCESS_URL + "/videos/" + compressedVideoFile.getName());
                    mediaV.setRlPath(compressedVideoFile.getAbsolutePath());
                    mediaV.setType("video");
                    mediaV.setLat(media.getLat());
                    mediaV.setLng(media.getLng());
                    mediaV.setCreatedOn(System.currentTimeMillis());
                    mediaV.setFetchedOn(System.currentTimeMillis());

                    MediaCreateListener mediaCreateListenerV = new MediaCreateListener();
                    mediaCreateListenerV.setMedia(mediaV);

                    MediaCreationTask mediaCreationTaskV = new MediaCreationTask(mediaCreateListenerV);
                    mediaCreationTaskV.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediaV);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }else if(mediaType.equalsIgnoreCase("document")){
                try{
                    AsyncTask documentUploadTask = new MediaUploadTask(context, null);
                    documentUploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            "document", media.getName(), mediaFile);

                    // Add the video
                    Media mediaD = new Media();
                    mediaD.setPlaceRef(area.getId());
                    mediaD.setName(mediaFile.getName());
                    mediaD.setTfName(ctnFile.getName());
                    mediaD.setTfPath(FixedValuesRegistry.MEDIA_ACCESS_URL + "/thumbnails/" + ctnFile.getName());
                    mediaD.setTlPath(ctnFile.getAbsolutePath());
                    mediaD.setRfName(mediaFile.getName());
                    mediaD.setRfPath(FixedValuesRegistry.MEDIA_ACCESS_URL + "/documents/" + mediaFile.getName());
                    mediaD.setRlPath(mediaFile.getAbsolutePath());
                    mediaD.setType("document");
                    mediaD.setLat(media.getLat());
                    mediaD.setLng(media.getLng());
                    mediaD.setCreatedOn(System.currentTimeMillis());
                    mediaD.setFetchedOn(System.currentTimeMillis());

                    MediaCreateListener mediaCreateListenerD = new MediaCreateListener();
                    mediaCreateListenerD.setMedia(mediaD);

                    MediaCreationTask mediaCreationTaskD = new MediaCreationTask(mediaCreateListenerD);
                    mediaCreationTaskD.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediaD);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            result = "Failure [" + e.getMessage() + "]";
        }
        return result;
    }

    class MediaCreateListener implements TaskFinishedListener {
        private Media media;
        public void setMedia(Media media) {
            this.media = media;
        }
        @Override
        public void onTaskFinished(String response) {
            if(response == null){
                media.setDirty(1);
                media.setDirtyAction("insert");
            }
            MediaDataBaseHandler mdh = new MediaDataBaseHandler(context);
            mdh.addMedia(media);

            Area area = AreaContext.INSTANCE.getArea();
            if(media.getType().equals("picture")){
                area.getPictures().add(media);
            }else if(media.getType().equals("video")){
                area.getVideos().add(media);
            }else if(media.getType().equals("document")){
                area.getDocuments().add(media);
            }
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