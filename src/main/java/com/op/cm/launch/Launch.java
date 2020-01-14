package com.op.cm.launch;

import com.op.cm.services.ConnectionManager;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by jalagari on 14/01/20.
 */
public class Launch {

    public static void main(String args[]) throws IOException, InterruptedException {
        int port = Integer.parseInt(null);
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }
        ConnectionManager.start(port);
    }
}
