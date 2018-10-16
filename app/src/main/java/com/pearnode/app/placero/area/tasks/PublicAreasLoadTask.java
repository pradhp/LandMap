package com.pearnode.app.placero.area.tasks;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import com.pearnode.app.placero.area.model.AreaAddress;
import com.pearnode.app.placero.area.model.AreaElement;
import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.model.AreaMeasure;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.drive.DriveResource;
import com.pearnode.app.placero.permission.PermissionElement;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.position.PositionElement;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.tags.TagsDBHelper;
import com.pearnode.app.placero.util.GeneralUtil;

/**
 * Created by Rinky on 21-10-2017.
 */

public class PublicAreasLoadTask extends AsyncTask<JSONObject, Void, String> {

    private Context localContext;
    private AreaDBHelper adh;
    private PositionsDBHelper pdh;
    private DriveDBHelper ddh;
    private PermissionsDBHelper pmh;
    private TagsDBHelper tdh;

    private AsyncTaskCallback callback;

    public PublicAreasLoadTask(Context appContext) {
        localContext = appContext;
        adh = new AreaDBHelper(localContext);
        pdh = new PositionsDBHelper(localContext);
        ddh = new DriveDBHelper(localContext);
        pmh = new PermissionsDBHelper(localContext, null);
        tdh = new TagsDBHelper(localContext, null);
    }

    protected void onPreExecute() {
    }

    protected String doInBackground(JSONObject... postDataParams) {
        try {
            String urlString = "http://"+ GeneralUtil.dbHost+"/lm/AreaPublicSearch.php";
            URL url = null;
            if (postDataParams.length > 0) {
                JSONObject postDataParam = postDataParams[0];
                String searchKey = postDataParam.getString("sk");
                url = new URL(urlString + "?sk=" + URLEncoder.encode(searchKey, "utf-8"));
            } else {
                url = new URL(urlString);
            }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new
                        InputStreamReader(
                        conn.getInputStream()));

                StringBuffer sb = new StringBuffer("");
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                in.close();
                return sb.toString();
            } else {
                return new String("false : " + responseCode);
            }
        } catch (Exception e) {
            return new String("Exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            JSONArray responseArr = new JSONArray(s);
            for (int i = 0; i < responseArr.length(); i++) {
                JSONObject responseObj = (JSONObject) responseArr.get(i);

                JSONObject areaObj = (JSONObject) responseObj.get("area");

                AreaElement ae = new AreaElement();
                ae.setName(areaObj.getString("name"));
                ae.setCreatedBy(areaObj.getString("created_by"));
                ae.setDescription(areaObj.getString("description"));
                ae.getCenterPosition().setLat(areaObj.getDouble("center_lat"));
                ae.getCenterPosition().setLon(areaObj.getDouble("center_lon"));
                ae.setUniqueId(areaObj.getString("unique_id"));
                AreaMeasure measure = new AreaMeasure(areaObj.getDouble("measure_sqft"));
                ae.setMeasure(measure);

                String addressText = areaObj.getString("address");
                AreaAddress areaAddress = AreaAddress.fromStoredAddress(addressText);
                if(areaAddress != null){
                    ae.setAddress(areaAddress);
                    tdh.insertTagsLocally(areaAddress.getTags(), "area", ae.getUniqueId());
                }

                ae.setType(areaObj.getString("type"));
                this.adh.insertAreaFromServer(ae);

                JSONArray positionsArr = (JSONArray) responseObj.get("positions");
                for (int p = 0; p < positionsArr.length(); p++) {
                    JSONObject positionObj = (JSONObject) positionsArr.get(p);

                    PositionElement pe = new PositionElement();
                    pe.setUniqueId((String) positionObj.get("unique_id"));
                    pe.setUniqueAreaId((String) positionObj.get("unique_area_id"));
                    pe.setName((String) positionObj.get("name"));
                    pe.setDescription((String) positionObj.get("description"));
                    pe.setLat(positionObj.getDouble("lat"));
                    pe.setLon(positionObj.getDouble("lon"));
                    pe.setTags((String) positionObj.get("tags"));
                    pe.setCreatedOnMillis(positionObj.getString("created_on"));

                    pdh.insertPositionFromServer(pe);
                }

                JSONArray driveArr = (JSONArray) responseObj.get("drs");
                for (int d = 0; d < driveArr.length(); d++) {
                    JSONObject driveObj = (JSONObject) driveArr.get(d);

                    DriveResource dr = new DriveResource();
                    dr.setUniqueId(driveObj.getString("unique_id"));
                    dr.setAreaId(driveObj.getString("area_id"));
                    dr.setUserId(driveObj.getString("user_id"));
                    dr.setContainerId(driveObj.getString("container_id"));
                    dr.setResourceId(driveObj.getString("resource_id"));
                    dr.setName(driveObj.getString("name"));
                    dr.setType(driveObj.getString("type"));
                    dr.setSize(driveObj.getString("size"));
                    dr.setMimeType(driveObj.getString("mime_type"));
                    dr.setContentType(driveObj.getString("content_type"));

                    PositionElement position = new PositionElement();
                    try{
                        position.setLat(driveObj.getDouble("latitude"));
                        position.setLon(driveObj.getDouble("longitude"));
                        position.setUniqueId(UUID.randomUUID().toString());
                    }catch (Exception e){
                    }
                    dr.setPosition(position);

                    dr.setCreatedOnMillis(driveObj.getString("created_on"));

                    ddh.insertResourceFromServer(dr);
                }

                JSONArray permissionsArr = (JSONArray) responseObj.get("permissions");
                for (int e = 0; e < permissionsArr.length(); e++) {
                    JSONObject permissionObj = (JSONObject) permissionsArr.get(e);

                    PermissionElement pe = new PermissionElement();
                    pe.setUserId(permissionObj.getString("user_id"));
                    pe.setAreaId(permissionObj.getString("area_id"));
                    pe.setFunctionCode(permissionObj.getString("function_code"));

                    pmh.insertPermissionLocally(pe);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finalizeTaskCompletion();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        this.callback.taskCompleted("");
    }
}
