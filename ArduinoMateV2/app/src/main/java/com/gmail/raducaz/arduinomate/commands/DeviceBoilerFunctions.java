package com.gmail.raducaz.arduinomate.commands;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.concurrent.TimeUnit;

public class DeviceBoilerFunctions {
    ArduinoCommander arduinoCommander;
    DataRepository dataRepository;
    DeviceEntity deviceEntity;

    public DeviceBoilerFunctions(DataRepository dataRepository, String deviceName) {
        this.deviceEntity = dataRepository.loadDeviceByNameSync(deviceName);
        arduinoCommander = new ArduinoCommander(deviceEntity.getIp(), deviceEntity.getPort());
    }

    public boolean boilerON() {
        String TAG = "boilerON";

        // TODO: check the actual pin and state need to be in
        String command = "[=7:0]";
        String result = arduinoCommander.SendCommand(command);

        //TODO: check also the current consumption

        return true;

    }
    public boolean boilerOFF() {
        String TAG = "boilerOFF";

        // TODO: check the actual pin and state need to be in
        String command = "[=7:1]";
        String result = arduinoCommander.SendCommand(command);

        return true;

    }

}
