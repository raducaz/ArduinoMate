package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.ArduinoCommander;
import com.gmail.raducaz.arduinomate.commands.CommandGeneratorStart;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.service.FunctionStateUpdater;

public class ProcessStartGenerator {

    public static void execute(DataRepository dataRepository, FunctionStateUpdater functionStateUpdater) {
        DeviceEntity deviceEntity = dataRepository.loadDeviceSync("192.168.100.100");
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(deviceEntity.getIp(), deviceEntity.getPort());


    }

}
