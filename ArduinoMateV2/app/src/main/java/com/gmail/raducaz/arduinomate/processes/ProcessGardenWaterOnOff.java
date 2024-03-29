package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

public class ProcessGardenWaterOnOff extends Process {

    public ProcessGardenWaterOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "GardenWaterOnOff");
    }
    public ProcessGardenWaterOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "GardenWaterOnOff");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        ProcessWaterSupplyTapOnOff pWaterSupplyTap = new ProcessWaterSupplyTapOnOff(dataRepository, "Tap");
        ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, "Generator");

        logInfo("OPEN main tap");
        if(pWaterSupplyTap.execute(false, isOnDemand, FunctionResultStateEnum.ON, "Garden water is starting")) {
            logInfo("tap opened, START pump");
            if (!pPump.execute(false, isOnDemand, FunctionResultStateEnum.ON,"Garden water is starting")) {

                throw new Exception("Problem starting pump.");

            }
        }
        else
        {
            throw new Exception("Water supply tap is not Opened.");
        }

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        ProcessWaterSupplyTapOnOff pWaterSupplyTap = new ProcessWaterSupplyTapOnOff(dataRepository, "Tap");
        ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, "Generator");

        logInfo("Stopping pump..");
        // Stop the pump
        if (!pPump.execute(false, isOnDemand, FunctionResultStateEnum.OFF, "Garden water is stopping")) {
            throw new Exception("Problem stopping pump.");
        }else {
            logInfo("CLOSE main tap");
            if (!pWaterSupplyTap.execute(false, isOnDemand, FunctionResultStateEnum.OFF, "Garden water is stopping")) {
                throw new Exception("Water supply tap is not Closed.");
            }
        }

        return super.off(isOnDemand);
    }
}
