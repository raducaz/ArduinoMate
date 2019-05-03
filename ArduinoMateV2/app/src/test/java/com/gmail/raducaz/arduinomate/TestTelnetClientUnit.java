package com.gmail.raducaz.arduinomate;

import android.util.Log;

import com.gmail.raducaz.arduinomate.processes.TaskInterface;
import com.gmail.raducaz.arduinomate.telnet.TelnetClient;
import com.gmail.raducaz.arduinomate.ui.TaskExecutor;

import org.junit.Test;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TestTelnetClientUnit {
    @Test
    public void TestSendMessage() {

        String command = "[{\"=6\":0,\"@\":500},{\"!\":2000},{\"=2\":0},{\"!\":1000},{\"=8\":1,\"@\":2000},{\"=7\":0,\"@\":500}]";

        final TelnetClient telnetClient = new TelnetClient("192.168.100.100", 8080,
                "test", "test");
        Map<String, Object> map = new HashMap<>();
        telnetClient.executeCommand(command, map);

        Object result = map.get(command);
        Log.d("Test", result == null ? "" : result.toString());
    }
}
