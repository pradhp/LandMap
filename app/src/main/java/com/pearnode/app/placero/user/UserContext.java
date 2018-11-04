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

    private User user;

    public User getUser() {
        if (this.user == null) {
            throw new UserUnavailableException();
        }
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
