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
        ConnectionManager connectionManager = null;
        Integer port = null;
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }
        try {
           ConnectionManager.start(ConnectionManager.DEFAULT_PORT);
            connectionManager = ConnectionManager.getInstance();
        } catch (Exception e) {

            System.out.println("Error while starting serer ");
            e.printStackTrace();
        }
    }
}
