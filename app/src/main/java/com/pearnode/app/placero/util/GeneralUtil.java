package com.pearnode.app.placero.util;

import android.util.Patterns;

/**
 * Created by USER on 11/11/2017.
 */
public class GeneralUtil {

    private GeneralUtil() {

    }

    public static final boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static final String dbHost = "35.200.243.66";
}
