package com.pearnode.app.placero.area.tasks;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.pearnode.app.placero.position.PositionElement;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.util.GeneralUtil;

/**
 * Created by Rinky on 21-10-2017.
 */

public class AreaPositionsLoadTask extends AsyncTask<JSONObject, Void, String> {

    private Context localContext;
    private PositionsDBHelper pdh;

    public AreaPositionsLoadTask(Context appContext) {
        localContext = appContext;
        this.pdh = new PositionsDBHelper(localContext);
    }

    protected void onPreExecute() {
    }

    protected String doInBackground(JSONObject... postDataParams) {
        try {
            String urlString = "http://"+ GeneralUtil.dbHost+"/lm/PositionsSearch.php";

            JSONObject postDataParam = postDataParams[0];
            String searchField = postDataParam.getString("sf_alt");
            String sfURL = "?sf_alt=" + searchField;
            String searchStr = postDataParam.getString("ss");
            String ssURL = "&ss=" + searchStr;

            URL url = new URL(urlString + sfURL + ssURL);

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

                PositionElement pe = new PositionElement();
                pe.setUniqueId((String) responseObj.get("unique_id"));
                pe.setUniqueAreaId((String) responseObj.get("unique_area_id"));
                pe.setName((String) responseObj.get("name"));
                pe.setDescription((String) responseObj.get("description"));
                pe.setLat(new Double((String) responseObj.get("lat")));
                pe.setLon(new Double((String) responseObj.get("lon")));
                pe.setTags((String) responseObj.get("tags"));

                this.pdh.insertPositionFromServer(pe);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(s);
    }
}
