package com.pearnode.app.placero.user;

import com.google.gson.Gson;

/**
 * Created by USER on 10/24/2017.
 */
public class User {

    private String displayName;
    private String email;
    private String familyName;
    private String givenName;
    private String photoUrl;
    private String authSystemId;

    private UserPersistableSelections selections = new UserPersistableSelections();

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFamilyName() {
        return this.familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return this.givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAuthSystemId() {
        return this.authSystemId;
    }

    public void setAuthSystemId(String authSystemId) {
        this.authSystemId = authSystemId;
    }

    public UserPersistableSelections getSelections() {
        return this.selections;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this).toString();
    }

}
