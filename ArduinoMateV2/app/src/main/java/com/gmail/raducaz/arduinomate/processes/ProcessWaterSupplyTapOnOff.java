package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.commands.DeviceTapFunctions;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

public class ProcessWaterSupplyTapOnOff extends Process {

    public ProcessWaterSupplyTapOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "WaterSupplyTapOnOff");
    }

    public ProcessWaterSupplyTapOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "WaterSupplyTapOnOff");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceTapFunctions deviceTapFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        logInfo("OPEN main tap");
        if(!deviceTapFunctions.tapOPEN())
        {
            throw new Exception("Problem opening tap.");
        }

        // Mark House water function as OFF when main tap is OPEN
        FunctionEntity houseWaterFunction = dataRepository.loadFunctionSync(deviceEntity.getId(),"HouseWaterOnOff");
        houseWaterFunction.setResultState(FunctionResultStateEnum.OFF.getId());
        dataRepository.updateFunction(houseWaterFunction);

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        DeviceTapFunctions deviceGeneratorFunctions = new DeviceTapFunctions(dataRepository, deviceEntity.getName());

        logInfo("CLOSE main tap");
        if(!deviceGeneratorFunctions.tapCLOSE())
        {
            throw new Exception("Problem closing tap.");
        }

        // Mark Garden water function as OFF when main tap is CLOSED
        FunctionEntity gardenWaterFunction = dataRepository.loadFunctionSync(deviceEntity.getId(),"GardenWaterOnOff");
        gardenWaterFunction.setResultState(FunctionResultStateEnum.OFF.getId());
        dataRepository.updateFunction(gardenWaterFunction);

        return super.off(isOnDemand);
    }
}
