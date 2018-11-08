package com.pearnode.app.placero.tags;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.pearnode.app.placero.custom.AsyncTaskCallback;

public class TagDatabaseHandler extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "com.pearnode.app.placero.db";
    public static final String TABLE_NAME = "tag_master";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String TYPE_FIELD = "type_field";
    public static final String CONTEXT = "context";
    public static final String CONTEXT_ID = "context_id";
    private static final String DIRTY_FLAG = "dirty";
    private static final String DIRTY_ACTION = "d_action";
    private static final String CREATED_ON = "con";

    private AsyncTaskCallback callback;

    public TagDatabaseHandler(Context context, AsyncTaskCallback callback) {
        super(context, DATABASE_NAME, null, 1);
        this.callback = callback;
    }

    public TagDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_NAME + "(" +
                        NAME + " text," +
                        TYPE + " text," +
                        TYPE_FIELD + " text," +
                        CONTEXT + " text, " +
                        CONTEXT_ID + " text, " +
                        DIRTY_FLAG    + " integer DEFAULT 0," +
                        DIRTY_ACTION  + " text," +
                        CREATED_ON    + " long"
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

    public void addTag(Tag tag) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTEXT_ID, tag.getContextId());
        contentValues.put(CONTEXT, tag.getContext());
        contentValues.put(NAME, tag.getName());
        contentValues.put(TYPE, tag.getType());
        contentValues.put(TYPE_FIELD, tag.getTypeField());
        contentValues.put(DIRTY_FLAG, tag.getDirty());
        contentValues.put(DIRTY_ACTION, tag.getDirtyAction());
        contentValues.put(CREATED_ON, tag.getCreatedOn());
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }

    public void addTags(List<Tag> elements, String context, String contextId) {
        SQLiteDatabase db = getWritableDatabase();
        for(Tag tag : elements){
            ContentValues contentValues = new ContentValues();
            if(context.equalsIgnoreCase("user")){
                contentValues.put(CONTEXT_ID, contextId);
                contentValues.put(CONTEXT, "user");
            }else {
                contentValues.put(CONTEXT_ID, contextId);
                contentValues.put(CONTEXT, "area");
            }
            contentValues.put(NAME, tag.getName());
            contentValues.put(TYPE, tag.getType());
            contentValues.put(TYPE_FIELD, tag.getTypeField());
            contentValues.put(DIRTY_FLAG, tag.getDirty());
            contentValues.put(DIRTY_ACTION, tag.getDirtyAction());
            contentValues.put(CREATED_ON, tag.getCreatedOn());
            db.insert(TABLE_NAME, null, contentValues);
        }
        db.close();
    }

    public ArrayList<Tag> getTagsByContext(String context){
        ArrayList<Tag> tags = new ArrayList<Tag>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE " + CONTEXT + "=?",
                new String[]{context});
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                Tag tag = new Tag();
                tag.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                tag.setContext(cursor.getString(cursor.getColumnIndex(CONTEXT)));
                tag.setContextId(cursor.getString(cursor.getColumnIndex(CONTEXT_ID)));
                tag.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                tag.setTypeField(cursor.getString(cursor.getColumnIndex(TYPE_FIELD)));
                tag.setDirty(cursor.getInt(cursor.getColumnIndex(DIRTY_FLAG)));
                tag.setDirtyAction(cursor.getString(cursor.getColumnIndex(DIRTY_ACTION)));
                tag.setCreatedOn(cursor.getLong(cursor.getColumnIndex(CREATED_ON)));
                tags.add(tag);
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return tags;
    }

    public void deleteTagsByContext(String context, String contextId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, CONTEXT + "=? AND " + CONTEXT_ID + "=?",
                new String[]{context, contextId});
        db.close();
    }

    public void deleteAllTags() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "1", null);
        db.close();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        this.callback.taskCompleted("");
    }

}