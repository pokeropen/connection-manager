package com.op.cm.test;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.op.cm.api.ICommunicationService;
import com.op.cm.models.EventType;
import com.op.cm.models.Message;
import com.op.cm.models.Player;
import com.op.cm.models.Room;
import com.op.cm.services.CommunicationService;
import com.op.cm.services.ConnectionManager;
import com.op.cm.util.ErrorMessages;
import com.op.cm.util.Util;
import javafx.beans.binding.When;
import org.java_websocket.WebSocket;

import static org.junit.Assert.*;

import org.java_websocket.framing.Framedata;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
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
public class CommunicationServiceTest {

    private static ICommunicationService communicationService;
    private static ConnectionManager connectionManager;

    @Mock
    private WebSocket webSocket;


    @BeforeClass
    public static void setup() throws JsonProcessingException {
        communicationService = new CommunicationService();
        connectionManager = Mockito.mock(ConnectionManager.class);

    }

    @Before
    public void beforeEach() {
        PowerMockito.mockStatic(ConnectionManager.class);

        PowerMockito.when(ConnectionManager.getInstance()).thenReturn(connectionManager);
        PowerMockito.when(ConnectionManager.getInstance(Mockito.anyInt())).thenReturn(connectionManager);
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

        ListAppender communicationServiceLog = getLogger(ICommunicationService.class);
        Message joinMessage = Message.of(EventType.EXIST, "Room 1", "",null);
        sendMessage(joinMessage);
        assertEquals("Expected error in log", ErrorMessages.INVALID_USER_NAME.getName(), getMessage(communicationServiceLog, 0));


        joinMessage = Message.of(EventType.EXIST, "Room 1", null,null);
        sendMessage(joinMessage);
        assertEquals("Expected error in log", ErrorMessages.INVALID_USER_NAME.getName(), getMessage(communicationServiceLog, 1));
    }

    @Test
    public void testLeave() throws Exception {
        Collection<Room> rooms = communicationService.getRooms();
        assertEquals("Expected Room added ",1, rooms.size());
        assertNotNull("Expected rooms list", rooms);

        Room room = rooms.stream().findFirst().get();
        assertNotNull("Expected room", room);
        assertEquals("Expected Player already exists in Room ",1, room.getConnectedPlayers().size());
        assertEquals("Expected Player connection already exists to Room ",1, room.getConnectionSize());


        Message joinMessage = Message.of(EventType.EXIST, "Room 1", "Player 1",null);
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
        verifyNotification(joinMessage, "Player 2 joined room", 2);

        joinMessage = Message.of(EventType.JOIN, "Room 2", "Player 3",null);
        sendMessage(joinMessage);
        verifyNotification(joinMessage, "Player 3 joined room", 1);

        joinMessage = Message.of(EventType.EXIST, "Room 1", "Player 2",null);
        sendMessage(joinMessage);
        verifyNotification(joinMessage, "Player 2 left room", 1);
    }

    private void verifyNotification(Message expectedMessage, String message, int noOfSessions) throws JsonProcessingException {

        ArgumentCaptor<Collection> clientsArg = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<String> notificationArg = ArgumentCaptor.forClass(String.class);
        Mockito.verify(connectionManager, Mockito.atLeastOnce()).broadcast(notificationArg.capture(), clientsArg.capture());

        assertNotNull("Expected notification argument ", message);
        Message notifyMessage = Util.getObjectMapper().readValue(notificationArg.getValue(), Message.class);
        assertEquals("Expected Notification Type Event", EventType.NOTIFICATION, notifyMessage.getEventType());
        assertEquals("Expected Notification in Room 1", expectedMessage.getRoomName(), notifyMessage.getRoomName());
        assertEquals("Expected Notification to " + noOfSessions + " sessions" , noOfSessions, clientsArg.getValue().size());
    }

    private void verifyMessage(String message, Message expectedMessage) throws JsonProcessingException {

        assertNotNull("Expected notification argument ", message);
        Message notifyMessage = Util.getObjectMapper().readValue(message, Message.class);
        assertEquals("Expected Notification Type Event", expectedMessage.getEventType(), notifyMessage.getEventType());
        assertEquals("Expected Notification in Room 1", expectedMessage.getRoomName(), notifyMessage.getRoomName());
        assertEquals("Expected Notification Message not matching", expectedMessage.getData(), notifyMessage.getData());
    }


    private Collection<Room> sendMessage(Message message) throws JsonProcessingException {
        String joinMessageStr = Util.getObjectMapper().writeValueAsString(message);
        communicationService.parse(webSocket, joinMessageStr);
        return communicationService.getRooms();
    }


    private ListAppender getLogger(Class trackingClass) {

        Logger classLogger = (Logger) LoggerFactory.getLogger(trackingClass);
        ListAppender listAppender = new ListAppender<>();
        listAppender.start();

        classLogger.addAppender(listAppender);
        return listAppender;
    }

    private String getMessage(ListAppender listAppender, int index) throws Exception {
        List<ILoggingEvent> logsList = listAppender.list;
        if(index < logsList.size()) {
            ILoggingEvent event = logsList.get(index);
            if(event != null) {
                return event.getMessage();
            }
        }
        throw new Exception("Log message not found");
    }
}