package com.gmail.raducaz.arduinomate.TestProcesses;

import com.gmail.raducaz.arduinomate.commands.ArduinoCommander;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TapOpenStateCommand {
    @Test
    public void TestSendMessage() {

        String command = "[~A3:0|#A4|?A4|~A3:1|#A4|?A4]";
//        String command = "[?A4|#A4|?A4|#A4|?A4|#A4]";

        ArduinoCommander arduinoCommander = new ArduinoCommander("192.168.1.101", 8081);
        String result = arduinoCommander.SendCommand(command);
    }
}
