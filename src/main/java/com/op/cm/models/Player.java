package com.op.cm.models;

import org.apache.commons.lang3.StringUtils;
import org.java_websocket.WebSocket;

/**
 * Created by jalagari on 14/01/20.
 */
public class Player {

    private String username = null;
    private WebSocket connection = null;

    public Player(String username, WebSocket connection) {
        this.username = username;
        this.connection = connection;
    }

    public boolean isValid() {
        return StringUtils.isNoneBlank(username) && connection != null;
    }

    public String getUsername() {
        return username;
    }

    public WebSocket getConnection() {
        return connection;
    }

    public void send(String message) {
        connection.send(message);
    }
}
