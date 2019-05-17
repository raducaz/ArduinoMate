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
    protected boolean on() throws Exception {

        ProcessWaterSupplyTapOnOff pWaterSupplyTap = new ProcessWaterSupplyTapOnOff(dataRepository, deviceEntity.getName());
        ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, "Generator");

        if(pWaterSupplyTap.execute(false, FunctionResultStateEnum.OFF)) {
            if (!pPump.execute(false, FunctionResultStateEnum.ON)) {

                throw new Exception("Problem starting pump.");

            }
        }
        else
        {
            throw new Exception("Water supply tap is not Closed.");
        }

        return super.on();
    }

    @Override
    protected boolean off() throws Exception {
        ProcessWaterSupplyTapOnOff pWaterSupplyTap = new ProcessWaterSupplyTapOnOff(dataRepository, deviceEntity.getName());
        ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, "Generator");

        //Ensure the tap is Close so the pressure is maintained
        if(pWaterSupplyTap.execute(false, FunctionResultStateEnum.OFF)) {
            if (!pPump.execute(false, FunctionResultStateEnum.OFF)) {

                throw new Exception("Problem stopping pump.");

            }
        }
        else
        {
            throw new Exception("Water supply tap is not Closed.");
        }

        return super.off();
    }
}
