package com.pearnode.app.placero.area.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.JsonObject;
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
            JSONArray dataArr = responseObj.getJSONArray("data");
            for (int i = 0; i < dataArr.length(); i++) {
                JSONObject dataObj = (JSONObject) dataArr.get(i);
                JSONObject detailObj = dataObj.getJSONObject("detail");

                Area ae = new Area();
                ae.setName(detailObj.getString("name"));
                ae.setCreatedBy(detailObj.getString("createdBy"));
                ae.setDescription(detailObj.getString("description"));
                ae.getCenterPosition().setLat(detailObj.getDouble("center_lat"));
                ae.getCenterPosition().setLng(detailObj.getDouble("center_lon"));
                ae.setUniqueId(detailObj.getString("uniqueId"));

                double msqFt = detailObj.getDouble("msqft");
                AreaMeasure measure = new AreaMeasure(msqFt);
                ae.setMeasure(measure);

                String addressText = detailObj.getString("address");
                Address address = Address.fromStoredAddress(addressText);
                if (address != null) {
                    ae.setAddress(address);
                    tdh.insertTagsLocally(address.getTags(), "area", ae.getUniqueId());
                }
                ae.setType(detailObj.getString("type"));
                ae.setDirty(0);
                ae.setDirtyAction("none");
                adh.insertArea(ae);

                JSONObject permissionObj = dataObj.getJSONObject("permission");
                PermissionElement permissionElement = new PermissionElement();
                permissionElement.setUserId(permissionObj.getString("user_id"));
                permissionElement.setAreaId(permissionObj.getString("area_id"));
                permissionElement.setFunctionCode(permissionObj.getString("function_code"));
                pmh.insertPermissionLocally(permissionElement);

                JSONArray positions = dataObj.getJSONArray("positions");
                for (int p = 0; p < positions.length(); p++) {
                    JSONObject positionObj = (JSONObject) positions.get(p);
                    Position position = new Position();
                    position.setUniqueId(positionObj.getString("unique_id"));
                    position.setUniqueAreaId(positionObj.getString("unique_area_id"));
                    position.setName(positionObj.getString("name"));
                    position.setDescription(positionObj.getString("description"));
                    position.setLat(positionObj.getDouble("lat"));
                    position.setLng(positionObj.getDouble("lon"));
                    position.setTags(positionObj.getString("tags"));
                    position.setType(positionObj.getString("type"));
                    position.setCreatedOnMillis(positionObj.getString("created_on"));

                    pdh.insertPositionFromServer(position);
                }

                JSONArray resources = dataObj.getJSONArray("resources");
                for (int d = 0; d < resources.length(); d++) {
                    JSONObject resourceObj = (JSONObject) resources.get(d);
                    Resource resource = new Resource();
                    resource.setUniqueId(resourceObj.getString("unique_id"));
                    resource.setAreaId(resourceObj.getString("area_id"));
                    resource.setUserId(resourceObj.getString("user_id"));
                    resource.setContainerId(resourceObj.getString("container_id"));
                    resource.setResourceId(resourceObj.getString("resource_id"));
                    resource.setName(resourceObj.getString("name"));
                    resource.setType(resourceObj.getString("type"));
                    resource.setSize(resourceObj.getString("size"));
                    resource.setMimeType(resourceObj.getString("mime_type"));
                    resource.setContentType(resourceObj.getString("content_type"));
                    String positionId = resourceObj.getString("position_id");
                    if (!positionId.equalsIgnoreCase("null")) {
                        resource.setPosition(pdh.getPositionById(positionId));
                    }
                    resource.setCreatedOnMillis(resourceObj.getString("created_on"));
                    ddh.insertResourceFromServer(resource);
                }
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
