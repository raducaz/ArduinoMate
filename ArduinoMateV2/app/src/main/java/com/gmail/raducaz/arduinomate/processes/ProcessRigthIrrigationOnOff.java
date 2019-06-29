package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceTapFunctions;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.concurrent.TimeUnit;

public class ProcessRigthIrrigationOnOff extends Process {

    public ProcessRigthIrrigationOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "RightIrrigationOnOff");
    }
    public ProcessRigthIrrigationOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "RightIrrigationOnOff");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceTapFunctions tapFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        logInfo("Start OPEN right tap");
        tapFunctions.tapIrrigationRightOPENStart();
        logInfo("Wait 45 sec");
        TimeUnit.SECONDS.sleep(45);
        logInfo("Stop OPEN right tap");
        tapFunctions.tapIrrigationRightOPENStop();
        logInfo("Right tap OPENED");

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        DeviceTapFunctions tapFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        logInfo("Start CLOSE right tap");
        tapFunctions.tapIrrigationRightCLOSEStart();
        logInfo("Wait 45 sec");
        TimeUnit.SECONDS.sleep(45);
        logInfo("Stop CLOSE right tap");
        tapFunctions.tapIrrigationRightCLOSEStop();
        logInfo("Right tap CLOSED");

        return super.off(isOnDemand);
    }
}
