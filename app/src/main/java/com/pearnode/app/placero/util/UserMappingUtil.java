package com.pearnode.app.placero.util;

import android.net.Uri;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.pearnode.app.placero.user.User;

/**
 * Created by USER on 10/24/2017.
 */
public class UserMappingUtil {

    public static final User convertGoogleAccountToLocalAccount(GoogleSignInAccount acct) {
        User ue = new User();
        ue.setDisplayName(acct.getDisplayName());
        ue.setEmail(acct.getEmail());
        ue.setFamilyName(acct.getFamilyName());
        ue.setGivenName(acct.getGivenName());
        ue.setAuthSystemId(acct.getServerAuthCode());
        Uri photoUri = acct.getPhotoUrl();
        if (photoUri != null) {
            ue.setPhotoUrl(photoUri.toString());
        } else {
            ue.setPhotoUrl("");
        }
        return ue;
    }
}
