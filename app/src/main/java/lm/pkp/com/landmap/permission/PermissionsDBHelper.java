package lm.pkp.com.landmap.permission;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.connectivity.ConnectivityChangeReceiver;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GlobalContext;
import lm.pkp.com.landmap.sync.LMSRestAsyncTask;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;

public class PermissionsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    public static final String ACCESS_TABLE_NAME = "area_access";
    public static final String ACCESS_COLUMN_AREA_ID = "area_id";
    public static final String ACCESS_COLUMN_USER_ID = "user_id";
    public static final String ACCESS_COLUMN_FUNCTION_CODE = "function_code";
    private static final String ACCESS_COLUMN_DIRTY_FLAG = "dirty";
    private static final String ACCESS_COLUMN_DIRTY_ACTION = "d_action";

    private AsyncTaskCallback callback;
    private Context localContext;
    public PermissionsDBHelper(Context context, AsyncTaskCallback callback) {
        super(context, DATABASE_NAME, null, 1);
        this.callback = callback;
        this.localContext = context;
    }

    public PermissionsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.localContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " +
                        ACCESS_TABLE_NAME + "(" +
                        ACCESS_COLUMN_AREA_ID + " text," +
                        ACCESS_COLUMN_USER_ID + " text, " +
                        ACCESS_COLUMN_DIRTY_FLAG + " integer DEFAULT 0," +
                        ACCESS_COLUMN_DIRTY_ACTION + " text," +
                        ACCESS_COLUMN_FUNCTION_CODE + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ACCESS_TABLE_NAME);
        this.onCreate(db);
    }

    public PermissionElement insertPermissionLocally(PermissionElement pe) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ACCESS_COLUMN_AREA_ID, pe.getAreaId());
        contentValues.put(ACCESS_COLUMN_FUNCTION_CODE, pe.getFunctionCode());
        contentValues.put(ACCESS_COLUMN_USER_ID, pe.getUserId());
        contentValues.put(ACCESS_COLUMN_DIRTY_ACTION, pe.getDirtyAction());
        contentValues.put(ACCESS_COLUMN_DIRTY_ACTION, pe.getDirtyAction());

        db.insertOrThrow(ACCESS_TABLE_NAME, null, contentValues);
        db.close();
        return pe;
    }

    public boolean insertPermissionsToServer(String targetUser, String functionCodes) {
        boolean networkAvailable = (new Boolean(GlobalContext.INSTANCE.get(GlobalContext.INTERNET_AVAILABLE))
                || ConnectivityChangeReceiver.isConnected(localContext) );
        if (networkAvailable) {
            new LMSRestAsyncTask(callback)
                    .execute(preparePostParams("insert", targetUser, functionCodes));
        }
        return networkAvailable;
    }

    private JSONObject preparePostParams(String queryType, String targetUser, String functionCodes) {
        JSONObject postParams = new JSONObject();
        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        UserElement userElement = UserContext.getInstance().getUserElement();
        try {
            postParams.put("requestType", "AreaShare");
            postParams.put("query_type", queryType);
            postParams.put("source_user", userElement.getEmail());
            postParams.put("target_user", targetUser);
            postParams.put("area_id", areaElement.getUniqueId());
            postParams.put("function_codes", functionCodes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deletePermissionsByAreaId(String areaId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + ACCESS_TABLE_NAME + " WHERE " + ACCESS_COLUMN_AREA_ID + " = '" + areaId + "'");
        db.close();
    }

    public Map<String, PermissionElement> fetchPermissionsByAreaId(String areaId) {
        SQLiteDatabase db = getWritableDatabase();
        Map<String, PermissionElement> perMap = new HashMap<>();
        Cursor cursor = db.rawQuery("select * from " + ACCESS_TABLE_NAME + " WHERE " + ACCESS_COLUMN_AREA_ID + "=?", new String[]{areaId});
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                PermissionElement pe = new PermissionElement();

                pe.setUserId(cursor.getString(cursor.getColumnIndex(ACCESS_COLUMN_USER_ID)));
                pe.setAreaId(areaId);

                String functionCode = cursor.getString(cursor.getColumnIndex(ACCESS_COLUMN_FUNCTION_CODE));
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
        db.delete(ACCESS_TABLE_NAME, ACCESS_COLUMN_DIRTY_FLAG + " = 0 ", null);
        db.close();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        this.callback.taskCompleted("");
    }

}