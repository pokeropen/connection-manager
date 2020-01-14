package com.op.cm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.op.cm.util.Util;
import org.slf4j.LoggerFactory;

/**
 * Created by jalagari on 14/01/20.
 */
public class Message {


    private EventType eventType;
    private String data;
    private String userName;
    private String roomName;

    // For serialization
    private Message() {

    }

    private Message(EventType eventType, String roomName, String userName, String message) {
        this.eventType = eventType;
        this.data = data;
        this.userName = userName;
        this.roomName = roomName;
    }

    public static Message of(EventType eventType, String roomName, String userName, String message) {
        return new Message(eventType, message, userName, roomName);
    }

    public static Message of(EventType eventType, String message) {
        return new Message(eventType, message, null, null);
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getData() {
        return data;
    }

    public String getUserName() {
        return userName;
    }

    public String getRoomName() {
        return roomName;
    }
}