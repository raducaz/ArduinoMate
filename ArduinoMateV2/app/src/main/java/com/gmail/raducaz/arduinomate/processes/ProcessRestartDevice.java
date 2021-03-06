package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

public class ProcessRestartDevice extends Process {

    public ProcessRestartDevice(DataRepository dataRepository, String deviceName, String functionName)
    {
        super(dataRepository, deviceName, functionName);
    }
    public ProcessRestartDevice(DataRepository dataRepository, long deviceId, String functionName)
    {
        super(dataRepository, deviceId, functionName);
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");

        // Do not close generator because it is possible that there are other consumers

        logInfo("Check if generator contact is already ON");
        if(deviceGeneratorFunctions.getGeneratorState()==FunctionResultStateEnum.ON)
        {
            logInfo("Contact still ON");
            logInfo("Check current consumption");
            if (!deviceGeneratorFunctions.isCurrentAbove(0.7)) {
                logInfo("NO current consumption consider stopping it");
            }
        }
        else {
            logInfo("Contact is OFF, START generator");
            deviceGeneratorFunctions.generatorON();
        }

        logInfo("Mark SOC function OFF");
        FunctionEntity socOnOff = dataRepository.loadFunctionSync(deviceEntity.getId(),"SocOnOff");
        socOnOff.setResultState(FunctionResultStateEnum.OFF.getId());
        dataRepository.updateFunction(socOnOff);

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");

        // This ensures also stopping all dependent processes ...
        // TODO: Maybe it is a good idea to implement an event driven mechanism that when ON a function to add also a handler for future OFF events from dependent processes
        logInfo("Check power state is ON");
        if(deviceGeneratorFunctions.getPowerState()==FunctionResultStateEnum.ON) {
            ProcessHouseWaterOnOff pWater = new ProcessHouseWaterOnOff(dataRepository, "Tap");
            logInfo("Power state is ON, STOP HouseWater");
            pWater.execute(false,isOnDemand, FunctionResultStateEnum.OFF, "Generator is stopping");


            ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, "Generator");
            logInfo("STOP pump");
            pPump.execute(false, isOnDemand, FunctionResultStateEnum.OFF, "Generator is stopping");
            //TODO; SOLVED Solve this recursive call, this will call Gen.Off again inside Pump.off
        }
        logInfo("STOP generator");
        deviceGeneratorFunctions.generatorOFF();

        logInfo("Check if generator contact is still ON");
        if(deviceGeneratorFunctions.getGeneratorState()==FunctionResultStateEnum.ON)
        {
            throw new Exception("Contact still ON, stop it manually !");
        }
        if(deviceGeneratorFunctions.isCurrentAbove(0.7))
        {
            throw new Exception("Generator CANNOT BE STOPPED !!!");
        }

        logInfo("Mark SOC function OFF");
        FunctionEntity socOnOff = dataRepository.loadFunctionSync(deviceEntity.getId(),"SocOnOff");
        socOnOff.setResultState(FunctionResultStateEnum.OFF.getId());
        dataRepository.updateFunction(socOnOff);

        return super.off(isOnDemand);
    }
}
