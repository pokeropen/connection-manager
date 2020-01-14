package com.op.cm.services;


import com.op.cm.api.ICommunicationService;
import com.op.cm.models.EventType;
import com.op.cm.models.Message;
import com.op.cm.models.Player;
import com.op.cm.models.Room;
import com.op.cm.util.ErrorMessages;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jalagari on 14/01/20.
 */
public class CommunicationService implements ICommunicationService {

    private Map<String, Room> rooms;

    public CommunicationService() {
        rooms = new HashMap<String, Room>();
    }

    private Room getRoom(String roomName) {
       return rooms.get(roomName);
    }


    @Override
    public void join(String roomName, Player player) {

        if(isValid(roomName, player)) {
            Room room = getRoom(roomName);
            // If its new room then include it in list
            if(room == null) {
                room = new Room(roomName);
                rooms.put(roomName, room);
            }

            // Add user in room
            room.addPlayer(player);

            // Send notification to room about new player
            String message = String.format("%s joined room", player.getUsername());
            sendAction(room, Message.of(EventType.NOTIFICATION, roomName, message));
        }
    }

    @Override
    public void exit(String roomName, Player player) {
        Room room = getRoom(roomName);
        if(isValid(room, player)) {
            // remove user in room
            room.removePlayer(player);

            // Send notification to room about new player
            String message = String.format("%s left room", player.getUsername());
            sendAction(room, Message.of(EventType.NOTIFICATION, roomName, message));
        }
    }

    @Override
    public void sendAction(Room room, Message message) {
        room.broadcast(encode(message));
    }

    @Override
    public void sendAction(Player player, Message message) {
        player.send(encode(message));
    }

    @Override
    public void receivedAction(Player player, Message message) {
        logger.info(String.format("Received message '%s' from player '%s' in room '%s'", message.getData(), message.getUserName(), message.getRoomName()));
    }

    @Override
    public Collection<Room> getRooms() {
        return rooms.values();
    }

    private boolean isValid(Room room, Player player) {
        if(room == null) {
            logger.error(ErrorMessages.INVALID_ROOM_NAME.getName());
            return false;
        } else {
            return isValid(room.getName(), player);
        }
    }

    private boolean isValid(String roomName, Player player) {
        String message = null;
        if(StringUtils.isBlank(roomName)) {
            message = ErrorMessages.INVALID_ROOM_NAME.getName();
        } else if(player == null || !player.isValid()) {
            message = ErrorMessages.INVALID_USER_NAME.getName();
        }

        if(message != null) {
            logger.error(message);
        }

        return  message == null;
    }
}
