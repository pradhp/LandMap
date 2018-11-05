package com.pearnode.app.placero.area.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.model.Address;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.model.AreaMeasure;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.media.model.Media;
import com.pearnode.app.placero.permission.Permission;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.tags.TagsDBHelper;
import com.pearnode.app.placero.user.User;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.constants.APIRegistry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Rinky on 21-10-2017.
 */

public class SharedAreasLoadTask extends AsyncTask<JSONObject, Void, String> {

    private Context localContext;
    private AreaDBHelper adh;
    private PositionsDBHelper pdh;
    private PermissionsDBHelper pmh;
    private TagsDBHelper tdh;
    private MediaDataBaseHandler pmdh;

    private AsyncTaskCallback callback;

    public SharedAreasLoadTask(Context appContext) {
        localContext = appContext;
        adh = new AreaDBHelper(localContext);
        pdh = new PositionsDBHelper(localContext);
        pmh = new PermissionsDBHelper(localContext, null);
        tdh = new TagsDBHelper(localContext, null);
        pmdh = new MediaDataBaseHandler(localContext);
    }

    protected void onPreExecute() {
    }

    protected String doInBackground(JSONObject... postDataParams) {
        try {
            String urlString = APIRegistry.USER_SHARED_AREA_SEARCH;
            UserContext userContext = UserContext.getInstance();
            User user = userContext.getUser();
            URL url = new URL(urlString + "?us=" + user.getEmail());
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
            JSONObject responseObj = new JSONObject(s);
            JSONArray responseArr = responseObj.getJSONArray("data");
            for (int i = 0; i < responseArr.length(); i++) {
                JSONObject dataObj = (JSONObject) responseArr.get(i);
                JSONObject areaObj = (JSONObject) dataObj.get("detail");

                Area area = new Area();
                area.setId(areaObj.getString("id"));
                area.setName(areaObj.getString("name"));
                area.setDescription(areaObj.getString("description"));
                area.getCenterPosition().setLat(areaObj.getDouble("center_lat"));
                area.getCenterPosition().setLng(areaObj.getDouble("center_lon"));
                area.setCreatedBy(areaObj.getString("createdBy"));
                area.setDirty(0);
                area.setDirtyAction("none");
                area.setType("shared");

                AreaMeasure measure = new AreaMeasure(areaObj.getDouble("msqft"));
                area.setMeasure(measure);

                String addressText = areaObj.getString("address");
                Address address = Address.fromStoredAddress(addressText);
                if(address != null){
                    area.setAddress(address);
                    tdh.addTags(address.getTags(), "area", area.getId());
                }
                adh.insertArea(area);

                JSONArray positionsArr = (JSONArray) dataObj.get("positions");
                for (int p = 0; p < positionsArr.length(); p++) {
                    JSONObject positionObj = (JSONObject) positionsArr.get(p);
                    Position pe = new Position();
                    pe.setId((String) positionObj.get("id"));
                    pe.setAreaRef((String) positionObj.get("area_ref"));
                    pe.setName((String) positionObj.get("name"));
                    pe.setDescription((String) positionObj.get("description"));
                    pe.setLat(positionObj.getDouble("lat"));
                    pe.setLng(positionObj.getDouble("lng"));
                    pe.setTags((String) positionObj.get("tags"));
                    pe.setCreatedOn(positionObj.getString("created_on"));
                    pdh.addPostion(pe);
                }

                JSONArray mediaElements = dataObj.getJSONArray("medias");
                for (int d = 0; d < mediaElements.length(); d++) {
                    JSONObject mediaObj = (JSONObject) mediaElements.get(d);
                    Media media = new Media();
                    media.setPlaceRef(mediaObj.getString("place_ref"));
                    media.setName(mediaObj.getString("name"));
                    media.setType(mediaObj.getString("type"));
                    media.setTfName(mediaObj.getString("tf_name"));
                    media.setTfPath(mediaObj.getString("tf_path"));
                    media.setRfName(mediaObj.getString("rf_name"));
                    media.setRfPath(mediaObj.getString("rf_path"));
                    media.setLat(mediaObj.getString("lat"));
                    media.setLng(mediaObj.getString("lng"));
                    media.setCreatedOn(System.currentTimeMillis());
                    pmdh.addMedia(media);
                }

                JSONObject permissionObj = (JSONObject) dataObj.get("permission");
                Permission pe = new Permission();
                pe.setUserId(permissionObj.getString("user_id"));
                pe.setAreaId(permissionObj.getString("area_id"));
                pe.setFunctionCode(permissionObj.getString("function_code"));
                pmh.insertPermissionLocally(pe);
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
