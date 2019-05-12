package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.commands.DeviceTapFunctions;

public class ProcessWaterSupplyTapOnOff extends Process {

    public ProcessWaterSupplyTapOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "WaterSupplyTapOnOff");
    }

    public ProcessWaterSupplyTapOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "WaterSupplyTapOnOff");
    }

    @Override
    protected boolean on() throws Exception {
        DeviceTapFunctions deviceGeneratorFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        return deviceGeneratorFunctions.tapOPEN() && super.on();
    }

    @Override
    protected boolean off() throws Exception {
        DeviceTapFunctions deviceGeneratorFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        return deviceGeneratorFunctions.tapCLOSE() && super.off();
    }
}
