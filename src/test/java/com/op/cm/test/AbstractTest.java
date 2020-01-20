package com.op.cm.test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.op.cm.api.ICommunicationService;
import com.op.cm.models.EventType;
import com.op.cm.models.Message;
import com.op.cm.models.Room;
import com.op.cm.services.ConnectionManager;
import com.op.cm.util.Util;
import org.java_websocket.WebSocket;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by jalagari on 18/01/20.
 */
public class AbstractTest {

    protected static ConnectionManager connectionManager;
    protected static ICommunicationService communicationService;
    protected static WebSocket webSocket;

    protected void verifyNotification(String roomName, String message, int noOfSessions) throws JsonProcessingException {

        ArgumentCaptor<Collection> clientsArg = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<String> notificationArg = ArgumentCaptor.forClass(String.class);
        Mockito.verify(connectionManager, Mockito.atLeastOnce()).broadcast(notificationArg.capture(), clientsArg.capture());

        assertNotNull("Expected notification argument ", message);
        Message notifyMessage = Util.getObjectMapper().readValue(notificationArg.getValue(), Message.class);
        assertEquals("Expected Notification Type Event", EventType.NOTIFICATION, notifyMessage.getEventType());
        assertEquals("Expected Notification in " + roomName, roomName, notifyMessage.getRoomName());
        assertEquals("Expected Notification to " + noOfSessions + " sessions" , noOfSessions, clientsArg.getValue().size());
    }

    protected void verifyNotification(Message expected, Message actual) {
        assertNotNull("Expected Message Obj", expected);
        assertNotNull("Expected Message Obj", actual);
        assertEquals("Expected msg ", expected.getData(), actual.getData());
        assertEquals("Expected Type Event", expected.getEventType(), actual.getEventType());
        assertEquals("Expected Room name in " + expected.getRoomName(), expected.getRoomName(), actual.getRoomName());
        assertEquals("Expected User name in " + expected.getUserName(), expected.getUserName(), actual.getUserName());

    }

    protected void verifyMessage(String message, Message expectedMessage) throws JsonProcessingException {

        assertNotNull("Expected notification argument ", message);
        Message notifyMessage = Util.getObjectMapper().readValue(message, Message.class);
        assertEquals("Expected Notification Type Event", expectedMessage.getEventType(), notifyMessage.getEventType());
        assertEquals("Expected Notification in Room 1", expectedMessage.getRoomName(), notifyMessage.getRoomName());
        assertEquals("Expected Notification Message not matching", expectedMessage.getData(), notifyMessage.getData());
    }

    protected Collection<Room> sendMessage(Message message) throws JsonProcessingException {
        String joinMessageStr = Util.getObjectMapper().writeValueAsString(message);
        communicationService.parse(webSocket, joinMessageStr);
        return communicationService.getRooms();
    }

    protected ListAppender getLogger(Class trackingClass) {

        Logger classLogger = (Logger) LoggerFactory.getLogger(trackingClass);
        ListAppender listAppender = new ListAppender<>();
        listAppender.start();

        classLogger.addAppender(listAppender);
        return listAppender;
    }
    protected String getMessage(ListAppender listAppender, int index) throws Exception {
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
