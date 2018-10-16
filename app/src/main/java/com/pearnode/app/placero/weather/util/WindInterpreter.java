package com.pearnode.app.placero.weather.util;

/**
 * Created by USER on 11/19/2017.
 */
public class WindInterpreter {

    public static final String getDirectionFromBearing(double bearing) {

        if (bearing < 0 && bearing > -180) {
            // Normalize to [0,360]
            bearing = 360.0 + bearing;
        }

        if (bearing > 360 || bearing < -180) {
            return "Unknown";
        }

        String directions[] = {
                "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW",
                "N"};

        return directions[(int) Math.floor((bearing + 11.25) % 360 / 22.5)];
    }

}
