package com.gmail.raducaz.arduinomate.commands;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;

public class DeviceSocketFunctions {
    ArduinoCommander arduinoCommander;
    DataRepository dataRepository;
    DeviceEntity deviceEntity;

    public DeviceSocketFunctions(DataRepository dataRepository, String deviceName) {
        this.deviceEntity = dataRepository.loadDeviceByNameSync(deviceName);
        arduinoCommander = new ArduinoCommander(deviceEntity.getIp(), deviceEntity.getPort());
    }

    public boolean soket6ON() {
        String TAG = "soket6ON";

        // Soc ON
        String command = "[=6:0]";
        String result = arduinoCommander.SendCommand(command);

        return true;
    }
    // Set Soc On sequence
    public boolean soket6OFF() {
        String TAG = "soket6OFF";

        // Soc ON
        String command = "[=6:1]";
        String result = arduinoCommander.SendCommand(command);

        return true;
    }

}
