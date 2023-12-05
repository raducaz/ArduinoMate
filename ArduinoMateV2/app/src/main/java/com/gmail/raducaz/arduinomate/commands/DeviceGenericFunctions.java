package com.gmail.raducaz.arduinomate.commands;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class DeviceGenericFunctions {
    ArduinoCommander arduinoCommander;
    DataRepository dataRepository;
    DeviceEntity deviceEntity;

    public DeviceGenericFunctions(DataRepository dataRepository, String deviceName) {
        this.deviceEntity = dataRepository.loadDeviceByNameSync(deviceName);
        arduinoCommander = new ArduinoCommander(deviceEntity.getIp(), deviceEntity.getPort());
    }
    public DeviceGenericFunctions(DataRepository dataRepository, long deviceId) {
        this.deviceEntity = dataRepository.loadDeviceSync(deviceId);
        arduinoCommander = new ArduinoCommander(deviceEntity.getIp(), deviceEntity.getPort());
    }

    public boolean restartDevice()
    {
        try {
            String command = "[F0]";
            String result = arduinoCommander.SendCommand(command);

            return true;
        }
        catch (Exception exc)
        {
            Logger.e("restartDevice"+exc.getMessage());
            return false;
        }
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
