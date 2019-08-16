package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionStateUpdater;

import java.util.concurrent.TimeUnit;

public class ProcessPumpOnOff extends Process {

    public ProcessPumpOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "PumpOnOff");
    }
    public ProcessPumpOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "PumpOnOff");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        String currentReason = "Pump is starting";

        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");

        // Don't want to validate the children process as long as the parent is AutoEnabled
        ProcessGeneratorOnOff pGen = new ProcessGeneratorOnOff(dataRepository, "Generator");
        ProcessPowerOnOff pPower = new ProcessPowerOnOff(dataRepository, "Generator");

        logInfo("START generator");
        if (pGen.execute(false, isOnDemand, FunctionResultStateEnum.ON, currentReason)) {
            logInfo("Wait 1 sec before start power");
            // Wait for one second before starting pump to give time to generator to run properly
            TimeUnit.SECONDS.sleep(1);

            logInfo("START power");
            if (!pPower.execute(false, isOnDemand, FunctionResultStateEnum.ON, currentReason)) {
                //TODO: This can be avoided by PinStateChanged event which will sense no current and stop the generator automatically
                // Stop generator if no consumption
                // pGen.execute(false, FunctionResultStateEnum.OFF);
                throw new Exception("No current consumption after Pump started.");
            }
        }

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        String currentReason = "Pump is stopping";

        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");

        // Don't want to validate the children process as long as the parent is AutoEnabled
        ProcessGeneratorOnOff pGen = new ProcessGeneratorOnOff(dataRepository, "Generator");
        ProcessPowerOnOff pPower = new ProcessPowerOnOff(dataRepository, "Generator");

        logInfo("Check if generator Contact is ON");
        if(deviceGeneratorFunctions.getPowerState()==FunctionResultStateEnum.ON) {
            logInfo("Power on, stop power");
            pPower.execute(false, isOnDemand, FunctionResultStateEnum.OFF, currentReason);
        }
        logInfo("STOP generator");
        if (!pGen.execute(false, isOnDemand, FunctionResultStateEnum.OFF, currentReason)) {
            throw new Exception("Generator couldn't be stopped. Retry.");
        }
        return super.off(isOnDemand);
    }
}
