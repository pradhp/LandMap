/**
 *
 */
package com.pearnode.app.placero.media.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pearnode.app.placero.media.model.Media;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author pradipta
 */
public class PlaceMediaDataBaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "com.pearnode.app.placero.db";

    private static final String TABLE_NAME = "place_media";
    private static final String KEY_ID = "id";
    private static final String PLACE_REF = "pid";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String THUMBNAIL_FILE_NAME = "tfname";
    private static final String THUMBNAIL_FILE_PATH = "tfpath";
    private static final String RESOURCE_FILE_NAME = "rfname";
    private static final String RESOURCE_FILE_PATH = "rfpath";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lng";
    private static final String CREATED_ON = "created_on";
    private static final String FETCHED_ON = "fetched_on";

    private Context context = null;

    public PlaceMediaDataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ORG_MEDIA_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PLACE_REF + " LONG,"
                + NAME + " TEXT,"
                + TYPE + " TEXT,"
                + RESOURCE_FILE_NAME + " TEXT,"
                + RESOURCE_FILE_PATH + " TEXT,"
                + THUMBNAIL_FILE_NAME + " TEXT,"
                + THUMBNAIL_FILE_PATH + " TEXT,"
                + LATITUDE + " TEXT,"
                + LONGITUDE + " TEXT,"
                + CREATED_ON + " LONG,"
                + FETCHED_ON + " LONG"
                + ")";
        db.execSQL(CREATE_ORG_MEDIA_TABLE);
    }

    public void dryRun() {
        SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(db);
        onCreate(db);
    }

    public void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void addMedia(Media media) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PLACE_REF, media.getPlaceRef());
        values.put(NAME, media.getName());
        values.put(TYPE, media.getType());
        values.put(THUMBNAIL_FILE_NAME, media.getTfName());
        values.put(THUMBNAIL_FILE_PATH, media.getTfPath());
        values.put(RESOURCE_FILE_NAME, media.getRfName());
        values.put(RESOURCE_FILE_PATH, media.getRfPath());
        values.put(LATITUDE, media.getLat());
        values.put(LONGITUDE, media.getLng());
        values.put(CREATED_ON, media.getCreatedOn());
        values.put(FETCHED_ON, new Long(System.currentTimeMillis()));
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void updateMedia(Media media) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PLACE_REF, media.getPlaceRef());
        values.put(NAME, media.getName());
        values.put(TYPE, media.getType());
        values.put(THUMBNAIL_FILE_NAME, media.getTfName());
        values.put(THUMBNAIL_FILE_PATH, media.getTfPath());
        values.put(RESOURCE_FILE_NAME, media.getRfName());
        values.put(RESOURCE_FILE_PATH, media.getRfPath());
        values.put(LATITUDE, media.getLat());
        values.put(LONGITUDE, media.getLng());
        values.put(CREATED_ON, media.getCreatedOn());
        values.put(FETCHED_ON, new Long(System.currentTimeMillis()));
        db.update(TABLE_NAME, values, KEY_ID + "=?", new String[]{media.getId() + ""});
        db.close();
    }

    public void addMedias(List<Media> media) {
        for (Media eachMedia : media) {
            addMedia(eachMedia);
        }
    }

    public Media getOrgMediaById(String id) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " where "+ KEY_ID +" ='" + id + "' limit 0,1";
        List<Media> mediaList = prepareDataFromQuery(selectQuery);
        if(mediaList.size() > 0){
            return mediaList.get(0);
        }else {
            return null;
        }
    }

    private List<Media> prepareDataFromQuery(String selectQuery) {
        List<Media> medias = new ArrayList<Media>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Media media = new Media();
                media.setId(Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_ID))));
                media.setPlaceRef(cursor.getLong(cursor.getColumnIndex(PLACE_REF)));
                media.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                media.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                media.setTfName(cursor.getString(cursor.getColumnIndex(THUMBNAIL_FILE_NAME)));
                media.setTfPath(cursor.getString(cursor.getColumnIndex(THUMBNAIL_FILE_PATH)));
                media.setRfName(cursor.getString(cursor.getColumnIndex(RESOURCE_FILE_NAME)));
                media.setRfPath(cursor.getString(cursor.getColumnIndex(RESOURCE_FILE_PATH)));
                media.setLat(cursor.getString(cursor.getColumnIndex(LATITUDE)));
                media.setLng(cursor.getString(cursor.getColumnIndex(LONGITUDE)));
                media.setCreatedOn(cursor.getLong(cursor.getColumnIndex(CREATED_ON)));
                media.setFetchedOn(cursor.getLong(cursor.getColumnIndex(FETCHED_ON)));
                medias.add(media);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return medias;
    }


    public int getMediaCount() {
        String countQuery = "SELECT count(*) FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        String count = cursor.getString(0);
        cursor.close();
        db.close();
        return Integer.parseInt(count);
    }

    public void deleteAllMedia() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public boolean checkIfStale(){
        String selectQuery = "SELECT * FROM "+ TABLE_NAME + " limit 0,1";
        List<Media> mediaList = prepareDataFromQuery(selectQuery);
        if(mediaList.size() > 0){
            Media media = mediaList.get(0);
            Long fetchedOn = media.getFetchedOn();
            Date fetchDate = new Date(fetchedOn);

            Calendar todayCal = Calendar.getInstance();
            // set the calendar to start of today
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            Date todayDate = todayCal.getTime();

            if(fetchDate.before(todayDate)){
                return true;
            }
        }else {
            return true;
        }
        return false;
    }

}