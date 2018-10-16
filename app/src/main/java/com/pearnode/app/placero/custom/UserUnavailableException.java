package com.pearnode.app.placero.custom;

/**
 * Created by USER on 11/11/2017.
 */
public class UserUnavailableException extends RuntimeException {

    public UserUnavailableException() {
    }

    public UserUnavailableException(Exception e) {
        super(e);
    }

    public UserUnavailableException(String message) {
        super(message);
    }
}
