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

public class TagsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "com.pearnode.app.placero.db";
    public static final String TAG_TABLE_NAME = "tag_master";
    public static final String TAG_COLUMN_NAME = "name";
    public static final String TAG_COLUMN_TYPE = "type";
    public static final String TAG_COLUMN_TYPE_FIELD = "type_field";
    public static final String TAG_COLUMN_CONTEXT = "context";
    public static final String TAG_COLUMN_CONTEXT_ID = "context_id";
    private AsyncTaskCallback callback;

    public TagsDBHelper(Context context, AsyncTaskCallback callback) {
        super(context, DATABASE_NAME, null, 1);
        this.callback = callback;
    }

    public TagsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " +
                        TAG_TABLE_NAME + "(" +
                        TAG_COLUMN_NAME + " text," +
                        TAG_COLUMN_TYPE + " text," +
                        TAG_COLUMN_TYPE_FIELD + " text," +
                        TAG_COLUMN_CONTEXT + " text, " +
                        TAG_COLUMN_CONTEXT_ID + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TAG_TABLE_NAME);
        this.onCreate(db);
    }

    public void insertTagsLocally(List<TagElement> elements, String context, String contextId) {
        SQLiteDatabase db = getWritableDatabase();
        for(TagElement tagElement : elements){
            ContentValues contentValues = new ContentValues();
            if(context.equalsIgnoreCase("user")){
                contentValues.put(TAG_COLUMN_CONTEXT_ID, contextId);
                contentValues.put(TAG_COLUMN_CONTEXT, "user");
            }else {
                contentValues.put(TAG_COLUMN_CONTEXT_ID, contextId);
                contentValues.put(TAG_COLUMN_CONTEXT, "area");
            }
            contentValues.put(TAG_COLUMN_NAME, tagElement.getName());
            contentValues.put(TAG_COLUMN_TYPE, tagElement.getType());
            contentValues.put(TAG_COLUMN_TYPE_FIELD, tagElement.getTypeField());
            db.insert(TAG_TABLE_NAME, null, contentValues);
        }
        db.close();
    }

    public void insertTagsToServer(List<TagElement> tagElements, String context, String contextId) {
        for (TagElement tagElement: tagElements){
            TagInsertAsyncTask task = new TagInsertAsyncTask(callback);
            task.execute(preparePostParams(tagElement, context, contextId));
        }
    }

    private JSONObject preparePostParams(TagElement tagElement, String context, String contextId) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("name", tagElement.getName());
            postParams.put("type", tagElement.getType());
            postParams.put("type_field", tagElement.getTypeField());
            postParams.put("context", context);
            postParams.put("context_id", contextId);
            postParams.put("query_type", "insert");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public ArrayList<TagElement> getTagsByContext(String context){
        ArrayList<TagElement> tagElements = new ArrayList<TagElement>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TAG_TABLE_NAME + " WHERE " + TAG_COLUMN_CONTEXT + "=?",
                new String[]{context});
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                TagElement te = new TagElement();
                te.setName(cursor.getString(cursor.getColumnIndex(TAG_COLUMN_NAME)));
                te.setContext(cursor.getString(cursor.getColumnIndex(TAG_COLUMN_CONTEXT)));
                te.setContextId(cursor.getString(cursor.getColumnIndex(TAG_COLUMN_CONTEXT_ID)));
                te.setType(cursor.getString(cursor.getColumnIndex(TAG_COLUMN_TYPE)));
                te.setTypeField(cursor.getString(cursor.getColumnIndex(TAG_COLUMN_TYPE_FIELD)));
                if(!tagElements.contains(te)){
                    tagElements.add(te);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return tagElements;
    }

    public void deleteTagsByContext(String context, String contextId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TAG_TABLE_NAME, TAG_COLUMN_CONTEXT + "=? AND " + TAG_COLUMN_CONTEXT_ID + "=?",
                new String[]{context, contextId});
        db.close();
    }

    public void deleteAllTagsLocally() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TAG_TABLE_NAME, "1", null);
        db.close();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        this.callback.taskCompleted("");
    }

}