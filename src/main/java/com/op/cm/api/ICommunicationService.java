package com.op.cm.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.op.cm.models.Message;
import com.op.cm.models.Player;
import com.op.cm.models.Room;
import com.op.cm.util.Util;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jalagari on 14/01/20.
 */
public interface ICommunicationService {

    static Logger logger = LoggerFactory.getLogger(ICommunicationService.class);

    void join(String roomName, Player player);

    void exit(String roomName, Player player);

    void sendAction(Room room, Message message);

    void sendAction(Player player, Message message);

    void receivedAction(Player player, Message message);

    default void parse(WebSocket client, String data) {
        Message message = encode(data);
        Player player = new Player(message.getUserName(), client);

        switch (message.getEventType()) {
            case JOIN:
                join(message.getRoomName(), player);
                break;

            case EXIST:
                exit(message.getRoomName(), player);
                break;
            case ACTION:
                receivedAction(player, message);
                break;
        }
    }

    default Message encode(String data) {
        Message message = null;
        try {
             message = Util.getObjectMapper().readValue(data, Message.class);
        } catch (JsonProcessingException e) {
            logger.error("Error while parsing json string");
        }
        return message;
    }

    default String encode(Message message) {
        String result = null;
        try {
            result = Util.getObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            logger.error("Error while convert message to json string");
        }
        return result;
    }

}
