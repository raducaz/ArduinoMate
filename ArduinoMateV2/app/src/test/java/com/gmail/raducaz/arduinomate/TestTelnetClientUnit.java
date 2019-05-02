package com.gmail.raducaz.arduinomate;

import com.gmail.raducaz.arduinomate.telnet.TelnetClient;

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
        assertEquals(4, 2 + 2);

        try {
            final TelnetClient telnetClient = new TelnetClient("192.168.100.100", 8080,
                    "test", "test");
            Map<String, Object> map = new HashMap<>();
            String[] commands = new String[]{"[{\"?13\":0}]"};
            long start = System.currentTimeMillis();
            telnetClient.executeCommands(Arrays.asList(commands), map);
            long end = System.currentTimeMillis();
            NumberFormat formatter = new DecimalFormat("#0.00000");
            System.out.println("Batch execution time is " + formatter.format((end - start) / 1000d) + " seconds");
            System.out.println("Result =" + map);
        }
        catch (Exception exc)
        {}
    }
}