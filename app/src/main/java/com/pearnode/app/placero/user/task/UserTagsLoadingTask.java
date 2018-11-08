package com.pearnode.app.placero.user.task;

import android.content.Context;
import android.os.AsyncTask;

import com.pearnode.app.placero.tags.Tag;
import com.pearnode.app.placero.user.User;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.UserPersistableSelections;
import com.pearnode.common.TaskFinishedListener;
import com.pearnode.common.URlUtils;
import com.pearnode.constants.APIRegistry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Rinky on 21-10-2017.
 */

public class UserTagsLoadingTask extends AsyncTask<Object, Void, String> {

    private Context context;
    private TaskFinishedListener finishedListener;

    public UserTagsLoadingTask(Context context, TaskFinishedListener listener) {
        this.context = context;
        this.finishedListener = listener;
    }

    protected String doInBackground(Object... params) {
        try {
            User user  = UserContext.getInstance().getUser();
            URL url = new URL(APIRegistry.USER_TAGS_LOAD);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("user", user);

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
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if(result != null){
            try {
                User user = UserContext.getInstance().getUser();
                UserPersistableSelections selections = user.getSelections();
                List<Tag> userTagList = selections.getTags();

                JSONObject responseObj = new JSONObject(result);
                JSONArray tagsArr = responseObj.getJSONArray("data");
                for (int i = 0; i < tagsArr.length(); i++) {
                    JSONObject tagObj = (JSONObject) tagsArr.get(i);
                    Tag tag = new Tag();
                    tag.setId(tagObj.getLong("id"));
                    tag.setName(tagObj.getString("name"));
                    tag.setType(tagObj.getString("type"));
                    tag.setContext("user");
                    tag.setContextId(user.getEmail());
                    tag.setCreatedOn(System.currentTimeMillis());
                    userTagList.add(tag);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(finishedListener != null){
            finishedListener.onTaskFinished("");
        }
    }

}
