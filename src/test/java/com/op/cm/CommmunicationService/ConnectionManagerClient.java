package com.op.cm.CommmunicationService;

import com.op.cm.services.ConnectionManager;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by jalagari on 14/01/20.
 */
public class ConnectionManagerClient extends WebSocketClient {

    public ConnectionManagerClient() throws URISyntaxException {
        super(new URI("ws://localhost:"+ ConnectionManager.DEFAULT_PORT));
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

    }

    @Override
    public void onMessage(String s) {

    }

    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @Override
    public void onError(Exception e) {

    }
}
