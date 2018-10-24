package com.pearnode.app.placero.position;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.connectivity.ConnectivityChangeReceiver;
import com.pearnode.app.placero.sync.LMSRestAsyncTask;
import com.pearnode.app.placero.util.AndroidSystemUtil;

public class PositionsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "com.pearnode.app.placero.db";
    public static final String TABLE_NAME = "position_master";

    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String DESCRIPTION = "desc";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String TAGS = "tags";
    private static final String UNIQUE_AREA_ID = "uniqueAreaId";
    private static final String UNIQUE_ID = "uniqueId";
    private static final String DIRTY = "dirty";
    private static final String D_ACTION = "d_action";
    private static final String CREATED_ON = "created_on";

    private Context context = null;
    public PositionsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                        NAME + " text," +
                        TYPE + " text," +
                        DESCRIPTION + " text," +
                        LAT + " text, " +
                        LON + " text," +
                        UNIQUE_AREA_ID + " text," +
                        UNIQUE_ID + " text," +
                        CREATED_ON + " text," +
                        DIRTY + " integer DEFAULT 0," +
                        D_ACTION + " text," +
                        TAGS + " text)"
        );
    }

    public void dryRun() {
        SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Position insertPositionLocally(Position pe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UNIQUE_ID, pe.getUniqueId());
        contentValues.put(UNIQUE_AREA_ID, pe.getUniqueAreaId());
        contentValues.put(NAME, pe.getName());
        contentValues.put(TYPE, pe.getType());
        contentValues.put(DESCRIPTION, pe.getDescription());
        contentValues.put(LAT, pe.getLat());
        contentValues.put(LON, pe.getLng());
        contentValues.put(TAGS, pe.getTags());
        contentValues.put(DIRTY, pe.getDirty());
        contentValues.put(D_ACTION, pe.getDirtyAction());
        contentValues.put(CREATED_ON, pe.getCreatedOnMillis());

        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return pe;
    }

    public Position updatePositionLocally(Position pe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UNIQUE_ID, pe.getUniqueId());
        contentValues.put(UNIQUE_AREA_ID, pe.getUniqueAreaId());
        contentValues.put(NAME, pe.getName());
        contentValues.put(TYPE, pe.getType());
        contentValues.put(DESCRIPTION, pe.getDescription());
        contentValues.put(LAT, pe.getLat());
        contentValues.put(LON, pe.getLng());
        contentValues.put(TAGS, pe.getTags());
        contentValues.put(DIRTY, pe.getDirty());
        contentValues.put(D_ACTION, pe.getDirtyAction());
        contentValues.put(CREATED_ON, pe.getCreatedOnMillis());

        db.update(TABLE_NAME, contentValues, UNIQUE_ID + "=?",
                new String[]{pe.getUniqueId()});
        db.close();
        return pe;
    }

    public Position insertPositionFromServer(Position pe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UNIQUE_ID, pe.getUniqueId());
        contentValues.put(UNIQUE_AREA_ID, pe.getUniqueAreaId());
        contentValues.put(NAME, pe.getName());
        contentValues.put(TYPE, pe.getType());
        contentValues.put(DESCRIPTION, pe.getDescription());
        contentValues.put(LAT, pe.getLat());
        contentValues.put(LON, pe.getLng());
        contentValues.put(TAGS, pe.getTags());
        contentValues.put(DIRTY, pe.getDirty());
        contentValues.put(D_ACTION, pe.getDirtyAction());
        contentValues.put(CREATED_ON, pe.getCreatedOnMillis());
        contentValues.put(DIRTY, 0);
        contentValues.put(D_ACTION, "none");

        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return pe;
    }

    public boolean insertPositionToServer(Position pe) {
        boolean networkAvailable = ConnectivityChangeReceiver.isConnected(context);
        if (networkAvailable) {
            new LMSRestAsyncTask().execute(preparePostParams("insert", pe));
        } else {
            pe.setDirty(1);
            pe.setDirtyAction("insert");
            updatePositionLocally(pe);
        }
        return networkAvailable;
    }

    public boolean updatePositionToServer(Position pe) {
        boolean networkAvailable = ConnectivityChangeReceiver.isConnected(context);
        if (networkAvailable) {
            new LMSRestAsyncTask().execute(preparePostParams("update", pe));
        } else {
            pe.setDirty(1);
            pe.setDirtyAction("update");
            updatePositionLocally(pe);
        }
        return networkAvailable;
    }

    public void deletePositionLocally(Position pe) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, UNIQUE_ID + "=?", new String[]{pe.getUniqueId()});
        db.close();
        return;
    }

    public boolean deletePositionFromServer(Position pe) {
        boolean networkAvailable = ConnectivityChangeReceiver.isConnected(context);
        if (networkAvailable) {
            new LMSRestAsyncTask().execute(preparePostParams("delete", pe));
        } else {
            pe.setDirty(1);
            pe.setDirtyAction("delete");
            updatePositionLocally(pe);
        }
        return networkAvailable;
    }

    public void deletePositionByAreaId(String uniqueAreaId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE "
                + UNIQUE_AREA_ID + " = '" + uniqueAreaId + "'");

        db.close();
    }

    public ArrayList<Position> getPositionsForArea(Area ae) {
        ArrayList<Position> pes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME
                        + " WHERE " + UNIQUE_AREA_ID + "=? AND "
                        + D_ACTION + "<> 'delete'",
                new String[]{ae.getUniqueId()});
        if((cursor != null) && (cursor.getCount() > 0)){
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                Position pe = new Position();

                pe.setUniqueId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                pe.setUniqueAreaId(cursor.getString(cursor.getColumnIndex(UNIQUE_AREA_ID)));

                pe.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                pe.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                String posDesc = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
                if (!posDesc.trim().equalsIgnoreCase("")) {
                    pe.setDescription(posDesc);
                }

                String latStr = cursor.getString(cursor.getColumnIndex(LAT));
                pe.setLat(Double.parseDouble(latStr));

                String lonStr = cursor.getString(cursor.getColumnIndex(LON));
                pe.setLng(Double.parseDouble(lonStr));

                pe.setTags(cursor.getString(cursor.getColumnIndex(TAGS)));
                pe.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(CREATED_ON)));

                pe.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
                pe.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));

                if (!pes.contains(pe)) {
                    pes.add(pe);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return pes;
    }

    public ArrayList<Position> getDirtyPositions() {
        ArrayList<Position> pes = new ArrayList<Position>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME
                + " WHERE " + DIRTY + " = 1", null);
        if((cursor != null) && (cursor.getCount() > 0)){
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                Position pe = new Position();

                pe.setUniqueId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                pe.setUniqueAreaId(cursor.getString(cursor.getColumnIndex(UNIQUE_AREA_ID)));
                pe.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                pe.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                String posDesc = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
                if (!posDesc.trim().equalsIgnoreCase("")) {
                    pe.setDescription(posDesc);
                }
                String latStr = cursor.getString(cursor.getColumnIndex(LAT));
                pe.setLat(Double.parseDouble(latStr));
                String lonStr = cursor.getString(cursor.getColumnIndex(LON));
                pe.setLng(Double.parseDouble(lonStr));
                pe.setTags(cursor.getString(cursor.getColumnIndex(TAGS)));
                pe.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(CREATED_ON)));

                pe.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
                pe.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));

                if (!pes.contains(pe)) {
                    pes.add(pe);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return pes;
    }

    public Position getPositionById(String positionId) {
        SQLiteDatabase db = getReadableDatabase();
        Position pe = null;
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME
                        + " WHERE " + UNIQUE_ID + "=?",
                new String[]{positionId});
        if((cursor != null) && (cursor.getCount() > 0)){
            cursor.moveToFirst();
            pe = new Position();

            pe.setUniqueId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
            pe.setUniqueAreaId(cursor.getString(cursor.getColumnIndex(UNIQUE_AREA_ID)));
            pe.setName(cursor.getString(cursor.getColumnIndex(NAME)));
            pe.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
            pe.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));

            String latStr = cursor.getString(cursor.getColumnIndex(LAT));
            pe.setLat(Double.parseDouble(latStr));

            String lonStr = cursor.getString(cursor.getColumnIndex(LON));
            pe.setLng(Double.parseDouble(lonStr));

            pe.setTags(cursor.getString(cursor.getColumnIndex(TAGS)));
            pe.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(CREATED_ON)));
            pe.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
            pe.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));

            cursor.close();
        }
        db.close();
        return pe;
    }

    private JSONObject preparePostParams(String queryType, Position pe) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("requestType", "PositionMaster");
            postParams.put("queryType", queryType);
            postParams.put("deviceID", AndroidSystemUtil.getDeviceId(context));
            postParams.put("lon", pe.getLng() + "");
            postParams.put("lat", pe.getLat() + "");
            postParams.put("desc", pe.getDescription());
            postParams.put("tags", pe.getTags());
            postParams.put("name", pe.getName());
            postParams.put("type", pe.getType());
            postParams.put("uniqueAreaId", pe.getUniqueAreaId());
            postParams.put("uniqueId", pe.getUniqueId());
            postParams.put("created_on", pe.getCreatedOnMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deletePositionsLocally() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, DIRTY + " = 0", null);
        db.close();
    }

}