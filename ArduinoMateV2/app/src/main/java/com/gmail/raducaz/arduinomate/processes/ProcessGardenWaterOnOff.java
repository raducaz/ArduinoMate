package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

public class ProcessGardenWaterOnOff extends Process {

    public ProcessGardenWaterOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "PumpOnOff");
    }
    public ProcessGardenWaterOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "PumpOnOff");
    }

    @Override
    protected boolean on() throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getName());

        // Don't want to validate the children process as long as the parent is AutoEnabled
        ProcessGeneratorOnOff pGen = new ProcessGeneratorOnOff(dataRepository, deviceEntity.getName());
        ProcessPowerOnOff pPower = new ProcessPowerOnOff(dataRepository, deviceEntity.getName());

        if (pGen.execute(false, FunctionResultStateEnum.ON)) {
            if (!pPower.execute(false, FunctionResultStateEnum.ON)) {
                //TODO: This can be avoided by PinStateChanged event which will sense no current and stop the generator automatically
                // Stop generator if no consumption
                // pGen.execute(false, FunctionResultStateEnum.OFF);
                throw new Exception("No current consumption after Pump started.");
            }
        }

        return super.on();
    }

    @Override
    protected boolean off() throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getName());

        // Don't want to validate the children process as long as the parent is AutoEnabled
        ProcessGeneratorOnOff pGen = new ProcessGeneratorOnOff(dataRepository, deviceEntity.getName());
        ProcessPowerOnOff pPower = new ProcessPowerOnOff(dataRepository, deviceEntity.getName());

        pPower.execute(false, FunctionResultStateEnum.OFF);

        if (!pGen.execute(false, FunctionResultStateEnum.OFF)) {
            throw new Exception("Generator couldn't be stopped. Retry.");
        }

        return super.off();
    }
}
