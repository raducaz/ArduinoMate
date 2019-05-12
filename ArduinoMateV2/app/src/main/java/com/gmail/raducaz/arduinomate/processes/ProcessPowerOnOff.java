package com.gmail.raducaz.arduinomate.processes;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

public class ProcessPowerOnOff extends Process {

    public ProcessPowerOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "PowerOnOff");
    }

    public ProcessPowerOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "PowerOnOff");
    }

    @Override
    protected boolean on() throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getName());

        deviceGeneratorFunctions.powerON();

        return super.on();
    }

    @Override
    protected boolean off() throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getName());

        deviceGeneratorFunctions.powerOFF();

        return super.off();
    }
}
