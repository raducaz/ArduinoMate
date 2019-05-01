package com.gmail.raducaz.arduinomate.processes;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

public class ProcessPowerOnOff extends Process {

    public ProcessPowerOnOff(DataRepository dataRepository, String deviceIp)
    {
        super(dataRepository, deviceIp, "PowerOnOff");
    }

    @Override
    protected boolean on() {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getIp());

        try {
            deviceGeneratorFunctions.powerON();

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
            deviceGeneratorFunctions.powerOFF();

            return super.on();
        } catch (Exception exc) {
            functionExecution.setResultState(FunctionResultStateEnum.ERROR.getId());
            throw exc;
        }
    }
}
