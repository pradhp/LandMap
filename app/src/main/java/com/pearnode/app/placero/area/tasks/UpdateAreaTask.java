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

public class UpdateAreaTask extends AsyncTask<Object, Void, String> {

    private Context context;
    private TaskFinishedListener finishedListener;
    private Area area = null;

    public UpdateAreaTask(Context context, TaskFinishedListener listener) {
        this.context = context;
        this.finishedListener = listener;
    }

    protected String doInBackground(Object... params) {
        try {
            area = (Area) params[0];
            URL url = new URL(APIRegistry.AREA_UPDATE);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            Address address = area.getAddress();
            if (address == null) {
                CommonGeoHelper geoHelper = CommonGeoHelper.INSTANCE;
                Position centerPosition = area.getCenterPosition();
                if(centerPosition != null){
                    Address areaAddress = geoHelper.getAddressByGeoLocation(context,
                            centerPosition.getLat(), centerPosition.getLng());
                    area.setAddress(areaAddress);
                }
            }

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
            area.setDirty(1);
            area.setDirtyAction("update");
        }else {
            area.setDirty(0);
            area.setDirtyAction("none");
        }
        adh.updateArea(area);
        if(finishedListener != null){
            finishedListener.onTaskFinished(area.toString());
        }
    }

}
