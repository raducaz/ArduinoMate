package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

public class ProcessSocOnOff extends Process {

    public ProcessSocOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "SocOnOff");
    }
    public ProcessSocOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "SocOnOff");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        FunctionEntity generatorOnOffFunction = dataRepository.loadFunctionSync(deviceEntity.getId(),"GeneratorOnOff");

        // Block this function while generator is ON (or Error)
        if(generatorOnOffFunction.getResultState()==FunctionResultStateEnum.ON.getId() ||
                generatorOnOffFunction.getResultState()==FunctionResultStateEnum.ERROR.getId())
        {
            throw new Exception("Cannot activate SOC while generator is not in OFF state");
        }

        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");

        deviceGeneratorFunctions.socON();

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");

        deviceGeneratorFunctions.socOFF();

        return super.off(isOnDemand);
    }
}
