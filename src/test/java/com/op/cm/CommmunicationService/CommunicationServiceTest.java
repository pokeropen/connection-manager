package com.op.cm.CommmunicationService;

import com.op.cm.services.ConnectionManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by jalagari on 14/01/20.
 */


public class CommunicationServiceTest {

    static Thread thread = null;

    @BeforeClass
    public static void setup() throws IOException, InterruptedException {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Setup Connection Manager instance
                try {
                    ConnectionManager.start(ConnectionManager.DEFAULT_PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    @AfterClass
    public static void tearDown() {
        thread.stop();
    }

    @Test
    public void testConnection() throws URISyntaxException, InterruptedException {

       ConnectionManagerClient client1 = new ConnectionManagerClient();
       client1.connect();
       Assert.assertTrue("Expected client1 to connected", client1.isOpen());

    }
}
