package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

public class ProcessHouseWaterOnOff extends Process {

    public ProcessHouseWaterOnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "HouseWaterOnOff");
    }
    public ProcessHouseWaterOnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "HouseWaterOnOff");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {

        String currentReason = "House water is starting";

        ProcessWaterSupplyTapOnOff pWaterSupplyTap = new ProcessWaterSupplyTapOnOff(dataRepository, "Tap");
        ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, "Generator");

        logInfo("CLOSE main tap");
        if(pWaterSupplyTap.execute(false, isOnDemand, FunctionResultStateEnum.OFF, currentReason)) {
            logInfo("START pump");
            if (!pPump.execute(false, isOnDemand, FunctionResultStateEnum.ON, currentReason)) {

                throw new Exception("Problem starting pump.");

            }
        }
        else
        {
            throw new Exception("Water supply tap is not Closed.");
        }

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {

        String currentReason = "House water is stopping";

        ProcessWaterSupplyTapOnOff pWaterSupplyTap = new ProcessWaterSupplyTapOnOff(dataRepository, "Tap");
        ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, "Generator");

        //Ensure the tap is Close so the pressure is maintained
        logInfo("CLOSE main tap");
        if(pWaterSupplyTap.execute(false, isOnDemand, FunctionResultStateEnum.OFF, currentReason)) {
            logInfo("STOP pump");
            if (!pPump.execute(false, isOnDemand, FunctionResultStateEnum.OFF, currentReason)) {

                throw new Exception("Problem stopping pump.");

            }
        }
        else
        {
            throw new Exception("Water supply tap is not Closed.");
        }

        return super.off(isOnDemand);
    }
}
