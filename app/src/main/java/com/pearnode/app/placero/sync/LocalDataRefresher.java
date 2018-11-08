package com.pearnode.app.placero.sync;

import android.content.Context;
import android.os.AsyncTask;

import com.pearnode.app.placero.area.db.AreaDatabaseHandler;
import com.pearnode.app.placero.area.tasks.PublicAreasLoadTask;
import com.pearnode.app.placero.area.tasks.SharedAreasLoadTask;
import com.pearnode.app.placero.area.tasks.UserAreaDetailsLoadTask;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.permission.PermissionDatabaseHandler;
import com.pearnode.app.placero.position.PositionDatabaseHandler;
import com.pearnode.app.placero.tags.TagDatabaseHandler;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.task.UserTagsLoadingTask;
import com.pearnode.common.TaskFinishedListener;

import org.json.JSONObject;

/**
 * Created by USER on 11/4/2017.
 */
public class LocalDataRefresher implements AsyncTaskCallback {

    private Context context;
    private AsyncTaskCallback callback;

    public LocalDataRefresher(Context context, AsyncTaskCallback caller) {
        this.context = context;
        this.callback = caller;
    }

    public void refreshLocalData() {
        cleanCurrentData();

        UserAreaDetailsLoadTask userAreasLoadTask = new UserAreaDetailsLoadTask(this.context);
        userAreasLoadTask.setCompletionCallback(this);
        try {
            JSONObject queryObj = new JSONObject();
            queryObj.put("us", UserContext.getInstance().getUser().getEmail());
            userAreasLoadTask.execute(queryObj);

            UserTagsLoadingTask tagsLoadingTask = new UserTagsLoadingTask(context, null);
            tagsLoadingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanCurrentData() {
        AreaDatabaseHandler adh = new AreaDatabaseHandler(this.context);
        adh.dryRun();
        adh.deleteAllAreas();

        PositionDatabaseHandler pdh = new PositionDatabaseHandler(this.context);
        pdh.dryRun();
        pdh.deleteAllPositions();

        MediaDataBaseHandler ddh = new MediaDataBaseHandler(this.context);
        ddh.dryRun();
        ddh.deleteAllMedia();

        PermissionDatabaseHandler pmh = new PermissionDatabaseHandler(this.context);
        pmh.dryRun();
        pmh.deleteAllPermissions();

        TagDatabaseHandler tdh = new TagDatabaseHandler(this.context);
        tdh.dryRun();
        tdh.deleteAllTags();
    }

    public void refreshPublicAreas() {
        AreaDatabaseHandler adh = new AreaDatabaseHandler(this.context);
        adh.deletePublicAreas();

        PublicAreasLoadTask loadTask = new PublicAreasLoadTask(this.context);
        loadTask.setCompletionCallback(this);
        try {
            loadTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshSharedAreas() {
        AreaDatabaseHandler adh = new AreaDatabaseHandler(this.context);
        adh.deleteSharedAreas();

        SharedAreasLoadTask loadTask = new SharedAreasLoadTask(this.context);
        loadTask.setCompletionCallback(this);
        try {
            loadTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshPublicAreas(String searchKey) {
        AreaDatabaseHandler adh = new AreaDatabaseHandler(this.context);
        adh.deletePublicAreas();

        PublicAreasLoadTask loadTask = new PublicAreasLoadTask(this.context);
        try {
            JSONObject queryObj = new JSONObject();
            queryObj.put("sk", searchKey);
            loadTask.execute(queryObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadTask.setCompletionCallback(this);
    }

    @Override
    public void taskCompleted(Object result) {
        if(callback != null){
            this.callback.taskCompleted(result);
        }
    }

}
