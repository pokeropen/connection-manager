package com.op.cm.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.op.cm.util.Util;
import com.op.cm.api.ICommunicationService;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Created by jalagari on 13/01/20.
 */
public class ConnectionManager extends WebSocketServer {

    private static ConnectionManager connectionManager = null;
    private static Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    public static int DEFAULT_PORT = 9898;

    protected ICommunicationService communicationService = null;
    private ObjectMapper objectMapper = null;

    protected ConnectionManager(Integer port) {
        super( new InetSocketAddress( port ) );
        init();
    }

    protected void init() {
        objectMapper = Util.getObjectMapper();
        communicationService = Util.getCommunicationService();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("New user connected");
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket webSocket, String data) {
        communicationService.parse(webSocket, data);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        logger.error("Received Error ", e);
    }

    @Override
    public void onStart() {
        System.out.println( "Connection Manager started on port: " + getPort() );
    }

    public static ConnectionManager getInstance(int port) {
        Optional<Integer> portNo = Optional.of(port);
        if(connectionManager == null) {
            connectionManager = new ConnectionManager(portNo.orElse(DEFAULT_PORT));
        }
        return connectionManager;
    }

    public static ConnectionManager getInstance() {
        return getInstance(DEFAULT_PORT);
    }

    public static void start(int port) throws IOException, InterruptedException {
        ConnectionManager connectionManager = getInstance( port );
        connectionManager.start();

        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String in = sysin.readLine();
            connectionManager.broadcast( in );
            if( in.equals( "exit" ) ) {
                connectionManager.stop(1000);
                break;
            }
        }
    }
}
