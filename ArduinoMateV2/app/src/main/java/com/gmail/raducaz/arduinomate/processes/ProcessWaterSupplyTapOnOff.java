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
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceTapFunctions deviceTapFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        logInfo("OPEN main tap");
        if(!deviceTapFunctions.tapOPEN())
        {
            throw new Exception("Problem opening tap.");
        }

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        DeviceTapFunctions deviceGeneratorFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        logInfo("CLOSE main tap");
        if(!deviceGeneratorFunctions.tapCLOSE())
        {
            throw new Exception("Problem closing tap.");
        }
        return super.off(isOnDemand);
    }
}
