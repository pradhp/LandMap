package com.pearnode.app.placero.area.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import com.pearnode.app.placero.area.model.AreaAddress;
import com.pearnode.app.placero.area.model.AreaElement;
import com.pearnode.app.placero.area.model.AreaMeasure;
import com.pearnode.app.placero.connectivity.ConnectivityChangeReceiver;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.google.geo.CommonGeoHelper;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.position.PositionElement;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.sync.LMSRestAsyncTask;
import com.pearnode.app.placero.tags.TagsDBHelper;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.util.AndroidSystemUtil;

public class AreaDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "com.pearnode.app.placero.db";
    private Context context;

    private static final String AREA_TABLE_NAME = "area_master";
    private static final String AREA_COLUMN_NAME = "name";
    private static final String AREA_COLUMN_DESCRIPTION = "desc";
    private static final String AREA_COLUMN_CREATED_BY = "created_by";
    private static final String AREA_COLUMN_CENTER_LAT = "center_lat";
    private static final String AREA_COLUMN_CENTER_LON = "center_lon";
    private static final String AREA_COLUMN_MEASURE_SQ_FT = "measure_sq_ft";
    private static final String AREA_COLUMN_UNIQUE_ID = "unique_id";
    private static final String AREA_COLUMN_ADDRESS = "address";
    private static final String AREA_COLUMN_TYPE = "type";
    private static final String AREA_COLUMN_DIRTY_FLAG = "dirty";
    private static final String AREA_COLUMN_DIRTY_ACTION = "d_action";

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
        db.execSQL(
                "create table " + AREA_TABLE_NAME + "(" +
                        AREA_COLUMN_NAME + " text," +
                        AREA_COLUMN_DESCRIPTION + " text," +
                        AREA_COLUMN_CREATED_BY + " text," +
                        AREA_COLUMN_CENTER_LAT + " text, " +
                        AREA_COLUMN_CENTER_LON + " text, " +
                        AREA_COLUMN_MEASURE_SQ_FT + " text, " +
                        AREA_COLUMN_UNIQUE_ID + " text, " +
                        AREA_COLUMN_ADDRESS + " text, " +
                        AREA_COLUMN_DIRTY_FLAG + " integer DEFAULT 0," +
                        AREA_COLUMN_DIRTY_ACTION + " text," +
                        AREA_COLUMN_TYPE + " text )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AREA_TABLE_NAME);
        this.onCreate(db);
    }

    public AreaElement insertAreaLocally(boolean online) {
        SQLiteDatabase db = getWritableDatabase();

        AreaElement ae = new AreaElement();
        String uniqueId = UUID.randomUUID().toString();

        ae.setName("PL_" + uniqueId);

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_NAME, ae.getName());

        ae.setDescription("No Description");
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());

        String createdBy = UserContext.getInstance().getUserElement().getEmail();
        contentValues.put(AREA_COLUMN_CREATED_BY, createdBy);
        ae.setCreatedBy(createdBy);

        contentValues.put(AREA_COLUMN_UNIQUE_ID, uniqueId);
        ae.setUniqueId(uniqueId);

        contentValues.put(AREA_COLUMN_CENTER_LAT, "0.0");
        ae.getCenterPosition().setLat(0.0);

        contentValues.put(AREA_COLUMN_CENTER_LON, "0.0");
        ae.getCenterPosition().setLon(0.0);

        contentValues.put(AREA_COLUMN_MEASURE_SQ_FT, "0");

        AreaMeasure measure = new AreaMeasure(0.0);
        ae.setMeasure(measure);

        contentValues.put(AREA_COLUMN_ADDRESS, "");
        ae.setAddress(null);

        contentValues.put(AREA_COLUMN_TYPE, "self");
        ae.setType("self");

        if (!online) {
            contentValues.put(AREA_COLUMN_DIRTY_FLAG, "1");
            ae.setDirty(1);

        } else {
            contentValues.put(AREA_COLUMN_DIRTY_FLAG, "0");
            ae.setDirty(0);
        }

        contentValues.put(AREA_COLUMN_DIRTY_ACTION, "insert");
        ae.setDirtyAction("insert");

        db.insert(AREA_TABLE_NAME, null, contentValues);
        db.close();

        return ae;
    }

    public boolean insertAreaToServer(AreaElement ae) {
        boolean networkAvailable = ConnectivityChangeReceiver.isConnected(context);
        if (networkAvailable) {
            new LMSRestAsyncTask(callback).execute(preparePostParams("insert", ae));
        } else {
            ae.setDirty(1);
            ae.setDirtyAction("insert");
            updateAreaLocally(ae);
        }
        return networkAvailable;
    }

    public AreaElement insertAreaFromServer(AreaElement ae) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_UNIQUE_ID, ae.getUniqueId());
        contentValues.put(AREA_COLUMN_NAME, ae.getName());
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());
        contentValues.put(AREA_COLUMN_CREATED_BY, ae.getCreatedBy());
        contentValues.put(AREA_COLUMN_CENTER_LAT, ae.getCenterPosition().getLat() + "");
        contentValues.put(AREA_COLUMN_CENTER_LON, ae.getCenterPosition().getLon() + "");
        contentValues.put(AREA_COLUMN_MEASURE_SQ_FT, ae.getMeasure().getSqFeet() + "");

        AreaAddress areaAddress = ae.getAddress();
        if (areaAddress != null) {
            contentValues.put(AREA_COLUMN_ADDRESS, areaAddress.getStorableAddress());
        } else {
            contentValues.put(AREA_COLUMN_ADDRESS, "");
        }
        contentValues.put(AREA_COLUMN_TYPE, ae.getType());
        contentValues.put(AREA_COLUMN_DIRTY_FLAG, 0);
        contentValues.put(AREA_COLUMN_DIRTY_ACTION, "none");

        db.insert(AREA_TABLE_NAME, null, contentValues);
        db.close();

        return ae;
    }

    public void updateAreaLocally(AreaElement ae) {
        SQLiteDatabase db = getWritableDatabase();
        PositionElement centerPosition = ae.getCenterPosition();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_UNIQUE_ID, ae.getUniqueId());
        contentValues.put(AREA_COLUMN_NAME, ae.getName());
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());
        contentValues.put(AREA_COLUMN_CENTER_LAT, centerPosition.getLat());
        contentValues.put(AREA_COLUMN_CENTER_LON, centerPosition.getLon());
        contentValues.put(AREA_COLUMN_MEASURE_SQ_FT, ae.getMeasure().getSqFeet() + "");
        contentValues.put(AREA_COLUMN_CREATED_BY, ae.getCreatedBy());
        contentValues.put(AREA_COLUMN_TYPE, ae.getType());

        AreaAddress address = ae.getAddress();
        if (address != null) {
            contentValues.put(AREA_COLUMN_ADDRESS, address.getStorableAddress());
        } else {
            CommonGeoHelper geoHelper = CommonGeoHelper.INSTANCE;
            AreaAddress areaAddress = geoHelper.getAddressByGeoLocation(context,
                    centerPosition.getLat(), centerPosition.getLon());
            contentValues.put(AREA_COLUMN_ADDRESS, areaAddress.getStorableAddress());
            ae.setAddress(areaAddress);
        }
        contentValues.put(AREA_COLUMN_DIRTY_FLAG, ae.getDirty());
        contentValues.put(AREA_COLUMN_DIRTY_ACTION, ae.getDirtyAction());

        db.update(AREA_TABLE_NAME, contentValues, AREA_COLUMN_UNIQUE_ID + " = ? ", new String[]{ae.getUniqueId()});
        db.close();
    }

    public boolean updateAreaOnServer(AreaElement ae) {
        boolean networkAvailable = ConnectivityChangeReceiver.isConnected(context);
        if (networkAvailable) {
            new LMSRestAsyncTask(callback).execute(preparePostParams("update", ae));
        } else {
            ae.setDirty(1);
            ae.setDirtyAction("update");
            updateAreaLocally(ae);
        }
        return networkAvailable;
    }

    public void deleteArea(AreaElement ae) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(AREA_TABLE_NAME, AREA_COLUMN_UNIQUE_ID + " = ? ", new String[]{ae.getUniqueId()});
        db.close();

        PositionsDBHelper pdb = new PositionsDBHelper(context);
        pdb.deletePositionByAreaId(ae.getUniqueId());
    }

    public boolean deleteAreaFromServer(AreaElement ae) {
        boolean networkAvailable = ConnectivityChangeReceiver.isConnected(context);
        if (networkAvailable) {
            new LMSRestAsyncTask(callback).execute(preparePostParams("delete", ae));
        } else {
            ae.setDirty(1);
            ae.setDirtyAction("delete");
            updateAreaLocally(ae);
        }
        return networkAvailable;
    }

    public AreaElement getAreaById(String areaId) {
        SQLiteDatabase db = getReadableDatabase();
        AreaElement ae = new AreaElement();

        DriveDBHelper ddh = new DriveDBHelper(context);
        PermissionsDBHelper pmh = new PermissionsDBHelper(context);
        PositionsDBHelper pdh = new PositionsDBHelper(context);

        Cursor cursor = db.rawQuery("select * from " + AREA_TABLE_NAME + " WHERE "
                + AREA_COLUMN_UNIQUE_ID + " =?"
                , new String[]{areaId});
        try {
            if(cursor == null || cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            ae.setName(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_NAME)));
            ae.setDescription(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DESCRIPTION)));
            ae.setCreatedBy(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CREATED_BY)));
            ae.setUniqueId(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_UNIQUE_ID)));
            ae.getCenterPosition().setLat(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LAT))));
            ae.getCenterPosition().setLon(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LON))));

            Double sqFt = new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_MEASURE_SQ_FT)));
            AreaMeasure measure = new AreaMeasure(sqFt);
            ae.setMeasure(measure);

            String addressText = cursor.getString(cursor.getColumnIndex(AREA_COLUMN_ADDRESS));
            ae.setAddress(AreaAddress.fromStoredAddress(addressText));
            ae.setType(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_TYPE)));
            ae.setDirty(cursor.getInt(cursor.getColumnIndex(AREA_COLUMN_DIRTY_FLAG)));
            ae.setDirtyAction(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DIRTY_ACTION)));

            ae.getMediaResources().addAll(ddh.getDriveResourcesByAreaId(ae.getUniqueId()));
            ae.setUserPermissions(pmh.fetchPermissionsByAreaId(ae.getUniqueId()));
            ae.setPositions(pdh.getPositionsForArea(ae));

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return ae;
    }

    public ArrayList<AreaElement> getAreas(String type) {
        ArrayList<AreaElement> areas = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        DriveDBHelper ddh = new DriveDBHelper(context);
        PermissionsDBHelper pdh = new PermissionsDBHelper(context);

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + AREA_TABLE_NAME + " WHERE "
                    + AREA_COLUMN_TYPE + " =? AND "
                    + AREA_COLUMN_DIRTY_ACTION + " !=?"
                    , new String[]{type, "delete"});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    AreaElement ae = new AreaElement();
                    ae.setName(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_NAME)));
                    ae.setDescription(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DESCRIPTION)));
                    ae.setCreatedBy(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CREATED_BY)));
                    ae.setUniqueId(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_UNIQUE_ID)));
                    ae.getCenterPosition().setLat(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LAT))));
                    ae.getCenterPosition().setLon(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LON))));

                    Double sqFt = new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_MEASURE_SQ_FT)));
                    AreaMeasure measure = new AreaMeasure(sqFt);
                    ae.setMeasure(measure);

                    String addressText = cursor.getString(cursor.getColumnIndex(AREA_COLUMN_ADDRESS));
                    ae.setAddress(AreaAddress.fromStoredAddress(addressText));
                    ae.setType(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_TYPE)));
                    ae.setDirty(cursor.getInt(cursor.getColumnIndex(AREA_COLUMN_DIRTY_FLAG)));
                    ae.setDirtyAction(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DIRTY_ACTION)));

                    ae.getMediaResources().addAll(ddh.getDriveResourcesByAreaId(ae.getUniqueId()));
                    areas.add(ae);

                    ae.setUserPermissions(pdh.fetchPermissionsByAreaId(ae.getUniqueId()));
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

    public ArrayList<AreaElement> getDirtyAreas() {
        ArrayList<AreaElement> allAreas = new ArrayList<AreaElement>();
        SQLiteDatabase db = getReadableDatabase();

        DriveDBHelper ddh = new DriveDBHelper(context);
        PermissionsDBHelper pdh = new PermissionsDBHelper(context);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + AREA_TABLE_NAME
                    + " WHERE " + AREA_COLUMN_DIRTY_FLAG + "=1", null);
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    AreaElement ae = new AreaElement();
                    ae.setName(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_NAME)));
                    ae.setDescription(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DESCRIPTION)));
                    ae.setCreatedBy(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CREATED_BY)));
                    ae.setUniqueId(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_UNIQUE_ID)));
                    ae.getCenterPosition().setLat(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LAT))));
                    ae.getCenterPosition().setLon(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LON))));

                    Double sqFt = new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_MEASURE_SQ_FT)));
                    AreaMeasure measure = new AreaMeasure(sqFt);
                    ae.setMeasure(measure);

                    String addressText = cursor.getString(cursor.getColumnIndex(AREA_COLUMN_ADDRESS));
                    ae.setAddress(AreaAddress.fromStoredAddress(addressText));
                    ae.setType(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_TYPE)));
                    ae.setDirty(cursor.getInt(cursor.getColumnIndex(AREA_COLUMN_DIRTY_FLAG)));
                    ae.setDirtyAction(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DIRTY_ACTION)));

                    ae.getMediaResources().addAll(ddh.getDriveResourcesByAreaId(ae.getUniqueId()));
                    allAreas.add(ae);

                    ae.setUserPermissions(pdh.fetchPermissionsByAreaId(ae.getUniqueId()));
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

    private JSONObject preparePostParams(String queryType, AreaElement ae) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("requestType", "AreaMaster");
            postParams.put("queryType", queryType);
            postParams.put("deviceID", AndroidSystemUtil.getDeviceId(context));
            postParams.put("center_lon", ae.getCenterPosition().getLon());
            postParams.put("center_lat", ae.getCenterPosition().getLat());
            postParams.put("desc", ae.getDescription());
            postParams.put("name", ae.getName());
            postParams.put("created_by", ae.getCreatedBy());
            postParams.put("unique_id", ae.getUniqueId());
            postParams.put("msqft", ae.getMeasure().getSqFeet());
            AreaAddress address = ae.getAddress();
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
        db.delete(AREA_TABLE_NAME, AREA_COLUMN_DIRTY_FLAG + " = 0 ", null);
        db.close();
    }

    public void deletePublicAreas() {
        ArrayList<AreaElement> publicAreas = getAreas("public");

        PositionsDBHelper pdh = new PositionsDBHelper(context);
        DriveDBHelper ddh = new DriveDBHelper(context);
        PermissionsDBHelper pmh = new PermissionsDBHelper(context);

        SQLiteDatabase db = getWritableDatabase();
        for (int i = 0; i < publicAreas.size(); i++) {
            String areaId = publicAreas.get(i).getUniqueId();
            db.delete(AREA_TABLE_NAME, AREA_COLUMN_UNIQUE_ID + "=? AND "
                    + AREA_COLUMN_TYPE + "=?", new String[]{areaId, "public"});
            pdh.deletePositionByAreaId(areaId);
            ddh.deleteResourcesByAreaId(areaId);
            pmh.deletePermissionsByAreaId(areaId);
        }
        db.close();
    }

    public void fetchShareHistory(AreaElement ae) {
        LMSRestAsyncTask findTask = new LMSRestAsyncTask(callback);
        findTask.execute(preparePostParams("findShareHistory", ae));
    }

    public void insertAreaAddressTagsLocally(AreaElement ae) {
        TagsDBHelper tagsDBHelper = new TagsDBHelper(context);
        AreaAddress address = ae.getAddress();
        if (address != null) {
            tagsDBHelper.insertTagsLocally(address.getTags(), "area", ae.getUniqueId());
        }
    }

    public void insertAreaAddressTagsOnServer(AreaElement ae) {
        TagsDBHelper tagsDBHelper = new TagsDBHelper(context);
        AreaAddress address = ae.getAddress();
        if (address != null) {
            tagsDBHelper.insertTagsToServer(address.getTags(), "area", ae.getUniqueId());
        }
    }
}