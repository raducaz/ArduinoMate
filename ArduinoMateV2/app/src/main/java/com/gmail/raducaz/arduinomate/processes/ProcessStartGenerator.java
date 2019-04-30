package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.commands.ArduinoCommander;
import com.gmail.raducaz.arduinomate.commands.CommandGeneratorStart;

public class ProcessStartGenerator {

    public static void execute(ProcessExecutor processExecutor) {
        ArduinoCommander arduinoCommander = new ArduinoCommander(processExecutor.host,processExecutor.port);
        CommandGeneratorStart.execute(arduinoCommander);
    }

}
