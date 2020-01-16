package com.op.cm.client;

import com.op.cm.services.ConnectionManager;

/**
 * Created by jalagari on 17/01/20.
 */
public class ConnectionManagerServer extends ConnectionManager {
    
    private ConnectionManagerServer(Integer port) {
        super(port);
    }
}
