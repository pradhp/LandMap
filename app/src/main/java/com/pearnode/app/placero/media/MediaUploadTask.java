package com.pearnode.app.placero.media;

import android.content.Context;
import android.os.AsyncTask;

import com.pearnode.common.TaskFinishedListener;
import com.pearnode.common.ftp.PictureFTPUploader;
import com.pearnode.common.ftp.ThumbnailFTPUploader;
import com.pearnode.common.ftp.VideoFTPUploader;

import java.io.File;

/**
 * Created by USER on 11/5/2015.
 */
public class MediaUploadTask extends AsyncTask<Object, String, String> {

    private final TaskFinishedListener finishedListener;
    private Context context = null;

    public MediaUploadTask(Context context, TaskFinishedListener listener) {
        this.finishedListener = listener;
        this.context = context;
    }

    @Override
    protected String doInBackground(Object[] params) {
        String result = "SUCCESS";
        try {
            String mediaType = (String) params[0];
            String mediaName = (String) params[1];
            File mediaFile = (File) params[2];
            if(mediaType.equalsIgnoreCase("picture")){
                PictureFTPUploader uploader = new PictureFTPUploader(mediaName, mediaFile);
                uploader.doUpload();
            }else if(mediaType.equalsIgnoreCase("thumbnail")){
                ThumbnailFTPUploader uploader = new ThumbnailFTPUploader(mediaName, mediaFile);
                uploader.doUpload();
            } else if(mediaType.equalsIgnoreCase("video")){
                VideoFTPUploader uploader = new VideoFTPUploader(mediaName, mediaFile);
                uploader.doUpload();
            }
        }catch (Exception e){
            result = "Failure [" + e.getMessage() + "]";
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(finishedListener != null){
            finishedListener.onTaskFinished(result); // Tell whoever was listening we have finished
        }
    }

}