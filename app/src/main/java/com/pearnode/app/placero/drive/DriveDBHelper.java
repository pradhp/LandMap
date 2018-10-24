package com.pearnode.app.placero.drive;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.connectivity.ConnectivityChangeReceiver;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.sync.LMSRestAsyncTask;
import com.pearnode.app.placero.util.AndroidSystemUtil;

public class DriveDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "com.pearnode.app.placero.db";
    private static final String TABLE_NAME = "drive_master";

    private static final String UNIQUE_ID = "unique_id";
    private static final String USER_ID = "user_id";
    private static final String AREA_ID = "area_id";
    private static final String RESOURCE_ID = "resource_id";
    private static final String CONTAINER_ID = "container_id";

    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String CONTENT_TYPE = "content_type";
    private static final String MIME_TYPE = "mime_type";
    private static final String SIZE = "size";
    private static final String POSITION_ID = "position_id";
    private static final String CREATED_ON = "created_on";
    private static final String DIRTY = "dirty";
    private static final String D_ACTION = "d_action";
    private static final String FILE_PATH = "file_path";

    private AsyncTaskCallback callback;
    private Context context;

    public DriveDBHelper(Context context, AsyncTaskCallback callback) {
        super(context, DATABASE_NAME, null, 1);
        this.callback = callback;
        this.context = context;
    }

    public DriveDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                        UNIQUE_ID + " text," +
                        AREA_ID + " text," +
                        USER_ID + " text," +
                        RESOURCE_ID + " text," +
                        CONTAINER_ID + " text," +
                        NAME + " text," +
                        TYPE + " text," +
                        CONTENT_TYPE + " text," +
                        MIME_TYPE + " text," +
                        POSITION_ID + " text," +
                        CREATED_ON + " text," +
                        DIRTY + " integer DEFAULT 0," +
                        D_ACTION + " text," +
                        FILE_PATH + " text," +
                        SIZE + " text)"
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

    public void insertResourceLocally(Resource dr) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UNIQUE_ID, dr.getUniqueId());
        contentValues.put(AREA_ID, dr.getAreaId());
        contentValues.put(USER_ID, dr.getUserId());
        contentValues.put(RESOURCE_ID, dr.getResourceId());
        contentValues.put(CONTAINER_ID, dr.getContainerId());
        contentValues.put(NAME, dr.getName());
        contentValues.put(TYPE, dr.getType());
        contentValues.put(CONTENT_TYPE, dr.getContentType());
        contentValues.put(MIME_TYPE, dr.getMimeType());
        contentValues.put(SIZE, dr.getSize());

        Position position = dr.getPosition();
        if(position != null){
            contentValues.put(POSITION_ID, position.getUniqueId());
        }else {
            contentValues.put(POSITION_ID, "");
        }
        contentValues.put(CREATED_ON, dr.getCreatedOnMillis());
        contentValues.put(DIRTY, dr.isDirty());
        contentValues.put(D_ACTION, dr.getDirtyAction());
        contentValues.put(FILE_PATH, dr.getPath());

        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }

    public void updateResourceLocally(Resource dr) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UNIQUE_ID, dr.getUniqueId());
        contentValues.put(AREA_ID, dr.getAreaId());
        contentValues.put(USER_ID, dr.getUserId());
        contentValues.put(RESOURCE_ID, dr.getResourceId());
        contentValues.put(CONTAINER_ID, dr.getContainerId());
        contentValues.put(NAME, dr.getName());
        contentValues.put(TYPE, dr.getType());
        contentValues.put(CONTENT_TYPE, dr.getContentType());
        contentValues.put(MIME_TYPE, dr.getMimeType());
        contentValues.put(SIZE, dr.getSize());

        Position position = dr.getPosition();
        if(position != null){
            contentValues.put(POSITION_ID, position.getUniqueId());
        }else {
            contentValues.put(POSITION_ID, "");
        }

        contentValues.put(CREATED_ON, dr.getCreatedOnMillis());
        contentValues.put(DIRTY, dr.isDirty());
        contentValues.put(D_ACTION, dr.getDirtyAction());
        contentValues.put(FILE_PATH, dr.getPath());

        db.update(TABLE_NAME, contentValues,
                UNIQUE_ID + "=?", new String[]{dr.getUniqueId()});
        db.close();
    }

    public boolean insertResourceToServer(Resource dr) {
        boolean networkAvailable = ConnectivityChangeReceiver.isConnected(context);
        if(networkAvailable){
            new LMSRestAsyncTask(callback).execute(preparePostParams("insert", dr));
        }else {
            dr.setDirty(1);
            dr.setDirtyAction("insert");
            updateResourceLocally(dr);
        }
        return networkAvailable;
    }

    public boolean updateResourceToServer(Resource dr) {
        boolean networkAvailable = ConnectivityChangeReceiver.isConnected(context);
        if(networkAvailable){
            new LMSRestAsyncTask(callback).execute(preparePostParams("update", dr));
        }else {
            dr.setDirty(1);
            dr.setDirtyAction("update");
            updateResourceLocally(dr);
        }
        return networkAvailable;
    }

    public boolean deleteResourceFromServer(Resource resource) {
        boolean networkAvailable = ConnectivityChangeReceiver.isConnected(context);
        if(networkAvailable){
            new LMSRestAsyncTask(callback).execute(preparePostParams("delete", resource));
        }else {
            resource.setDirty(1);
            resource.setDirtyAction("delete");
            updateResourceLocally(resource);
        }
        return networkAvailable;
    }

    public Resource insertResourceFromServer(Resource dr) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(UNIQUE_ID, dr.getUniqueId());
        contentValues.put(AREA_ID, dr.getAreaId());
        contentValues.put(USER_ID, dr.getUserId());
        contentValues.put(RESOURCE_ID, dr.getResourceId());
        contentValues.put(CONTAINER_ID, dr.getContainerId());
        contentValues.put(NAME, dr.getName());
        contentValues.put(TYPE, dr.getType());
        contentValues.put(CONTENT_TYPE, dr.getContentType());
        contentValues.put(MIME_TYPE, dr.getMimeType());
        contentValues.put(SIZE, dr.getSize());

        Position position = dr.getPosition();
        if(position != null){
            contentValues.put(POSITION_ID, position.getUniqueId());
        }else {
            contentValues.put(POSITION_ID, "");
        }
        contentValues.put(CREATED_ON, dr.getCreatedOnMillis());
        contentValues.put(DIRTY, 0);
        contentValues.put(D_ACTION, "none");
        contentValues.put(FILE_PATH, dr.getPath());

        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return dr;
    }

    public ArrayList<Resource> getDriveResourcesByAreaId(String aid) {
        ArrayList<Resource> resources = new ArrayList<>();
        PositionsDBHelper pdh = new PositionsDBHelper(context);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE "
                            + AREA_ID + "=? AND "
                            + D_ACTION + "<> 'delete'", new String[]{aid});
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    Resource dr = new Resource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                    dr.setAreaId(cursor.getString(cursor.getColumnIndex(AREA_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(USER_ID)));
                    dr.setContainerId(cursor.getString(cursor.getColumnIndex(CONTAINER_ID)));
                    dr.setResourceId(cursor.getString(cursor.getColumnIndex(RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                    dr.setContentType(cursor.getString(cursor.getColumnIndex(CONTENT_TYPE)));
                    dr.setMimeType(cursor.getString(cursor.getColumnIndex(MIME_TYPE)));
                    dr.setSize(cursor.getString(cursor.getColumnIndex(SIZE)));

                    String positionId = cursor.getString(cursor.getColumnIndex(POSITION_ID));
                    if(positionId != null && !positionId.trim().equalsIgnoreCase("")){
                        dr.setPosition(pdh.getPositionById(positionId));
                    }
                    dr.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(CREATED_ON)));
                    dr.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
                    dr.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));
                    dr.setPath(cursor.getString(cursor.getColumnIndex(FILE_PATH)));

                    resources.add(dr);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return resources;
    }

    public Resource getDriveResourceByResourceId(String resourceID) {
        SQLiteDatabase db = getReadableDatabase();
        PositionsDBHelper pdh = new PositionsDBHelper(context);

        Cursor cursor = null;
        Resource resource = new Resource();
        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE "
                            + RESOURCE_ID + "=?",
                    new String[]{resourceID});
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                resource.setUniqueId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                resource.setAreaId(cursor.getString(cursor.getColumnIndex(AREA_ID)));
                resource.setUserId(cursor.getString(cursor.getColumnIndex(USER_ID)));
                resource.setContainerId(cursor.getString(cursor.getColumnIndex(CONTAINER_ID)));
                resource.setResourceId(cursor.getString(cursor.getColumnIndex(RESOURCE_ID)));
                resource.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                resource.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                resource.setContentType(cursor.getString(cursor.getColumnIndex(CONTENT_TYPE)));
                resource.setMimeType(cursor.getString(cursor.getColumnIndex(MIME_TYPE)));
                resource.setSize(cursor.getString(cursor.getColumnIndex(SIZE)));

                String positionId = cursor.getString(cursor.getColumnIndex(POSITION_ID));
                if(positionId != null && !positionId.trim().equalsIgnoreCase("")){
                    resource.setPosition(pdh.getPositionById(positionId));
                }
                resource.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(CREATED_ON)));
                resource.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
                resource.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));
                resource.setPath(cursor.getString(cursor.getColumnIndex(FILE_PATH)));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return resource;
    }

    public Resource getDriveResourceByPositionId(String positionId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        Resource resource = new Resource();
        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE "
                            + POSITION_ID + "=? AND "
                            + D_ACTION + "<> 'delete'", new String[]{positionId});
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                resource.setUniqueId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                resource.setAreaId(cursor.getString(cursor.getColumnIndex(AREA_ID)));
                resource.setUserId(cursor.getString(cursor.getColumnIndex(USER_ID)));
                resource.setContainerId(cursor.getString(cursor.getColumnIndex(CONTAINER_ID)));
                resource.setResourceId(cursor.getString(cursor.getColumnIndex(RESOURCE_ID)));
                resource.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                resource.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                resource.setContentType(cursor.getString(cursor.getColumnIndex(CONTENT_TYPE)));
                resource.setMimeType(cursor.getString(cursor.getColumnIndex(MIME_TYPE)));
                resource.setSize(cursor.getString(cursor.getColumnIndex(SIZE)));
                resource.setPosition(null);
                resource.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(CREATED_ON)));
                resource.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
                resource.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));
                resource.setPath(cursor.getString(cursor.getColumnIndex(FILE_PATH)));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return resource;
    }

    public Resource getDriveResourceRoot(String contentType, Area area) {
        Resource childResource = new Resource();
        PositionsDBHelper pdh = new PositionsDBHelper(context);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME
                            + " WHERE " + AREA_ID + "=? AND "
                            + TYPE + "='folder' AND "
                            + CONTENT_TYPE + "=?",
                    new String[]{area.getUniqueId(), contentType});
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                Resource resource = new Resource();
                resource.setUniqueId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                resource.setAreaId(cursor.getString(cursor.getColumnIndex(AREA_ID)));
                resource.setUserId(cursor.getString(cursor.getColumnIndex(USER_ID)));
                resource.setContainerId(cursor.getString(cursor.getColumnIndex(CONTAINER_ID)));
                resource.setResourceId(cursor.getString(cursor.getColumnIndex(RESOURCE_ID)));
                resource.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                resource.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                resource.setContentType(cursor.getString(cursor.getColumnIndex(CONTENT_TYPE)));
                resource.setMimeType(cursor.getString(cursor.getColumnIndex(MIME_TYPE)));
                resource.setSize(cursor.getString(cursor.getColumnIndex(SIZE)));

                String positionId = cursor.getString(cursor.getColumnIndex(POSITION_ID));
                if(positionId != null && !positionId.trim().equalsIgnoreCase("")){
                    resource.setPosition(pdh.getPositionById(positionId));
                }
                resource.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(CREATED_ON)));
                childResource = resource;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return childResource;
    }

    public Map<String, Resource> getCommonResourcesByName() {
        Map<String, Resource> resources = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE "
                            + CONTENT_TYPE + "=? AND "
                            + AREA_ID + "=''",
                    new String[]{"folder"});
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    Resource dr = new Resource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(USER_ID)));
                    dr.setContainerId(cursor.getString(cursor.getColumnIndex(CONTAINER_ID)));
                    dr.setResourceId(cursor.getString(cursor.getColumnIndex(RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                    dr.setContentType("folder");
                    dr.setMimeType("application/vnd.google-apps.folder");
                    dr.setAreaId("");
                    dr.setSize("0");
                    dr.setPosition(null);
                    dr.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(CREATED_ON)));

                    resources.put(dr.getName(), dr);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return resources;
    }

    public List<Resource> fetchImageResources(Area area) {
        List<Resource> resources = new ArrayList<>();
        PositionsDBHelper pdh = new PositionsDBHelper(context);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE "
                            + AREA_ID + "=? AND "
                            + TYPE + "='file' AND "
                            + CONTENT_TYPE + "='Image' AND "
                            + D_ACTION + "<>'delete'",
                    new String[]{area.getUniqueId()});

            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    Resource resource = new Resource();
                    resource.setUniqueId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                    resource.setAreaId(cursor.getString(cursor.getColumnIndex(AREA_ID)));
                    resource.setUserId(cursor.getString(cursor.getColumnIndex(USER_ID)));
                    resource.setContainerId(cursor.getString(cursor.getColumnIndex(CONTAINER_ID)));
                    resource.setResourceId(cursor.getString(cursor.getColumnIndex(RESOURCE_ID)));
                    resource.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                    resource.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                    resource.setContentType(cursor.getString(cursor.getColumnIndex(CONTENT_TYPE)));
                    resource.setMimeType(cursor.getString(cursor.getColumnIndex(MIME_TYPE)));
                    resource.setSize(cursor.getString(cursor.getColumnIndex(SIZE)));

                    String positionId = cursor.getString(cursor.getColumnIndex(POSITION_ID));
                    if(positionId != null && !positionId.trim().equalsIgnoreCase("")){
                        resource.setPosition(pdh.getPositionById(positionId));
                    }
                    resource.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(CREATED_ON)));
                    resource.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
                    resource.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));
                    resource.setPath(cursor.getString(cursor.getColumnIndex(FILE_PATH)));

                    resources.add(resource);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return resources;
    }

    public ArrayList<Resource> getDirtyResources() {
        ArrayList<Resource> resources = new ArrayList<>();
        PositionsDBHelper pdh = new PositionsDBHelper(context);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE "
                            + DIRTY + "= 1 ", null);
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    Resource dr = new Resource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                    dr.setAreaId(cursor.getString(cursor.getColumnIndex(AREA_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(USER_ID)));
                    dr.setContainerId(cursor.getString(cursor.getColumnIndex(CONTAINER_ID)));
                    dr.setResourceId(cursor.getString(cursor.getColumnIndex(RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                    dr.setContentType(cursor.getString(cursor.getColumnIndex(CONTENT_TYPE)));
                    dr.setMimeType(cursor.getString(cursor.getColumnIndex(MIME_TYPE)));
                    dr.setSize(cursor.getString(cursor.getColumnIndex(SIZE)));

                    String positionId = cursor.getString(cursor.getColumnIndex(POSITION_ID));
                    if(positionId != null && !positionId.trim().equalsIgnoreCase("")){
                        dr.setPosition(pdh.getPositionById(positionId));
                    }
                    dr.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(CREATED_ON)));
                    dr.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
                    dr.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));
                    dr.setPath(cursor.getString(cursor.getColumnIndex(FILE_PATH)));

                    resources.add(dr);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return resources;
    }

    public ArrayList<Resource> getUploadableDirtyResources(String areaId) {
        ArrayList<Resource> resources = new ArrayList<>();
        PositionsDBHelper pdh = new PositionsDBHelper(context);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE "
                    + AREA_ID + "=? AND "
                    + DIRTY + "=1 AND "
                    + D_ACTION + "=?", new String[]{areaId, "upload"});
            if ((cursor != null) && (cursor.getCount() > 0)){
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    Resource dr = new Resource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(UNIQUE_ID)));
                    dr.setAreaId(cursor.getString(cursor.getColumnIndex(AREA_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(USER_ID)));
                    dr.setContainerId(cursor.getString(cursor.getColumnIndex(CONTAINER_ID)));
                    dr.setResourceId(cursor.getString(cursor.getColumnIndex(RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                    dr.setContentType(cursor.getString(cursor.getColumnIndex(CONTENT_TYPE)));
                    dr.setMimeType(cursor.getString(cursor.getColumnIndex(MIME_TYPE)));
                    dr.setSize(cursor.getString(cursor.getColumnIndex(SIZE)));

                    String positionId = cursor.getString(cursor.getColumnIndex(POSITION_ID));
                    if(positionId != null && !positionId.trim().equalsIgnoreCase("")){
                        dr.setPosition(pdh.getPositionById(positionId));
                    }
                    dr.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(CREATED_ON)));
                    dr.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY)));
                    dr.setDirtyAction(cursor.getString(cursor.getColumnIndex(D_ACTION)));
                    dr.setPath(cursor.getString(cursor.getColumnIndex(FILE_PATH)));

                    resources.add(dr);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return resources;
    }

    private JSONObject preparePostParams(String queryType, Resource dr) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("requestType", "DriveMaster");
            postParams.put("query_type", queryType);
            postParams.put("device_id", AndroidSystemUtil.getDeviceId(context));
            postParams.put(AREA_ID, dr.getAreaId());
            postParams.put(USER_ID, dr.getUserId());
            postParams.put(UNIQUE_ID, dr.getUniqueId());
            postParams.put(CONTAINER_ID, dr.getContainerId());
            postParams.put(RESOURCE_ID, dr.getResourceId());
            postParams.put(NAME, dr.getName());
            postParams.put(TYPE, dr.getType());
            postParams.put(CONTENT_TYPE, dr.getContentType());
            postParams.put(MIME_TYPE, dr.getMimeType());
            postParams.put(SIZE, dr.getSize());

            Position position = dr.getPosition();
            if(position != null){
                postParams.put(POSITION_ID, position.getUniqueId());
            }else {
                postParams.put(POSITION_ID, "");
            }
            postParams.put(CREATED_ON, dr.getCreatedOnMillis());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deleteResourcesByAreaId(String areaId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE "
                + AREA_ID + " = '" + areaId + "'");
        db.close();
    }

    public void deleteResourceByGlobally(Resource resource) {
        deleteResourceLocally(resource);
        deleteResourceFromServer(resource);
    }

    public void deleteResourceLocally(Resource resource) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE "
                + AREA_ID + "='" + resource.getAreaId() + "' and "
                + NAME + "='" + resource.getName() + "'");
        db.close();
    }

    public void cleanLocalDriveResources() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, DIRTY + " = 0 ", null);
        db.close();
    }

}