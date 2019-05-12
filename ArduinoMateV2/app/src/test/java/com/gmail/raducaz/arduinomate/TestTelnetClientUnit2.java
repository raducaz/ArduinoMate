package com.gmail.raducaz.arduinomate;

import android.util.Log;

import com.gmail.raducaz.arduinomate.telnet.TelnetClient;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TestTelnetClientUnit2 {
    @Test
    public void TestSendMessage() {

        Thread t1 = new Thread(){
            public void run(){
                String command = "[{\"=6\":0,\"@\":500},{\"!\":2000},{\"=2\":0},{\"!\":1000},{\"=8\":1,\"@\":2000},{\"=7\":0,\"@\":500}]";

                final TelnetClient telnetClient = new TelnetClient("192.168.1.100", 8080,
                        "test", "test");
                Map<String, Object> map = new HashMap<>();
                telnetClient.executeCommand(command, map);

                Object result = map.get(command);
                Log.d("Test", result == null ? "" : result.toString());
            }
        };

// my second thread
        Thread t2 = new Thread(){
            public void run(){
                String command = "[{\"=6\":0,\"@\":500},{\"!\":2000},{\"=2\":0},{\"!\":1000},{\"=8\":1,\"@\":2000},{\"=7\":0,\"@\":500}]";

                final TelnetClient telnetClient = new TelnetClient("192.168.1.100", 8080,
                        "test", "test");
                Map<String, Object> map = new HashMap<>();
                telnetClient.executeCommand(command, map);

                Object result = map.get(command);
                Log.d("Test", result == null ? "" : result.toString());
            }
        };

        t1.start();
        t2.start();

    }
}
