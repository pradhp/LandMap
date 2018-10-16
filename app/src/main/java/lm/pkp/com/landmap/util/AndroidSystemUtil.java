package lm.pkp.com.landmap.util;

import android.content.Context;

import java.lang.reflect.Method;
import android.provider.Settings.Secure;

/**
 * Created by USER on 10/24/2017.
 */
public class AndroidSystemUtil {

    public static final String getDeviceId(Context localContext) {
        String deviceID = Secure.getString(localContext.getApplicationContext().getContentResolver(),Secure.ANDROID_ID);
        return deviceID;
    }
}
