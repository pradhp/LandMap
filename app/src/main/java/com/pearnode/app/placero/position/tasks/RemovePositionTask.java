package com.pearnode.app.placero.position.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionDatabaseHandler;
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

public class RemovePositionTask extends AsyncTask<Object, Void, String> {

    private Context context;
    private TaskFinishedListener finishedListener;
    private Position position = null;

    public RemovePositionTask(Context context, TaskFinishedListener listener) {
        this.context = context;
        this.finishedListener = listener;
    }

    protected String doInBackground(Object... params) {
        try {
            position = (Position) params[0];
            URL url = new URL(APIRegistry.POSITION_REMOVE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("position", position);

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
        PositionDatabaseHandler pdh = new PositionDatabaseHandler(context);
        if(result == null){
            if(position.getDirty() == 1){
                // Trying to create a dirty position on server. // Ignore this will be retried later.
            }else {
                position.setDirty(1);
                position.setDirtyAction("delete");
                pdh.deletePosition(position);
            }
        }else {
            // Area was created on server end.
            pdh.deletePosition(position);
        }
        if(finishedListener != null){
            finishedListener.onTaskFinished(position.toString());
        }
    }

}
