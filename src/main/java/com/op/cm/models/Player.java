package com.op.cm.models;

import org.java_websocket.WebSocket;

/**
 * Created by jalagari on 14/01/20.
 */
public class Player {

    private String username = null;
    private WebSocket session = null;

    public Player(String username, WebSocket session) {
        this.username = username;
        this.session = session;
    }

    public boolean isValid() {
        return username != null;
    }

    public String getUsername() {
        return username;
    }

    public WebSocket getSession() {
        return session;
    }

    public void send(String message) {
        session.send(message);
    }
}
