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
    public static final String TABLE_NAME = "tag_master";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String TYPE_FIELD = "type_field";
    public static final String CONTEXT = "context";
    public static final String CONTEXT_ID = "context_id";
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
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_NAME + "(" +
                        NAME + " text," +
                        TYPE + " text," +
                        TYPE_FIELD + " text," +
                        CONTEXT + " text, " +
                        CONTEXT_ID + " text)"
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

    public void insertTagsLocally(List<TagElement> elements, String context, String contextId) {
        SQLiteDatabase db = getWritableDatabase();
        for(TagElement tagElement : elements){
            ContentValues contentValues = new ContentValues();
            if(context.equalsIgnoreCase("user")){
                contentValues.put(CONTEXT_ID, contextId);
                contentValues.put(CONTEXT, "user");
            }else {
                contentValues.put(CONTEXT_ID, contextId);
                contentValues.put(CONTEXT, "area");
            }
            contentValues.put(NAME, tagElement.getName());
            contentValues.put(TYPE, tagElement.getType());
            contentValues.put(TYPE_FIELD, tagElement.getTypeField());
            db.insert(TABLE_NAME, null, contentValues);
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
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE " + CONTEXT + "=?",
                new String[]{context});
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                TagElement te = new TagElement();
                te.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                te.setContext(cursor.getString(cursor.getColumnIndex(CONTEXT)));
                te.setContextId(cursor.getString(cursor.getColumnIndex(CONTEXT_ID)));
                te.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                te.setTypeField(cursor.getString(cursor.getColumnIndex(TYPE_FIELD)));
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
        db.delete(TABLE_NAME, CONTEXT + "=? AND " + CONTEXT_ID + "=?",
                new String[]{context, contextId});
        db.close();
    }

    public void deleteAllTagsLocally() {
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