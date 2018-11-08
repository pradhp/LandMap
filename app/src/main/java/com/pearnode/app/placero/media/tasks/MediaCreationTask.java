package com.pearnode.app.placero.media.tasks;

import android.os.AsyncTask;

import com.pearnode.app.placero.media.model.Media;
import com.pearnode.common.TaskFinishedListener;
import com.pearnode.constants.APIRegistry;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by USER on 11/5/2015.
 */
public class MediaCreationTask extends AsyncTask<Object, String, String> {

    private TaskFinishedListener finishedListener;
    private Media media = null;

    public MediaCreationTask(TaskFinishedListener listener) {
        this.finishedListener = listener;
    }

    @Override
    protected String doInBackground(Object[] params) {
        media = (Media) params[0];
        return addMedia(media);
    }

    private String addMedia(Media media) {
        String response = null;
        HttpURLConnection conn = null;
        URL url = null;
        try {
            url = new URL(APIRegistry.MEDIA_CREATE);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            Map<String, Object> params = new HashMap<>();
            params.put("media", media);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer
                    = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            String dataStr = getPostDataString(params);
            writer.write(dataStr);

            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if(responseCode == 200){
                InputStream respStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(respStream));
                StringBuffer buffer = new StringBuffer();
                String line = null;
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                response = buffer.toString();
            }else {
                InputStream respStream = conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(respStream));
                StringBuffer buffer = new StringBuffer();
                String line = null;
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                response = buffer.toString();
            }
            conn.disconnect();
        } catch (Exception e) {
            return null;
        }
        return response;
    }

    public String getPostDataString(Map<String, Object> params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keySet().iterator();
        while(itr.hasNext()){
            String key= itr.next();
            Object value = params.get(key);
            if (first) {
                first = false;
            }else {
                result.append("&");
            }
            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        if(result == null){
            finishedListener.onTaskFinished(null); // Tell whoever was listening we have finished
        }
        if(finishedListener != null){
            finishedListener.onTaskFinished(result); // Tell whoever was listening we have finished
        }
    }

}