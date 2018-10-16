package com.pearnode.app.placero.custom;

import android.app.Application;
import android.content.Context;

/**
 * Created by USER on 11/10/2017.
 */
public class PlaceroApplication extends Application {

    public static PlaceroApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        PlaceroApplication.instance = this;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public static PlaceroApplication getInstance() {
        return PlaceroApplication.instance;
    }
}
