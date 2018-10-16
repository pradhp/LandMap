package com.pearnode.app.placero.user;

import com.pearnode.app.placero.custom.UserUnavailableException;

/**
 * Created by USER on 10/24/2017.
 */
public class UserContext {

    private static final UserContext ourInstance = new UserContext();

    public static UserContext getInstance() {
        return UserContext.ourInstance;
    }

    private UserContext() {
    }

    private UserElement userElement;

    public UserElement getUserElement() {
        if (this.userElement == null) {
            throw new UserUnavailableException();
        }
        return this.userElement;
    }

    public void setUserElement(UserElement userElement) {
        this.userElement = userElement;
    }
}
