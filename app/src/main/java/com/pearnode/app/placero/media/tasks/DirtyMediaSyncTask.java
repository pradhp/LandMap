package com.pearnode.app.placero.media.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.media.model.Media;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionDatabaseHandler;
import com.pearnode.common.TaskFinishedListener;
import com.pearnode.common.URlUtils;
import com.pearnode.constants.APIRegistry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Rinky on 21-10-2017.
 */

public class DirtyMediaSyncTask extends AsyncTask<Object, Void, String> {

    private Context context;
    private TaskFinishedListener finishedListener;
    private List<Media> dirtyMedias;

    public DirtyMediaSyncTask(Context context, TaskFinishedListener listener) {
        this.context = context;
        this.finishedListener = listener;
    }

    protected String doInBackground(Object... params) {
        try {
            MediaDataBaseHandler mdh = new MediaDataBaseHandler(context);
            dirtyMedias = mdh.getDirtyMedia();
            if(dirtyMedias.size() == 0){
                return "";
            }

            for (int i = 0; i < dirtyMedias.size(); i++) {
                Media dmedia = dirtyMedias.get(i);
                AsyncTask tUploadTask = new MediaUploadTask(context, null);
                // Upload the thumbnail
                File thumbnailFile = new File(dmedia.getTlPath());
                tUploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dmedia.getType(),
                        dmedia.getName(), thumbnailFile);

                // Upload the media file.
                AsyncTask mUploadTask = new MediaUploadTask(context, null);
                File dmediaFile = new File(dmedia.getRlPath());
                mUploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dmedia.getType(),
                        dmedia.getName(), dmediaFile);
            }

            URL url = new URL(APIRegistry.OFFLINE_MEDIA_SYNC);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("medias", dirtyMedias);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer
                    = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            String dataStr = URlUtils.getPostDataString(urlParams);
            writer.write(dataStr);

            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer sb = new StringBuffer("");
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                in.close();
                return sb.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if(result != null){
            MediaDataBaseHandler mdh = new MediaDataBaseHandler(context);
            try {
                JSONObject respObj = new JSONObject(result);
                JSONObject retAreas = respObj.getJSONObject("ret_obj");
                for (int i = 0; i < dirtyMedias.size(); i++) {
                    Media dirtyMedia = dirtyMedias.get(i);
                    String id = dirtyMedia.getId();
                    String medStatus = (String) retAreas.get(id);
                    if(medStatus != null && medStatus.equalsIgnoreCase("SUCCESS")){
                        dirtyMedia.setDirty(0);
                        dirtyMedia.setDirtyAction("");
                        mdh.updateMedia(dirtyMedia);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(finishedListener != null){
            finishedListener.onTaskFinished(result);
        }
    }
}
