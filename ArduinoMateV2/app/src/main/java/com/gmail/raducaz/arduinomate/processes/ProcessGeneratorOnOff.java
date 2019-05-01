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

    @Override
    protected boolean on() {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getIp());

        try {
            deviceGeneratorFunctions.generatorON();

            return super.on();
        } catch (Exception exc) {
            functionExecution.setResultState(FunctionResultStateEnum.ERROR.getId());
            throw exc;
        }
    }

    @Override
    protected boolean off()
    {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getIp());

        try {
            deviceGeneratorFunctions.generatorOFF();

            return super.on();
        } catch (Exception exc) {
            functionExecution.setResultState(FunctionResultStateEnum.ERROR.getId());
            throw exc;
        }
    }
}
