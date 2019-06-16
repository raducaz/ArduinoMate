package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

public class ProcessIgnitionOnOff extends Process {

    public ProcessIgnitionOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "IgnitionOnOff");
    }
    public ProcessIgnitionOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "IgnitionOnOff");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");

        FunctionEntity generatorOnOffFunction = dataRepository.loadFunctionSync(deviceEntity.getId(),"GeneratorOnOff");
        // Block this function while generator is ON (or Error)
        if(generatorOnOffFunction.getResultState()==FunctionResultStateEnum.ON.getId() ||
                generatorOnOffFunction.getResultState()==FunctionResultStateEnum.ERROR.getId())
        {
            throw new Exception("Cannot activate IGNITION while generator is not in OFF state");
        }

        logInfo("Contact OFF to stop if ON");
        deviceGeneratorFunctions.contactOFF();

        logInfo("Mark GeneratorOnOff function state to OFF");
        FunctionEntity generatorOnOff = dataRepository.loadFunctionSync(deviceEntity.getId(),"GeneratorOnOff");
        generatorOnOff.setResultState(FunctionResultStateEnum.OFF.getId());
        dataRepository.updateFunction(generatorOnOff);

        logInfo("Contact ON");
        deviceGeneratorFunctions.contactON();
        logInfo("Start !");
        deviceGeneratorFunctions.ignition();

        logInfo("Mark GeneratorOnOff function state to ON");
        generatorOnOff.setResultState(FunctionResultStateEnum.ON.getId());
        dataRepository.updateFunction(generatorOnOff);

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");

        logInfo("Contact OFF");
        deviceGeneratorFunctions.contactOFF();

        logInfo("Mark GeneratorOnOff function state to OFF");
        FunctionEntity generatorOnOff = dataRepository.loadFunctionSync(deviceEntity.getId(),"GeneratorOnOff");
        generatorOnOff.setResultState(FunctionResultStateEnum.OFF.getId());
        dataRepository.updateFunction(generatorOnOff);

        return super.off(isOnDemand);
    }
}
