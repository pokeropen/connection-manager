package com.op.cm.test;

import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.op.cm.api.ICommunicationService;
import com.op.cm.client.RetryPolicy;
import com.op.cm.models.EventType;
import com.op.cm.models.Message;
import com.op.cm.models.Player;
import com.op.cm.models.Room;
import com.op.cm.services.CommunicationService;
import com.op.cm.services.ConnectionManager;
import com.op.cm.util.ErrorMessages;
import org.java_websocket.WebSocket;

import static org.junit.Assert.*;

import org.java_websocket.framing.Framedata;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by jalagari on 14/01/20.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectionManager.class)
public class CommunicationServiceTest extends AbstractTest {



    @BeforeClass
    public static void setup() throws JsonProcessingException {
        communicationService = new CommunicationService();
        connectionManager = Mockito.mock(ConnectionManager.class);
        webSocket = Mockito.mock(WebSocket.class);
    }


    @Before
    public void beforeEach() {
        PowerMockito.mockStatic(ConnectionManager.class);

        PowerMockito.when(ConnectionManager.getInstance()).thenReturn(connectionManager);
        PowerMockito.when(ConnectionManager.getInstance(Mockito.anyInt())).thenReturn(connectionManager);
    }

    @After
    public void cleanUp() {
        webSocket.close();
        communicationService.getRooms().clear();
    }

    @Test
    public void testJoinWithInvalidRoomName() throws Exception {

        ListAppender communicationServiceLog = getLogger(ICommunicationService.class);
        Message joinMessage = Message.of(EventType.JOIN, "", "user1",null);
        sendMessage(joinMessage);
        assertEquals("Expected error in log", ErrorMessages.INVALID_ROOM_NAME.getName(), getMessage(communicationServiceLog, 0));

        joinMessage = Message.of(EventType.JOIN, null, "user1",null);
        sendMessage(joinMessage);
        assertEquals("Expected error in log", ErrorMessages.INVALID_ROOM_NAME.getName(), getMessage(communicationServiceLog, 1));
    }

    @Test
    public void testJoinWithInvalidPlayerName() throws Exception {

        ListAppender communicationServiceLog = getLogger(ICommunicationService.class);
        Message joinMessage = Message.of(EventType.JOIN, "Room 1", "",null);
        sendMessage(joinMessage);
        assertEquals("Expected error in log", ErrorMessages.INVALID_USER_NAME.getName(), getMessage(communicationServiceLog, 0));

        joinMessage = Message.of(EventType.JOIN, "Room 1", null,null);
        sendMessage(joinMessage);
        assertEquals("Expected error in log", ErrorMessages.INVALID_USER_NAME.getName(), getMessage(communicationServiceLog, 1));
    }

    @Test
    public void testJoin() throws Exception {

        Message joinMessage = Message.of(EventType.JOIN, "Room 1", "Player 1",null);
        Collection<Room> rooms = sendMessage(joinMessage);

        // Validate Room Details
        assertEquals("Expected Room added ",1, rooms.size());
        Room room = rooms.stream().findFirst().get();
        assertEquals("Expected Room name 'Room 1'", joinMessage.getRoomName(), room.getName());

        //Validate Player Details
        assertEquals("Expected Player added to Room ",1, room.getConnectedPlayers().size());
        assertEquals("Expected Player connection added to Room ",1, room.getConnectionSize());
        Player player = room.getConnectedPlayers().get(0);
        assertEquals("Expected player name 'Player 1'", joinMessage.getUserName(), player.getUsername());
        assertEquals("Expected player session", webSocket, player.getConnection());

    }

    @Test
    public void testLeaveWithInvalidRoomName() throws Exception {

        ListAppender communicationServiceLog = getLogger(ICommunicationService.class);
        Message joinMessage = Message.of(EventType.EXIST, "", "Player 1",null);
        sendMessage(joinMessage);
        assertEquals("Expected error in log", ErrorMessages.INVALID_ROOM_NAME.getName(), getMessage(communicationServiceLog, 0));


        joinMessage = Message.of(EventType.EXIST, null, "Player 1",null);
        sendMessage(joinMessage);
        assertEquals("Expected error in log", ErrorMessages.INVALID_ROOM_NAME.getName(), getMessage(communicationServiceLog, 1));


        Message.of(EventType.EXIST, "Unknow Room", "Player 1",null);
        sendMessage(joinMessage);
        assertEquals("Expected error in log", ErrorMessages.INVALID_ROOM_NAME.getName(), getMessage(communicationServiceLog, 2));
    }

    @Test
    public void testLeaveWithInvalidPlayerName() throws Exception {
        Message joinMessage = Message.of(EventType.JOIN, "Room 1", "Player 1",null);
        sendMessage(joinMessage);

        ListAppender communicationServiceLog = getLogger(ICommunicationService.class);
        joinMessage = Message.of(EventType.EXIST, "Room 1", "",null);
        sendMessage(joinMessage);
        assertEquals("Expected error in log", ErrorMessages.INVALID_USER_NAME.getName(), getMessage(communicationServiceLog, 0));


        joinMessage = Message.of(EventType.EXIST, "Room 1", null,null);
        sendMessage(joinMessage);
        assertEquals("Expected error in log", ErrorMessages.INVALID_USER_NAME.getName(), getMessage(communicationServiceLog, 1));
    }

    @Test
    public void testLeave() throws Exception {
        Collection<Room> rooms = communicationService.getRooms();
        assertEquals("Expected Room added ",0, rooms.size());
        assertNotNull("Expected rooms list", rooms);

        Message joinMessage = Message.of(EventType.JOIN, "Room 1", "Player 1",null);
        rooms = sendMessage(joinMessage);
        Room room = rooms.stream().findFirst().get();
        assertNotNull("Expected room", room);
        assertEquals("Expected Player already exists in Room ",1, room.getConnectedPlayers().size());
        assertEquals("Expected Player connection already exists to Room ",1, room.getConnectionSize());


        joinMessage = Message.of(EventType.EXIST, "Room 1", "Player 1",null);
        rooms = sendMessage(joinMessage);

        room = rooms.stream().findFirst().get();
        assertNotNull("Expected room", room);
        assertEquals("Expected Player removed from Room ",0, room.getConnectedPlayers().size());
        assertEquals("Expected Player connection removed from Room ",0, room.getConnectionSize());
    }

    @Test
    public void testActionReceived() throws Exception {
        ListAppender communicationServiceLog = getLogger(ICommunicationService.class);
        Message message = Message.of(EventType.ACTION, "Room 1", "Player 1","Testing Action Received");
        sendMessage(message);
        assertEquals("Expected error in log",
                String.format("Received message '%s' from player '%s' in room '%s'", message.getData(), message.getUserName(), message.getRoomName()),
                getMessage(communicationServiceLog, 0));

    }

    @Test
    public void testSendAction() throws Exception {
        Player player = new Player("P1", webSocket);
        Message message = Message.of(EventType.ACTION, "Room 1", player.getUsername(),"Testing Send Action");
        communicationService.sendAction(player, message);

        ArgumentCaptor<String> notificationArg = ArgumentCaptor.forClass(String.class);
        Mockito.verify(webSocket, Mockito.atLeastOnce()).send(notificationArg.capture());

        verifyMessage(notificationArg.getValue(), message);
    }

    @Test
    public void testNotification() throws Exception {

        Message joinMessage = Message.of(EventType.JOIN, "Room 1", "Player 1",null);
        Collection<Room> rooms = sendMessage(joinMessage);

        // Validate Room Details
        assertEquals("Expected Room added ",1, rooms.size());

        joinMessage = Message.of(EventType.JOIN, "Room 1", "Player 2",null);
        sendMessage(joinMessage);
        verifyNotification(joinMessage.getRoomName(), "Player 2 joined room", 2);

        joinMessage = Message.of(EventType.JOIN, "Room 2", "Player 3",null);
        sendMessage(joinMessage);
        verifyNotification(joinMessage.getRoomName(), "Player 3 joined room", 1);

        joinMessage = Message.of(EventType.EXIST, "Room 1", "Player 2",null);
        sendMessage(joinMessage);
        verifyNotification(joinMessage.getRoomName(), "Player 2 left room", 1);
    }
}
