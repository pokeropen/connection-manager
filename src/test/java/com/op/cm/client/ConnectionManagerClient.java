package com.op.cm.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.op.cm.models.Message;
import com.op.cm.services.ConnectionManager;
import com.op.cm.util.Util;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by jalagari on 14/01/20.
 */
public class ConnectionManagerClient extends WebSocketClient {

    public List<Message> receivedMsg = new ArrayList<>();
    public Message lastMsg = null;
    public boolean anyError = false;
    public boolean connected = false;
    public CountDownLatch countDownLatch = new CountDownLatch( 1 );


    public ConnectionManagerClient() throws URISyntaxException {
        super(new URI("ws://localhost:"+ ConnectionManager.DEFAULT_PORT));
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        connected = true;
    }

    @Override
    public void onMessage(String msg) {
        try {
            lastMsg = Util.getObjectMapper().readValue(msg, Message.class);
            receivedMsg.add(lastMsg);
        } catch (JsonProcessingException e) {
            System.out.println( "Error while conversion -" + e.getMessage());
            anyError = true;
        }

    }

    @Override
    public void onClose(int i, String s, boolean b) {
        connected = false;
        countDownLatch.countDown();
    }

    @Override
    public void onError(Exception e) {
        anyError = true;
    }
}
