package lm.pkp.com.landmap.weather;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.weather.db.WeatherDBHelper;
import lm.pkp.com.landmap.weather.model.WeatherElement;

/**
 * Created by USER on 11/19/2017.
 */
public class WeatherManager implements AsyncTaskCallback {

    private AsyncTaskCallback callback = null;
    private Context context = null;
    private PositionElement position = null;

    public WeatherManager(Context context, AsyncTaskCallback callback) {
        this.callback = callback;
        this.context = context;
    }

    public WeatherManager(Context context) {
        this.context = context;
    }

    public void loadWeatherInfoForPosition(PositionElement pe) {
        if ((pe.getLat() == 0.0) && (pe.getLon() == 0.0)) {
            return;
        }
        WeatherDBHelper wdh = new WeatherDBHelper(context);
        WeatherElement wbp = wdh.getWeatherByPosition(pe);
        if(wbp != null){
            position.setWeather(wbp);
            callback.taskCompleted(wbp);
            return;
        }
        PositionWeatherLoadAsyncTask task = new PositionWeatherLoadAsyncTask(context, pe, this);
        try {
            this.position = pe;
            JSONObject queryObj = new JSONObject();
            queryObj.put("latitude", pe.getLat() + "");
            queryObj.put("longitude", pe.getLon() + "");
            task.execute(queryObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void taskCompleted(Object result) {
        // Do the json parsing here and fill the WeatherELement
        WeatherElement weatherElement = null;
        if (result != null) {
            String response = result.toString();
            try {
                JSONObject responseObj = new JSONObject(response);
                JSONObject queryObj = responseObj.getJSONObject("query");
                int count = queryObj.getInt("count");
                if (count > 0) {
                    weatherElement = new WeatherElement();
                    JSONObject channelObj = queryObj.getJSONObject("results").getJSONObject("channel");

                    // Address
                    String title = channelObj.getString("title");
                    String[] titleSplit = title.split("-");
                    String address = titleSplit[1];
                    weatherElement.setAddress(address);

                    // Wind attributes
                    JSONObject windObj = channelObj.getJSONObject("wind");
                    weatherElement.setWindDirection(windObj.getString("direction"));
                    weatherElement.setWindChill(windObj.getString("chill"));
                    weatherElement.setWindSpeed(windObj.getString("speed"));

                    // Atmosphere attributes
                    JSONObject atmosphereObj = channelObj.getJSONObject("atmosphere");
                    weatherElement.setHumidity(atmosphereObj.getString("humidity"));
                    weatherElement.setVisibility(atmosphereObj.getString("visibility"));

                    // Item attributes
                    JSONObject conditionObj = channelObj.getJSONObject("item").getJSONObject("condition");
                    weatherElement.setConditionText(conditionObj.getString("text"));
                    weatherElement.setConditionCode(conditionObj.getString("code"));
                    weatherElement.setTemperature(conditionObj.getString("temp"));
                    weatherElement.setCreatedOn(System.currentTimeMillis() + "");

                    weatherElement.setPositionId(position.getUniqueId());
                    weatherElement.setUniqueId(UUID.randomUUID().toString());

                    WeatherDBHelper wdh = new WeatherDBHelper(context);
                    wdh.insertWeatherLocally(weatherElement);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        position.setWeather(weatherElement);
        if (callback != null) {
            callback.taskCompleted(weatherElement);
        }
    }
}
