package com.gmail.raducaz.arduinomate.commands;

import com.gmail.raducaz.arduinomate.telnet.TelnetClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ArduinoCommander {
    String host;
    int port;
    public ArduinoCommander(String host, int port)
    {
        this.host = host;
        this.port = port;
    }
    public String SendCommand(String command)
    {
        final TelnetClient telnetClient = new TelnetClient(host, port,
                "test", "test");
        Map<String, Object> map = new HashMap<>();
        telnetClient.executeCommand(command, map);

        Object result = map.get(command);
        if(result!=null)
            return result.toString();
        else
            return "";
    }
}
