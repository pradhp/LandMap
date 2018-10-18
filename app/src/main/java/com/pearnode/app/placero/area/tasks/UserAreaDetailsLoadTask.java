package com.pearnode.app.placero.area.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.model.Address;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.model.AreaMeasure;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.drive.Resource;
import com.pearnode.app.placero.permission.PermissionElement;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.tags.TagsDBHelper;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.UserElement;
import com.pearnode.constants.APIRegistry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class UserAreaDetailsLoadTask extends AsyncTask<JSONObject, Void, String> {

    private Context localContext;
    private AreaDBHelper adh;
    private PositionsDBHelper pdh;
    private DriveDBHelper ddh;
    private PermissionsDBHelper pmh;
    private TagsDBHelper tdh;

    private AsyncTaskCallback callback;

    public UserAreaDetailsLoadTask(Context appContext) {
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
            String urlString = APIRegistry.USER_AREA_SEARCH + "?us=";
            JSONObject postDataParam = postDataParams[0];
            String searchKey = postDataParam.getString("us");
            URL url = new URL(urlString + URLEncoder.encode(searchKey, "utf-8"));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

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
    protected void onPostExecute(String s) {
        try {
            if(s == null){
                finalizeTaskCompletion();
                return;
            }
            JSONObject responseObj = new JSONObject(s);
            JSONArray areaResponse = responseObj.getJSONArray("area_response");
            for (int i = 0; i < areaResponse.length(); i++) {
                JSONObject areaResponseObj = (JSONObject) areaResponse.get(i);
                JSONObject areaObj = (JSONObject) areaResponseObj.get("area");

                Area ae = new Area();
                ae.setName(areaObj.getString("name"));
                ae.setCreatedBy(areaObj.getString("created_by"));
                ae.setDescription(areaObj.getString("description"));
                ae.getCenterPosition().setLat(areaObj.getDouble("center_lat"));
                ae.getCenterPosition().setLon(areaObj.getDouble("center_lon"));
                ae.setUniqueId(areaObj.getString("unique_id"));

                double msqFt = areaObj.getDouble("measure_sqft");
                AreaMeasure measure = new AreaMeasure(msqFt);
                ae.setMeasure(measure);

                String addressText = areaObj.getString("address");
                Address address = Address.fromStoredAddress(addressText);
                if (address != null) {
                    ae.setAddress(address);
                    tdh.insertTagsLocally(address.getTags(), "area", ae.getUniqueId());
                }
                ae.setType(areaObj.getString("type"));
                ae.setDirty(0);
                ae.setDirtyAction("none");
                adh.insertArea(ae);

                JSONArray positionsArr = (JSONArray) areaResponseObj.get("positions");
                for (int p = 0; p < positionsArr.length(); p++) {
                    JSONObject positionObj = (JSONObject) positionsArr.get(p);
                    Position pe = new Position();
                    pe.setUniqueId(positionObj.getString("unique_id"));
                    pe.setUniqueAreaId(positionObj.getString("unique_area_id"));
                    pe.setName(positionObj.getString("name"));
                    pe.setDescription(positionObj.getString("description"));
                    pe.setLat(positionObj.getDouble("lat"));
                    pe.setLon(positionObj.getDouble("lon"));
                    pe.setTags(positionObj.getString("tags"));
                    pe.setType(positionObj.getString("type"));
                    pe.setCreatedOnMillis(positionObj.getString("created_on"));

                    pdh.insertPositionFromServer(pe);
                }

                JSONArray driveArr = (JSONArray) areaResponseObj.get("drs");
                for (int d = 0; d < driveArr.length(); d++) {
                    JSONObject driveObj = (JSONObject) driveArr.get(d);
                    Resource resource = new Resource();
                    resource.setUniqueId(driveObj.getString("unique_id"));
                    resource.setAreaId(driveObj.getString("area_id"));
                    resource.setUserId(driveObj.getString("user_id"));
                    resource.setContainerId(driveObj.getString("container_id"));
                    resource.setResourceId(driveObj.getString("resource_id"));
                    resource.setName(driveObj.getString("name"));
                    resource.setType(driveObj.getString("type"));
                    resource.setSize(driveObj.getString("size"));
                    resource.setMimeType(driveObj.getString("mime_type"));
                    resource.setContentType(driveObj.getString("content_type"));
                    String positionId = driveObj.getString("position_id");
                    if (!positionId.equalsIgnoreCase("null")) {
                        resource.setPosition(pdh.getPositionById(positionId));
                    }
                    resource.setCreatedOnMillis(driveObj.getString("created_on"));
                    ddh.insertResourceFromServer(resource);
                }

                JSONArray permissionsArr = (JSONArray) areaResponseObj.get("permissions");
                for (int e = 0; e < permissionsArr.length(); e++) {
                    JSONObject permissionObj = (JSONObject) permissionsArr.get(e);
                    PermissionElement pe = new PermissionElement();
                    pe.setUserId(permissionObj.getString("user_id"));
                    pe.setAreaId(permissionObj.getString("area_id"));
                    pe.setFunctionCode(permissionObj.getString("function_code"));
                    pmh.insertPermissionLocally(pe);
                }
            }

            JSONArray commonResponse = responseObj.getJSONArray("common_response");
            for (int i = 0; i < commonResponse.length(); i++) {
                JSONObject driveObj = commonResponse.getJSONObject(i);
                Resource resource = new Resource();
                resource.setUniqueId(driveObj.getString("unique_id"));
                resource.setAreaId(driveObj.getString("area_id"));
                resource.setUserId(driveObj.getString("user_id"));
                resource.setContainerId(driveObj.getString("container_id"));
                resource.setResourceId(driveObj.getString("resource_id"));
                resource.setName(driveObj.getString("name"));
                resource.setType(driveObj.getString("type"));
                resource.setSize(driveObj.getString("size"));
                resource.setMimeType(driveObj.getString("mime_type"));
                resource.setContentType(driveObj.getString("content_type"));
                resource.setCreatedOnMillis(driveObj.getString("created_on"));

                ddh.insertResourceFromServer(resource);
            }

            UserElement userElement = UserContext.getInstance().getUserElement();
            tdh.insertTagsLocally(userElement.getSelections().getTags(), "user", userElement.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finalizeTaskCompletion();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        if (callback != null) {
            callback.taskCompleted("");
        }
    }
}
