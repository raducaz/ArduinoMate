package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
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
    protected boolean on() throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getName());

        // Prevent other events to stop this - only manually overwrite
        function.setIsAutoEnabled(false);

        ProcessGeneratorOnOff pGen = new ProcessGeneratorOnOff(dataRepository, deviceEntity.getName());
        pGen.execute(false, FunctionResultStateEnum.ON);

        //TODO: Test if after Boiler is ready the power consumption stops.


        return super.on();
    }

    @Override
    protected boolean off() throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, deviceEntity.getName());


        // Prevent other events to stop this - only manually overwrite
        function.setIsAutoEnabled(false);

        ProcessGeneratorOnOff pGen = new ProcessGeneratorOnOff(dataRepository, deviceEntity.getName());
        pGen.execute(false, FunctionResultStateEnum.OFF);

        //TODO: Test if after Boiler is ready the power consumption stops.


        return super.off();

    }
}
