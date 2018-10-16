package com.pearnode.app.placero.weather.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pearnode.app.placero.position.PositionElement;
import com.pearnode.app.placero.weather.model.WeatherElement;

public class WeatherDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "com.pearnode.app.placero.db";
    private static final String TABLE_NAME = "weather_master";

    private static final String WEATHER_COLUMN_UNIQUE_ID = "unique_id";
    private static final String WEATHER_POSITION_COLUMN_ID = "position_id";
    private static final String WEATHER_COLUMN_TEMPERATURE = "temperature";
    private static final String WEATHER_COLUMN_CONDITION_CODE = "condition_c";
    private static final String WEATHER_COLUMN_CONDITION_TEXT = "condition_t";
    private static final String WEATHER_COLUMN_ADDRESS = "address";
    private static final String WEATHER_COLUMN_WIND_CHILL = "wind_chill";
    private static final String WEATHER_COLUMN_WIND_DIRECTION = "wind_direction";
    private static final String WEATHER_COLUMN_WIND_SPEED = "wind_speed";
    private static final String WEATHER_COLUMN_HUMIDITY = "humidity";
    private static final String WEATHER_COLUMN_VISIBILITY = "visibility";
    private static final String WEATHER_COLUMN_CREATED_MILLIS = "created_on";

    public WeatherDBHelper(Context context) {
        super(context, WeatherDBHelper.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + WeatherDBHelper.TABLE_NAME + "(" +
                        WeatherDBHelper.WEATHER_COLUMN_UNIQUE_ID + " text," +
                        WeatherDBHelper.WEATHER_POSITION_COLUMN_ID + " text," +
                        WeatherDBHelper.WEATHER_COLUMN_TEMPERATURE + " text," +
                        WeatherDBHelper.WEATHER_COLUMN_CONDITION_CODE + " text, " +
                        WeatherDBHelper.WEATHER_COLUMN_CONDITION_TEXT + " text, " +
                        WeatherDBHelper.WEATHER_COLUMN_ADDRESS + " text," +
                        WeatherDBHelper.WEATHER_COLUMN_WIND_CHILL + " text," +
                        WeatherDBHelper.WEATHER_COLUMN_WIND_DIRECTION + " text," +
                        WeatherDBHelper.WEATHER_COLUMN_WIND_SPEED + " text," +
                        WeatherDBHelper.WEATHER_COLUMN_HUMIDITY + " text," +
                        WeatherDBHelper.WEATHER_COLUMN_VISIBILITY + " text," +
                        WeatherDBHelper.WEATHER_COLUMN_CREATED_MILLIS + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WeatherDBHelper.TABLE_NAME);
        this.onCreate(db);
    }

    public void insertWeatherLocally(WeatherElement we) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(WeatherDBHelper.WEATHER_COLUMN_UNIQUE_ID, we.getUniqueId());
        contentValues.put(WeatherDBHelper.WEATHER_POSITION_COLUMN_ID, we.getPositionId());
        contentValues.put(WeatherDBHelper.WEATHER_COLUMN_TEMPERATURE, we.getTemperature());
        contentValues.put(WeatherDBHelper.WEATHER_COLUMN_CONDITION_CODE, we.getConditionCode());
        contentValues.put(WeatherDBHelper.WEATHER_COLUMN_CONDITION_TEXT, we.getConditionText());
        contentValues.put(WeatherDBHelper.WEATHER_COLUMN_ADDRESS, we.getAddress());
        contentValues.put(WeatherDBHelper.WEATHER_COLUMN_WIND_CHILL, we.getWindChill());
        contentValues.put(WeatherDBHelper.WEATHER_COLUMN_WIND_DIRECTION, we.getWindDirection());
        contentValues.put(WeatherDBHelper.WEATHER_COLUMN_WIND_SPEED, we.getWindSpeed());
        contentValues.put(WeatherDBHelper.WEATHER_COLUMN_HUMIDITY, we.getHumidity());
        contentValues.put(WeatherDBHelper.WEATHER_COLUMN_VISIBILITY, we.getVisibility());
        contentValues.put(WeatherDBHelper.WEATHER_COLUMN_CREATED_MILLIS, we.getCreatedOn());

        db.insert(WeatherDBHelper.TABLE_NAME, null, contentValues);
        db.close();
    }

    public void deleteWeatherElement(WeatherElement we) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(WeatherDBHelper.TABLE_NAME, WeatherDBHelper.WEATHER_COLUMN_UNIQUE_ID + " = ? ", new String[]{we.getUniqueId()});
        db.close();
    }

    public void deleteWeatherByPosition(PositionElement pe) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + WeatherDBHelper.TABLE_NAME + " WHERE "
                + WeatherDBHelper.WEATHER_POSITION_COLUMN_ID + " = '" + pe.getUniqueId() + "'");
        db.close();
    }

    public WeatherElement getWeatherByPosition(PositionElement pe) {
        WeatherElement we = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + WeatherDBHelper.TABLE_NAME + " WHERE " + WeatherDBHelper.WEATHER_POSITION_COLUMN_ID + "=?",
                new String[]{pe.getUniqueId()});
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                we = new WeatherElement();
                we.setUniqueId(cursor.getString(cursor.getColumnIndex(WeatherDBHelper.WEATHER_COLUMN_UNIQUE_ID)));
                we.setAddress(cursor.getString(cursor.getColumnIndex(WeatherDBHelper.WEATHER_COLUMN_ADDRESS)));
                we.setConditionCode(cursor.getString(cursor.getColumnIndex(WeatherDBHelper.WEATHER_COLUMN_CONDITION_CODE)));
                we.setConditionText(cursor.getString(cursor.getColumnIndex(WeatherDBHelper.WEATHER_COLUMN_CONDITION_TEXT)));
                we.setHumidity(cursor.getString(cursor.getColumnIndex(WeatherDBHelper.WEATHER_COLUMN_HUMIDITY)));
                we.setTemperature(cursor.getString(cursor.getColumnIndex(WeatherDBHelper.WEATHER_COLUMN_TEMPERATURE)));
                we.setVisibility(cursor.getString(cursor.getColumnIndex(WeatherDBHelper.WEATHER_COLUMN_VISIBILITY)));
                we.setWindChill(cursor.getString(cursor.getColumnIndex(WeatherDBHelper.WEATHER_COLUMN_WIND_CHILL)));
                we.setWindDirection(cursor.getString(cursor.getColumnIndex(WeatherDBHelper.WEATHER_COLUMN_WIND_DIRECTION)));
                we.setWindSpeed(cursor.getString(cursor.getColumnIndex(WeatherDBHelper.WEATHER_COLUMN_WIND_SPEED)));
                we.setCreatedOn(cursor.getString(cursor.getColumnIndex(WeatherDBHelper.WEATHER_COLUMN_CREATED_MILLIS)));
                we.setPositionId(pe.getUniqueId());
                break;
            }
            cursor.close();
        }
        db.close();
        return we;
    }


    public void deleteWeatherElementsLocally() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(WeatherDBHelper.TABLE_NAME, "1", null);
        db.close();
    }

}