package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionStateUpdater;

public class ProcessGeneratorOnOff extends Process {
    public ProcessGeneratorOnOff(DataRepository dataRepository, String deviceIp)
    {
        super(dataRepository, deviceIp, "GeneratorOnOff");
    }
    public ProcessGeneratorOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "GeneratorOnOff");
    }

    @Override
    protected boolean on() throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getIp());

        deviceGeneratorFunctions.generatorON();

        return super.on();
    }

    @Override
    protected boolean off() throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getIp());

        ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, deviceEntity.getIp());
        pPump.execute(false, FunctionResultStateEnum.OFF);

        //deviceGeneratorFunctions.generatorOFF();

        return super.off();
    }
}
