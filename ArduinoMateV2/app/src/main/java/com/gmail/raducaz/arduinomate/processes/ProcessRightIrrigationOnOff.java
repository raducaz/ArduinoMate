package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceTapFunctions;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.concurrent.TimeUnit;

public class ProcessRightIrrigationOnOff extends Process {

    public ProcessRightIrrigationOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "RightIrrigationOnOff");
    }
    public ProcessRightIrrigationOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "RightIrrigationOnOff");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceTapFunctions tapFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        logInfo("Start OPEN right tap");
        tapFunctions.tapIrrigationRightOPEN();
        logInfo("Wait 45 sec");
        TimeUnit.SECONDS.sleep(45);
        logInfo("Right tap OPENED");

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        DeviceTapFunctions tapFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        logInfo("Start CLOSE right tap");
        tapFunctions.tapIrrigationRightCLOSE();
        logInfo("Wait 45 sec");
        TimeUnit.SECONDS.sleep(45);
        logInfo("Right tap CLOSED");

        return super.off(isOnDemand);
    }
}
