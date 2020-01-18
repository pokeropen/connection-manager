package com.op.cm.client;

import com.op.cm.models.Room;
import com.op.cm.services.ConnectionManager;

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

    public Collection<Room> getRooms() {
        return communicationService.getRooms();
    }
}
