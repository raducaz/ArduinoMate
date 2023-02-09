package com.gmail.raducaz.arduinomate.commands;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.concurrent.TimeUnit;

public class DeviceDefault1Functions {
    ArduinoCommander arduinoCommander;
    DataRepository dataRepository;
    DeviceEntity deviceEntity;

    public DeviceDefault1Functions(DataRepository dataRepository, String deviceName) {
        this.deviceEntity = dataRepository.loadDeviceByNameSync(deviceName);
        arduinoCommander = new ArduinoCommander(deviceEntity.getIp(), deviceEntity.getPort());
    }

    // Returns true if pin = 1
    private boolean isPinOn(String pin) throws Exception {
        String TAG = "isPinOn";

        String command = "[?" + pin + "]";
        String result = arduinoCommander.SendCommand(command);

        // Read command results
        if (new Parser(result).getInt("?" + pin) == 0)
            return false;
        else
            return true;
    }

    public double getCurrentTemperature()
    {
        try {
            // Check AC current - if high then it's on
            String command = "[F2]";
            String result = arduinoCommander.SendCommand(command);
            double temp = 0;
            temp = new Parser(result).getDouble("F2");

            return temp;
        }
        catch (Exception exc)
        {
            Log.e("getTemperature", exc.getMessage());
            return -50;
        }
    }


}
