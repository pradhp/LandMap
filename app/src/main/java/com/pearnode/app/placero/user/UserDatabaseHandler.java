package com.pearnode.app.placero.user;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pearnode.app.placero.util.AndroidSystemUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class UserDatabaseHandler extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "com.pearnode.app.placero.db";
    private Context localContext;

    public static final String TABLE_NAME = "user_master";
    public static final String DISPLAY_NAME = "display_name";
    public static final String EMAIL = "email";
    public static final String FAMILY_NAME = "family_name";
    public static final String GIVEN_NAME = "given_name";
    public static final String PHOTO_URL = "photo_url";
    public static final String AUTH_SYS_ID = "auth_sys_id";

    public UserDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.localContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                        DISPLAY_NAME + " text, " +
                        EMAIL + " text," +
                        FAMILY_NAME + " text," +
                        GIVEN_NAME + " text, " +
                        PHOTO_URL + " text, " +
                        AUTH_SYS_ID + " text )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public void dryRun(){
        SQLiteDatabase db = getReadableDatabase();
        db.close();
    }

}