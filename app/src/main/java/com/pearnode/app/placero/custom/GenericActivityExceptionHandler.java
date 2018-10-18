package com.pearnode.app.placero.custom;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.lang.Thread.UncaughtExceptionHandler;

import com.pearnode.app.placero.google.signin.SignInActivity;

/**
 * Created by USER on 11/8/2017.
 */
public class GenericActivityExceptionHandler implements UncaughtExceptionHandler {

    private final Activity activity;

    public GenericActivityExceptionHandler(Activity context) {
        activity = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        StringBuffer content = new StringBuffer();
        content.append("\n\n Exception trace ! \n");

        StackTraceElement[] exceptionTrace = ex.getStackTrace();
        for (int i = 0; i < exceptionTrace.length; i++) {
            content.append(exceptionTrace[i]);
            content.append("\n");
        }

        content.append("\n\n Thread trace ! \n");
        StackTraceElement[] threadStackTrace = thread.getStackTrace();
        for (int i = 0; i < threadStackTrace.length; i++) {
            content.append(threadStackTrace[i]);
            content.append("\n");
        }

        ex.printStackTrace();
    }

}