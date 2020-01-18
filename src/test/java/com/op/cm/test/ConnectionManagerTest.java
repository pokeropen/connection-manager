package com.op.cm.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.op.cm.client.ConnectionManagerClient;
import com.op.cm.client.ConnectionManagerServer;
import com.op.cm.models.EventType;
import com.op.cm.models.Message;
import com.op.cm.models.Room;
import com.op.cm.services.ConnectionManager;
import com.op.cm.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * Created by jalagari on 17/01/20.
 */
public class ConnectionManagerTest {

    ConnectionManagerServer server;
    ConnectionManagerClient client;

    @Before
    public void beforeEach() throws URISyntaxException, InterruptedException {
        server = new ConnectionManagerServer();
        server.start();
        server.countDownLatch.await();
        client = new ConnectionManagerClient();
    }

    @After
    public void afterEach() throws IOException, InterruptedException {
        server.stop();
    }

    @Test
    public void testClientConnection() throws InterruptedException, URISyntaxException, IOException {
        client.connectBlocking();
        assertTrue( "webSocket.isOpen()", client.isOpen() );
        client.getSocket().close();
        client.countDownLatch.await();
        assertTrue( "!webSocket.isOpen()", !client.isOpen() );
    }

    @Test
    public void testEmptyRoom() {
        Collection<Room> rooms = server.getRooms();
        assertNotNull("Expected room list", rooms);
        assertEquals("Expected empty room", 0, rooms.size());
    }

    @Test
    public void testRoomCreation() throws InterruptedException, JsonProcessingException {
        client.connectBlocking();
        Message message = Message.of(EventType.JOIN, "Room 1", "User 1",  null);
        client.send(encode(message));
        Thread.sleep(1000);
        Collection<Room> rooms = server.getRooms();
        assertNotNull("Expected room list", rooms);
        assertEquals("Expected empty room", 1, rooms.size());
    }

    private String encode(Message message) throws JsonProcessingException {
        return Util.getObjectMapper().writeValueAsString(message);
    }
}
