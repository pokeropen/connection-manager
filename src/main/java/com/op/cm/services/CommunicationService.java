package com.op.cm.services;


import com.op.cm.api.ICommunicationService;
import com.op.cm.models.EventType;
import com.op.cm.models.Message;
import com.op.cm.models.Player;
import com.op.cm.models.Room;

import java.util.HashMap;
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
        if(roomName != null && (player != null && player.isValid())) {

            Room room = getRoom(roomName);
            // If its new room then include it in list
            if(room == null) {
                room = new Room(roomName);
                rooms.put(roomName, room);
            }

            // Add user in room
            room.addPlayer(player);

            // Send notification to room about new player
            String message = String.format("User %s joined", player.getUsername());
            sendAction(room, Message.of(EventType.NOTIFICATION, message));
        }
    }

    @Override
    public void exit(String roomName, Player player) {
        if(roomName != null && (player != null && player.isValid())) {

            Room room = getRoom(roomName);

            // remove user in room
            room.removePlayer(player);

            // Send notification to room about new player
            String message = String.format("User %s Left", player.getUsername());
            sendAction(room, Message.of(EventType.NOTIFICATION, message));
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
        logger.info(String.format("Received message %s from player %s", player.getUsername(), message.getData()));
    }
}
