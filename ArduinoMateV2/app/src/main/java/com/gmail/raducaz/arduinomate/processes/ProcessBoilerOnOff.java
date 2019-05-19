package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceBoilerFunctions;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

public class ProcessBoilerOnOff extends Process {

    public ProcessBoilerOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "BoilerOnOff");
    }
    public ProcessBoilerOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "BoilerOnOff");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");
        ProcessGeneratorOnOff pGen = new ProcessGeneratorOnOff(dataRepository, "Generator");

        // Prevent other events to stop this - only manually overwrite
        logInfo("Disable AUTO for generator to prevent auto stop on pump stop");
        pGen.setFunctionAuto(false);

        logInfo("START generator");
        pGen.execute(false, isOnDemand, FunctionResultStateEnum.ON, "Boiler is starting");

        DeviceBoilerFunctions deviceBoilerFunctions = new DeviceBoilerFunctions(dataRepository, "Boiler");
        logInfo("START boiler");
        if(!deviceBoilerFunctions.boilerON())
        {
            throw new Exception("Boiler cannot start");
        }

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        String currentReason = "Boiler is stopping";

        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");
        ProcessGeneratorOnOff pGen = new ProcessGeneratorOnOff(dataRepository, "Generator");

        DeviceBoilerFunctions deviceBoilerFunctions = new DeviceBoilerFunctions(dataRepository, "Boiler");
        logInfo("STOP boiler");
        if(!deviceBoilerFunctions.boilerOFF())
        {
            throw new Exception("Boiler cannot stop");
        }
        if(deviceGeneratorFunctions.isCurrentAbove(0.7)) {
            logInfo("Set generator to AUTO and WAIT for generator to automatically stop when no current consumption");
            pGen.setFunctionAuto(true);
        }
        else
        {
            logInfo("No current cunsumption now, stop the generator");
            pGen.execute(false, isOnDemand, FunctionResultStateEnum.OFF, currentReason);
        }
        //TODO: Test if after Boiler is ready the power consumption stops.

        return super.off(isOnDemand);

    }
}
