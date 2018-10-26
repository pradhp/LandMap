package com.pearnode.app.placero.area.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pearnode.app.placero.area.model.Address;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.model.AreaMeasure;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.tags.TagsDBHelper;
import com.pearnode.app.placero.util.AndroidSystemUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AreaDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "com.pearnode.app.placero.db";
    private Context context;

    private static final String TABLE_NAME = "am";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "desc";
    private static final String CREATED_BY = "cr_by";
    private static final String CENTER_LAT = "clat";
    private static final String CENTER_LON = "clng";
    private static final String MEASURE_SQ_FT = "m_sq_ft";
    private static final String UNIQUE_ID = "uid";
    private static final String ADDRESS = "address";
    private static final String TYPE = "type";
    private static final String DIRTY_FLAG = "dirty";
    private static final String DIRTY_ACTION = "d_action";
    private static final String CREATED_ON = "con";
    private static final String UPDATED_ON = "uon";

    private AsyncTaskCallback callback;

    public AreaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    public AreaDBHelper(Context context, AsyncTaskCallback callback) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                        + "("
                            + NAME          + " text,"
                            + DESCRIPTION   + " text,"
                            + CREATED_BY    + " text,"
                            + CENTER_LAT    + " text, "
                            + CENTER_LON    + " text, "
                            + MEASURE_SQ_FT + " text, "
                            + UNIQUE_ID     + " text, "
                            + ADDRESS       + " text, "
                            + DIRTY_FLAG    + " integer DEFAULT 0,"
                            + DIRTY_ACTION  + " text,"
                            + TYPE          + " text,"
                            + CREATED_ON    + " long,"
                            + UPDATED_ON    + " long"
                        + ")"
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
        this.onCreate(db);
    }

    public Area insertArea(Area area) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UNIQUE_ID, area.getId());
        contentValues.put(NAME, area.getName());
        contentValues.put(DESCRIPTION, area.getDescription());
        contentValues.put(CREATED_BY, area.getCreatedBy());

        Position centerPosition = area.getCenterPosition();
        if(centerPosition != null){
            contentValues.put(CENTER_LAT, centerPosition.getLat());
            contentValues.put(CENTER_LON, centerPosition.getLng());
        }else {
            contentValues.put(CENTER_LAT, 0.0D);
            contentValues.put(CENTER_LON, 0.0D);
        }

        AreaMeasure measure = area.getMeasure();
        if(measure != null){
            contentValues.put(MEASURE_SQ_FT, measure.getSqFeet());
        }else {
            contentValues.put(MEASURE_SQ_FT, 0);
        }

        Address address = area.getAddress();
        if(address != null){
            contentValues.put(ADDRESS, address.getStorableAddress());
        }else {
            contentValues.put(ADDRESS, "");
        }
        contentValues.put(TYPE, area.getType());
        contentValues.put(DIRTY_FLAG, area.getDirty());
        contentValues.put(DIRTY_ACTION, area.getDirtyAction());
        contentValues.put(CREATED_ON, System.currentTimeMillis());
        contentValues.put(UPDATED_ON, System.currentTimeMillis());
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return area;
    }

    public void updateArea(Area ae) {
        SQLiteDatabase db = getWritableDatabase();
        Position centerPosition = ae.getCenterPosition();

        ContentValues contentValues = new ContentValues();
        contentValues.put(UNIQUE_ID, ae.getId());
        contentValues.put(NAME, ae.getName());
        contentValues.put(DESCRIPTION, ae.getDescription());
        contentValues.put(CENTER_LAT, centerPosition.getLat());
        contentValues.put(CENTER_LON, centerPosition.getLng());
        contentValues.put(MEASURE_SQ_FT, ae.getMeasure().getSqFeet() + "");
        contentValues.put(CREATED_BY, ae.getCreatedBy());
        contentValues.put(TYPE, ae.getType());

        contentValues.put(DIRTY_FLAG, ae.getDirty());
        contentValues.put(DIRTY_ACTION, ae.getDirtyAction());
        contentValues.put(UPDATED_ON, System.currentTimeMillis());

        db.update(TABLE_NAME, contentValues, UNIQUE_ID + " = ? ", new String[]{ae.getId()});
        db.close();
    }

    public void deleteArea(Area ae) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, UNIQUE_ID + " = ? ", new String[]{ae.getId()});
        db.close();

        PositionsDBHelper pdb = new PositionsDBHelper(context);
        pdb.deletePositionByAreaId(ae.getId());
    }

    public Area getAreaById(String areaId) {
        SQLiteDatabase db = getReadableDatabase();
        Area ae = new Area();

        MediaDataBaseHandler ddh = new MediaDataBaseHandler(context);
        PermissionsDBHelper pmh = new PermissionsDBHelper(context);
        PositionsDBHelper pdh = new PositionsDBHelper(context);

        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE "
                + UNIQUE_ID + " =?"
                , new String[]{areaId});
        try {
            if(cursor == null || cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            ae.setName(cursor.getString(cursor.getColumnIndex(NAME)));
            ae.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
            ae.setCreatedBy(cursor.getString(cursor.getColumnIndex(CREATED_BY)));
            ae.setId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
            ae.getCenterPosition().setLat(new Double(cursor.getString(cursor.getColumnIndex(CENTER_LAT))));
            ae.getCenterPosition().setLng(new Double(cursor.getString(cursor.getColumnIndex(CENTER_LON))));

            Double sqFt = new Double(cursor.getString(cursor.getColumnIndex(MEASURE_SQ_FT)));
            AreaMeasure measure = new AreaMeasure(sqFt);
            ae.setMeasure(measure);

            String addressText = cursor.getString(cursor.getColumnIndex(ADDRESS));
            ae.setAddress(Address.fromStoredAddress(addressText));
            ae.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
            ae.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY_FLAG)));
            ae.setDirtyAction(cursor.getString(cursor.getColumnIndex(DIRTY_ACTION)));

            ae.getPictures().addAll(ddh.getPlacePictures(ae.getId()));
            ae.getVideos().addAll(ddh.getPlaceVideos(ae.getId()));
            ae.getDocuments().addAll(ddh.getPlaceDocuments(ae.getId()));
            ae.setPermissions(pmh.fetchPermissionsByAreaId(ae.getId()));
            ae.setPositions(pdh.getPositionsForArea(ae));

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return ae;
    }

    public ArrayList<Area> getAreas(String type) {
        ArrayList<Area> areas = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        MediaDataBaseHandler ddh = new MediaDataBaseHandler(context);
        PermissionsDBHelper pdh = new PermissionsDBHelper(context);

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE "
                    + TYPE + " =? AND "
                    + DIRTY_ACTION + " !=?"
                    , new String[]{type, "delete"});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    Area ae = new Area();
                    ae.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                    ae.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
                    ae.setCreatedBy(cursor.getString(cursor.getColumnIndex(CREATED_BY)));
                    ae.setId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                    ae.getCenterPosition().setLat(new Double(cursor.getString(cursor.getColumnIndex(CENTER_LAT))));
                    ae.getCenterPosition().setLng(new Double(cursor.getString(cursor.getColumnIndex(CENTER_LON))));

                    Double sqFt = new Double(cursor.getString(cursor.getColumnIndex(MEASURE_SQ_FT)));
                    AreaMeasure measure = new AreaMeasure(sqFt);
                    ae.setMeasure(measure);

                    String addressText = cursor.getString(cursor.getColumnIndex(ADDRESS));
                    ae.setAddress(Address.fromStoredAddress(addressText));
                    ae.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                    ae.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY_FLAG)));
                    ae.setDirtyAction(cursor.getString(cursor.getColumnIndex(DIRTY_ACTION)));

                    ae.getPictures().addAll(ddh.getPlacePictures(ae.getId()));
                    ae.getVideos().addAll(ddh.getPlaceVideos(ae.getId()));
                    ae.getDocuments().addAll(ddh.getPlaceDocuments(ae.getId()));
                    areas.add(ae);

                    ae.setPermissions(pdh.fetchPermissionsByAreaId(ae.getId()));
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return areas;
    }

    public ArrayList<Area> getDirtyAreas() {
        ArrayList<Area> allAreas = new ArrayList<Area>();
        SQLiteDatabase db = getReadableDatabase();

        MediaDataBaseHandler ddh = new MediaDataBaseHandler(context);
        PermissionsDBHelper pdh = new PermissionsDBHelper(context);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME
                    + " WHERE " + DIRTY_FLAG + "=1 ORDER BY " + UPDATED_ON + " ASC", new String[]{});
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    Area ae = new Area();
                    ae.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                    ae.setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
                    ae.setCreatedBy(cursor.getString(cursor.getColumnIndex(CREATED_BY)));
                    ae.setId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                    ae.getCenterPosition().setLat(new Double(cursor.getString(cursor.getColumnIndex(CENTER_LAT))));
                    ae.getCenterPosition().setLng(new Double(cursor.getString(cursor.getColumnIndex(CENTER_LON))));

                    Double sqFt = new Double(cursor.getString(cursor.getColumnIndex(MEASURE_SQ_FT)));
                    AreaMeasure measure = new AreaMeasure(sqFt);
                    ae.setMeasure(measure);

                    String addressText = cursor.getString(cursor.getColumnIndex(ADDRESS));
                    ae.setAddress(Address.fromStoredAddress(addressText));
                    ae.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                    ae.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY_FLAG)));
                    ae.setDirtyAction(cursor.getString(cursor.getColumnIndex(DIRTY_ACTION)));

                    ae.getPictures().addAll(ddh.getPlacePictures(ae.getId()));
                    ae.getVideos().addAll(ddh.getPlaceVideos(ae.getId()));
                    ae.getDocuments().addAll(ddh.getPlaceDocuments(ae.getId()));
                    allAreas.add(ae);

                    ae.setPermissions(pdh.fetchPermissionsByAreaId(ae.getId()));
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return allAreas;
    }

    private JSONObject preparePostParams(String queryType, Area ae) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("requestType", "AreaMaster");
            postParams.put("queryType", queryType);
            postParams.put("deviceID", AndroidSystemUtil.getDeviceId(context));
            postParams.put("center_lon", ae.getCenterPosition().getLng());
            postParams.put("center_lat", ae.getCenterPosition().getLat());
            postParams.put("desc", ae.getDescription());
            postParams.put("name", ae.getName());
            postParams.put("created_by", ae.getCreatedBy());
            postParams.put("unique_id", ae.getId());
            postParams.put("msqft", ae.getMeasure().getSqFeet());
            Address address = ae.getAddress();
            if (address != null) {
                postParams.put("address", address.getStorableAddress());
            } else {
                postParams.put("address", "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deleteAreasLocally() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, DIRTY_FLAG + "=0", null);
        db.close();
    }

    public void deletePublicAreas() {
        ArrayList<Area> publicAreas = getAreas("public");

        PositionsDBHelper pdh = new PositionsDBHelper(context);
        MediaDataBaseHandler ddh = new MediaDataBaseHandler(context);
        PermissionsDBHelper pmh = new PermissionsDBHelper(context);

        SQLiteDatabase db = getWritableDatabase();
        for (int i = 0; i < publicAreas.size(); i++) {
            String areaId = publicAreas.get(i).getId();
            db.delete(TABLE_NAME, UNIQUE_ID + "=? AND "
                    + TYPE + "=?", new String[]{areaId, "public"});
            pdh.deletePositionByAreaId(areaId);
            ddh.deletePlaceMedia(areaId);
            pmh.deletePermissionsByAreaId(areaId);
        }
        db.close();
    }

    public void insertAreaAddressTagsLocally(Area ae) {
        TagsDBHelper tagsDBHelper = new TagsDBHelper(context);
        Address address = ae.getAddress();
        if (address != null) {
            tagsDBHelper.addTags(address.getTags(), "area", ae.getId());
        }
    }

}