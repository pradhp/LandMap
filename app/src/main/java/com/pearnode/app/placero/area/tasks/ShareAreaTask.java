package com.pearnode.app.placero.area.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.model.Address;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.google.geo.CommonGeoHelper;
import com.pearnode.app.placero.position.Position;
import com.pearnode.common.TaskFinishedListener;
import com.pearnode.common.URlUtils;
import com.pearnode.constants.APIRegistry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Rinky on 21-10-2017.
 */

public class ShareAreaTask extends AsyncTask<Object, Void, String> {

    private Context context;
    private TaskFinishedListener finishedListener;
    private Area area = null;

    public ShareAreaTask(Context context, TaskFinishedListener listener) {
        this.context = context;
        this.finishedListener = listener;
    }

    protected String doInBackground(Object... params) {
        try {
            area = (Area) params[0];
            URL url = new URL(APIRegistry.SHARE_AREA);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("area", area);

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
            return new String("Exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String result) {
        AreaDBHelper adh = new AreaDBHelper(context);
        if(result == null){
            if(area.getDirty() == 1){
                // Trying to create a dirty area on server. // Ignore this will be retried later.
            }else {
                area.setDirty(1);
                area.setDirtyAction("insert");
                adh.insertArea(area);
            }
        }else {
            // Area was created on server end.
            area.setDirty(0);
            area.setDirtyAction("none");
            adh.insertArea(area);
        }
        if(finishedListener != null){
            finishedListener.onTaskFinished(area.toString());
        }
    }

}
