package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceTapFunctions;

import java.util.concurrent.TimeUnit;

public class ProcessLeftIrrigationOnOff extends Process {

    public ProcessLeftIrrigationOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "LeftIrrigationOnOff");
    }
    public ProcessLeftIrrigationOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "LeftIrrigationOnOff");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceTapFunctions tapFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        logInfo("Start OPEN left tap");
        tapFunctions.tapIrrigationLeftOPENStart();
        logInfo("Wait 45 sec");
        TimeUnit.SECONDS.sleep(45);
        logInfo("Stop OPEN left tap");
        tapFunctions.tapIrrigationLeftOPENStop();
        logInfo("Left tap OPENED");

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        DeviceTapFunctions tapFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        logInfo("Start CLOSE left tap");
        tapFunctions.tapIrrigationLeftCLOSEStart();
        logInfo("Wait 45 sec");
        TimeUnit.SECONDS.sleep(45);
        logInfo("Stop CLOSE left tap");
        tapFunctions.tapIrrigationLeftCLOSEStop();
        logInfo("Left tap CLOSED");

        return super.off(isOnDemand);
    }
}
