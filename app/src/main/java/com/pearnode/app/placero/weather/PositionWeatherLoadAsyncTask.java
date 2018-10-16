package com.pearnode.app.placero.weather;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.position.PositionElement;

/**
 * Created by USER on 11/19/2017.
 */
public class PositionWeatherLoadAsyncTask extends AsyncTask<JSONObject, Void, String> {

    private AsyncTaskCallback callback;
    private PositionElement position;
    private Context context;

    public PositionWeatherLoadAsyncTask(Context context, PositionElement position, AsyncTaskCallback callback) {
        this.position = position;
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected String doInBackground(JSONObject... params) {
        try {
            String urlString = "http://\"+ GeneralUtil.dbHost+\"/lm/FetchWeather.php";
            JSONObject postDataParam = params[0];
            String latitude = postDataParam.getString("latitude");
            String longitude = postDataParam.getString("longitude");
            URL url = new URL(urlString + "?latitude=" + latitude + "&longitude=" + longitude);

            Log.e("params", postDataParam.toString());

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
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (this.callback != null) {
            this.callback.taskCompleted(s);
        }
    }
}
