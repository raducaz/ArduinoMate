package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionStateUpdater;

public class ProcessGeneratorOnOff extends Process {
    public ProcessGeneratorOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "GeneratorOnOff");
    }
    public ProcessGeneratorOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "GeneratorOnOff");
    }

    @Override
    protected boolean on() throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getName());

        deviceGeneratorFunctions.generatorON();

        return super.on();
    }

    @Override
    protected boolean off() throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getName());

        // This ensures also stopping all dependent processes ...
        // TODO: Maybe it is a good idea to implement an event driven mechanism that when ON a function to add also a handler for future OFF events from dependent processes
        if(deviceGeneratorFunctions.getPowerState()==FunctionResultStateEnum.ON) {
            ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, deviceEntity.getName());
            pPump.execute(false, FunctionResultStateEnum.OFF);
            //TODO; SOLVED Solve this recursive call, this will call Gen.Off again inside Pump.off
        }
        deviceGeneratorFunctions.generatorOFF();

        return super.off();
    }
}
