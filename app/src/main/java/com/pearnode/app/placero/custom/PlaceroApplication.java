package com.pearnode.app.placero.custom;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import java.lang.reflect.Method;

/**
 * Created by USER on 11/10/2017.
 */
public class PlaceroApplication extends Application {

    public static PlaceroApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
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
