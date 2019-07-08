package com.gmail.raducaz.arduinomate.TestProcesses;

import com.gmail.raducaz.arduinomate.commands.ArduinoCommander;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class SimulateTapOpen {
    @Test
    public void TestSendMessage() {

        String command = "[{\"=6\":0}]";

        ArduinoCommander arduinoCommander = new ArduinoCommander("192.168.100.12", 8081);
        String result = arduinoCommander.SendCommand(command);
    }
}
