package com.op.cm.test;

import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.op.cm.api.ICommunicationService;
import com.op.cm.client.ConnectionManagerClient;
import com.op.cm.client.ConnectionManagerServer;
import com.op.cm.models.EventType;
import com.op.cm.models.Message;
import com.op.cm.models.Player;
import com.op.cm.models.Room;
import com.op.cm.util.ErrorMessages;
import com.op.cm.util.Util;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
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
public class ConnectionManagerTest extends AbstractTest {

    ConnectionManagerServer server;
    ConnectionManagerClient client;
    ConnectionManagerClient client2;

    int timeout = 1000;

    @Before
    public void beforeEach() throws URISyntaxException, InterruptedException {
        server = new ConnectionManagerServer();
        server.start();
        server.countDownLatch.await();
        client = new ConnectionManagerClient();
        client2 = new ConnectionManagerClient();
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
        verifyRoomAndPlayer(0, 0);
    }

    @Test
    public void testRoomCreation() throws InterruptedException, JsonProcessingException {
        connectAndJoin(client, "User 1");
        verifyRoomAndPlayer(1, 1);
    }

    @Test
    public void testUserJoinNotification() throws InterruptedException, IOException, URISyntaxException {
        connectAndJoin(client, "User 1");
        verifyRoomAndPlayer(1,1);

        connectAndJoin(client2, "User 2");
        verifyRoomAndPlayer(1,2);

        assertNotNull("Expected message",   client.lastMsg);
        Message expected = Message.of(EventType.NOTIFICATION, "Room 1", null, "User 2 joined room");
        verifyNotification(expected, client.lastMsg );
    }

    @Test
    public void testUserExitNotification() throws InterruptedException, IOException, URISyntaxException {
        testUserJoinNotification();
        client2.closeBlocking();
        client2.countDownLatch.await();
        Thread.sleep(timeout);
        assertNotNull("Expected message",   client.lastMsg);
        Message expected = Message.of(EventType.NOTIFICATION, "Room 1", null, "User 2 left room");
        verifyNotification(expected, client.lastMsg );

    }

    @Test
    public void testServerSendRoomAction() throws IOException, InterruptedException, URISyntaxException {
        connectAndJoin(client, "User 1");

        Collection<Room> rooms = server.getRooms();
        verifyRoomAndPlayer(1, 1);
        Room room = rooms.stream().findFirst().get();
        Message message = Message.of(EventType.ACTION, room.getName(), "New User", "Have you Recieved message");
        sendAction(room, message);
        verifyNotification(message, client.lastMsg);
    }

    @Test
    public void testServerSendPlayerAction() throws IOException, InterruptedException, URISyntaxException {
        connectAndJoin(client, "User 1");

        Collection<Room> rooms = server.getRooms();
        verifyRoomAndPlayer(1, 1);
        Room room = rooms.stream().findFirst().get();
        Player player = room.getConnectedPlayers().get(0);
        Message message = Message.of(EventType.ACTION, room.getName(), "New User", "Have you Recieved message");
        sendAction(player, message);
        verifyNotification(message, client.lastMsg);
    }

    @Test
    public void testClientSendAction() throws Exception {
        connectAndJoin(client, "User 1");
        Message message = Message.of(EventType.ACTION, "Room 1", "New User", "Send ack");

        ListAppender communicationServiceLog = getLogger(ICommunicationService.class);
        sendMessage(client, message);
        Thread.sleep(timeout);
        assertEquals("Expected message",
                "Received message '"+ message.getData() +"' from player '"+ message.getUserName() + "' in room '" + message.getRoomName()+"'",
                getMessage(communicationServiceLog, 0));

    }

    private void verifyRoomAndPlayer(int noOfRooms, int noOfPlayers) {
        Collection<Room> rooms = server.getRooms();
        assertNotNull("Expected room list", rooms);
        assertEquals("Expected room", noOfRooms, rooms.size());
        if(noOfRooms > 0) {
            Room room = rooms.stream().findFirst().get();
            assertNotNull("expected room", room);
            assertEquals("Players in Room", noOfPlayers, room.getConnectedPlayers().size());
            assertEquals("Connection in Room", noOfPlayers, room.getConnectionSize());
        }
    }

    private void connectAndJoin(WebSocketClient client, String userName) throws InterruptedException, JsonProcessingException {
        client.connectBlocking();
        assertTrue( "webSocket.isOpen()", client.isOpen() );
        Message message = Message.of(EventType.JOIN, "Room 1", userName,  null);
        sendMessage(client, message);
    }

    private void sendMessage(WebSocketClient client, Message message) throws InterruptedException, JsonProcessingException {
        client.send(encode(message));
        Thread.sleep(timeout);
    }
    private void sendAction (Room room, Message message) throws InterruptedException, JsonProcessingException {
        server.sendAction(room, message);
        Thread.sleep(timeout);
    }

    private void sendAction (Player player, Message message) throws InterruptedException, JsonProcessingException {
        server.sendAction(player, message);
        Thread.sleep(timeout);
    }

    private String encode(Message message) throws JsonProcessingException {
        return Util.getObjectMapper().writeValueAsString(message);
    }
}
