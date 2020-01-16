package com.op.cm.test;

import com.op.cm.client.ConnectionManagerClient;
import com.op.cm.services.ConnectionManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.net.URISyntaxException;

/**
 * Created by jalagari on 17/01/20.
 */
public class ConnectionManagerTest {

    ConnectionManager server;
    ConnectionManagerClient client;

    @Before
    public void beforeEach() throws URISyntaxException {
        server = ConnectionManager.getInstance(ConnectionManager.DEFAULT_PORT);
        server.start();
        client = new ConnectionManagerClient();
    }

    @Test
    public void testClientConnection() {
        client.connect();
        Thr
        assertTrue("Expected Client is connected", client.connected);
    }
}
