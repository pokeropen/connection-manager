package com.op.cm.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.op.cm.models.Message;
import com.op.cm.models.Player;
import com.op.cm.models.Room;
import com.op.cm.services.ConnectionManager;
import com.op.cm.util.Util;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * Created by jalagari on 17/01/20.
 */
public class ConnectionManagerServer extends ConnectionManager {

    public CountDownLatch countDownLatch = new CountDownLatch( 1 );
    public ConnectionManagerServer() {
        super(DEFAULT_PORT);
        communicationService.getRooms().clear();
    }

    public void onStart() {
        super.onStart();
        countDownLatch.countDown();
    }

    public void clear() {
        getRooms().clear();
    }


    public Collection<Room> getRooms() {
        return communicationService.getRooms();
    }

    public void sendAction(Room room, Message message) throws JsonProcessingException {
        System.out.println("Sending Message " + Util.getObjectMapper().writeValueAsString(message));
        communicationService.sendAction(room, message);
    }

    public void sendAction(Player player, Message message) throws JsonProcessingException {
        System.out.println("Sending Message " + Util.getObjectMapper().writeValueAsString(message));
        communicationService.sendAction(player, message);
    }
}
