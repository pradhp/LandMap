package com.pearnode.app.placero.sync.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.pearnode.app.placero.area.tasks.DirtyAreaSyncTask;
import com.pearnode.app.placero.media.tasks.DirtyMediaSyncTask;
import com.pearnode.app.placero.position.tasks.DirtyPositionsSyncTask;
import com.pearnode.common.TaskFinishedListener;

public class OfflineElementsSyncTask extends AsyncTask<Object, Void, String> {

    private Context context;
    private TaskFinishedListener finishedListener;

    public OfflineElementsSyncTask(Context context, TaskFinishedListener listener) {
        this.context = context;
        this.finishedListener = listener;
    }

    protected String doInBackground(Object... params) {
        String result = null;
        try {
            DirtyAreaSyncTask dirtyAreaSyncTask = new DirtyAreaSyncTask(context, null);
            dirtyAreaSyncTask.execute();

            DirtyPositionsSyncTask dirtyPositionsSyncTask = new DirtyPositionsSyncTask(context, null);
            dirtyPositionsSyncTask.execute();

            DirtyMediaSyncTask dirtyMediaSyncTask = new DirtyMediaSyncTask(context, null);
            dirtyMediaSyncTask.execute();

            result = "SUCCESS";
        } catch (Exception e) {
            result = "FAILURE";
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if(finishedListener != null){
            finishedListener.onTaskFinished(result);
        }
    }
}
