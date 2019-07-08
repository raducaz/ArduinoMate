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
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");

        logInfo("START power.");
        deviceGeneratorFunctions.powerON();

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");

        logInfo("STOP power");
        deviceGeneratorFunctions.powerOFF();

        return super.off(isOnDemand);
    }
}
