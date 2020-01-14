package com.op.cm.util;

/**
 * Created by jalagari on 15/01/20.
 */
public enum  ErrorMessages {

    INVALID_ROOM_NAME ("Invalid Room name"),
    INVALID_USER_NAME("Invalid User name");

    private String name;

    ErrorMessages(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
