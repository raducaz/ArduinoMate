package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionStateUpdater;

public class ProcessPumpOnOff extends Process {

    public ProcessPumpOnOff(DataRepository dataRepository, String deviceIp)
    {
        super(dataRepository, deviceIp, "PumpOnOff");
    }

    @Override
    protected boolean on()
    {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getIp());

        try {
            // Don't want to validate the children process as long as the parent is AutoEnabled
            ProcessGeneratorOnOff pGen = new ProcessGeneratorOnOff(dataRepository, deviceEntity.getIp());
            if(pGen.execute(false, FunctionResultStateEnum.ON))
            {
                if(!new ProcessPowerOnOff(dataRepository, deviceEntity.getIp()).execute(false, FunctionResultStateEnum.ON))
                {
                    //TODO: This can be avoided by PinStateChanged event which will sense no current and stop the generator automatically
                    // Stop generator if no consumption
                    // pGen.execute(false, FunctionResultStateEnum.OFF);
                    throw new Exception("No current consumption after Pump started.");
                }
            }

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
            // Don't want to validate the children process as long as the parent is AutoEnabled
            ProcessGeneratorOnOff pGen = new ProcessGeneratorOnOff(dataRepository, deviceEntity.getIp());
            ProcessPowerOnOff pPower = new ProcessPowerOnOff(dataRepository, deviceEntity.getIp());

            pPower.execute(false, FunctionResultStateEnum.OFF);

            if(!pGen.execute(false, FunctionResultStateEnum.OFF))
            {
                throw new Exception("Generator couldn't be stopped. Retry.");
            }

            return super.on();
        } catch (Exception exc) {
            functionExecution.setResultState(FunctionResultStateEnum.ERROR.getId());
            throw exc;
        }
    }
}
