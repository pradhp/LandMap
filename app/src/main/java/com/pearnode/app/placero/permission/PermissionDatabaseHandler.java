package com.pearnode.app.placero.permission;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

import com.pearnode.app.placero.custom.AsyncTaskCallback;

public class PermissionDatabaseHandler extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "com.pearnode.app.placero.db";
    public static final String TABLE_NAME = "area_access";
    public static final String AREA_ID = "area_id";
    public static final String USER_ID = "user_id";
    public static final String FUNCTION_CODE = "function_code";
    private static final String DIRTY_FLAG = "dirty";
    private static final String DIRTY_ACTION = "d_action";

    private Context context;

    public PermissionDatabaseHandler(Context context, AsyncTaskCallback callback) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    public PermissionDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_NAME + "(" +
                        AREA_ID + " text," +
                        USER_ID + " text, " +
                        DIRTY_FLAG + " integer DEFAULT 0," +
                        DIRTY_ACTION + " text," +
                        FUNCTION_CODE + " text)"
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

    public Permission addPermission(Permission pe) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_ID, pe.getAreaId());
        contentValues.put(FUNCTION_CODE, pe.getFunctionCode());
        contentValues.put(USER_ID, pe.getUserId());
        contentValues.put(DIRTY_ACTION, pe.getDirtyAction());
        contentValues.put(DIRTY_ACTION, pe.getDirtyAction());

        db.insertOrThrow(TABLE_NAME, null, contentValues);
        db.close();
        return pe;
    }

    public void deletePermissionsByAreaId(String areaId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + AREA_ID + " = '" + areaId + "'");
        db.close();
    }

    public Map<String, Permission> fetchPermissionsByAreaId(String areaId) {
        SQLiteDatabase db = getWritableDatabase();
        Map<String, Permission> perMap = new HashMap<>();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE " + AREA_ID + "=?", new String[]{areaId});
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                Permission pe = new Permission();

                pe.setUserId(cursor.getString(cursor.getColumnIndex(USER_ID)));
                pe.setAreaId(areaId);

                String functionCode = cursor.getString(cursor.getColumnIndex(FUNCTION_CODE));
                pe.setFunctionCode(functionCode);
                perMap.put(functionCode, pe);

                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return perMap;
    }

    public void deletePermissionsLocally() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, DIRTY_FLAG + " = 0 ", null);
        db.close();
    }
}