package com.op.cm.launch;

import com.op.cm.services.ConnectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * Created by jalagari on 14/01/20.
 */
public class Launch {

    public static void main(String args[]) throws IOException, InterruptedException {
        Integer port = null;
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
            port = ConnectionManager.DEFAULT_PORT;
        }
        System.out.println("Starting Server");
        ConnectionManager connectionManager = ConnectionManager.getInstance( port );
        connectionManager.start();

        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String in = sysin.readLine();
            connectionManager.broadcast( in );
            if( in.equals( "exit" ) ) {
                connectionManager.stop(1000);
                break;
            }
        }
    }
}
