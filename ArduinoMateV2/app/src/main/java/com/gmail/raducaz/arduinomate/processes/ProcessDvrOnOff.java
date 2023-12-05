package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceDvrFunctions;

public class ProcessDvrOnOff extends Process {

    public ProcessDvrOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "DvrOnOff");
    }
    public ProcessDvrOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "DvrOnOff");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceDvrFunctions deviceDvrFunctions = new DeviceDvrFunctions(dataRepository, "DVR");

        logInfo("Sending wakeup call..");
        deviceDvrFunctions.wakeup();

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        // Do nothing, just update the status
        return super.off(isOnDemand);
    }
}
