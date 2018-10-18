package com.pearnode.app.placero.weather;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.pearnode.app.placero.R;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.weather.model.WeatherElement;
import com.pearnode.app.placero.weather.util.WindInterpreter;

/**
 * Created by USER on 11/20/2017.
 */
public class WeatherDisplayFragment extends DialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int)(metrics.widthPixels * 0.80);
        int height = (int)(metrics.heightPixels * 0.50);
        window.setLayout(width, height);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setGravity(Gravity.CENTER);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        Position centerPosition = AreaContext.INSTANCE.getAreaElement().getCenterPosition();
        WeatherElement weather = centerPosition.getWeather();

        TextView addressText = (TextView) rootView.findViewById(R.id.w_address);
        addressText.setText(weather.getAddress());

        TextView updatedText = (TextView) rootView.findViewById(R.id.w_updated);
        Long createdMillis = new Long(weather.getCreatedOn());
        CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(createdMillis,
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
        updatedText.setText(timeSpan.toString());

        TextView weatherIcon = (TextView) rootView.findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/weathericons-regular-webfont.ttf"));
        weatherIcon.setText(this.getWeatherIcon(new Integer(weather.getConditionCode())));

        TextView condition = (TextView) rootView.findViewById(R.id.w_condition);
        condition.setText(weather.getConditionText());

        TextView temperature = (TextView) rootView.findViewById(R.id.w_temperature);
        temperature.setText("Temperature " + this.convertFarenheitToCelcius(weather.getTemperature()) + " \u2103");

        TextView humidity = (TextView) rootView.findViewById(R.id.w_humidity);
        humidity.setText("Humidity " + weather.getHumidity() + " %");

        TextView windDetails = (TextView) rootView.findViewById(R.id.w_wind_details);
        String windChill = this.convertFarenheitToCelcius(weather.getWindChill());
        String windDirection
                = WindInterpreter.getDirectionFromBearing(Double.parseDouble(weather.getWindDirection()));
        String windSpeed = this.convertMilesToKms(weather.getWindSpeed());
        String windText = "Wind @" + windSpeed + " km/hr, "
                + windDirection + ", " + windChill + " \u2103";
        windDetails.setText(windText);

        rootView.findViewById(R.id.close_dialog).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return rootView;
    }

    public String convertFarenheitToCelcius(String celcius) {
        int celInt = Integer.parseInt(celcius);
        double farenheit = (celInt - 32) * 5 / 9;
        return farenheit + "";
    }

    public String convertMilesToKms(String miles) {
        float milesFloat = Float.parseFloat(miles);
        double kilometers = milesFloat * 1.609344;
        return String.format("%.2f", kilometers);
    }

    private String getWeatherIcon(int code) {
        String prefix = "wi_yahoo_";
        String iconText = prefix + code;
        int resId = this.getResources().getIdentifier(iconText, "string", this.getActivity().getPackageName());
        return this.getActivity().getString(resId);
    }
}
