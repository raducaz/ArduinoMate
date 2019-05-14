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
    protected boolean on() throws Exception {
        ProcessWaterSupplyTapOnOff pWaterSupplyTap = new ProcessWaterSupplyTapOnOff(dataRepository, deviceEntity.getName());
        ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, deviceEntity.getName());

        if(pWaterSupplyTap.execute(false, FunctionResultStateEnum.ON)) {
            if (!pPump.execute(false, FunctionResultStateEnum.ON)) {

                throw new Exception("Problem starting pump.");

            }
        }
        else
        {
            throw new Exception("Water supply tap is not Opened.");
        }

        return super.on();
    }

    @Override
    protected boolean off() throws Exception {
        ProcessWaterSupplyTapOnOff pWaterSupplyTap = new ProcessWaterSupplyTapOnOff(dataRepository, deviceEntity.getName());
        ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, deviceEntity.getName());

        //Ensure the tap is Close so the pressure is increased before the pump will automatically
        //be closed (because no current is consumed)
        if(pWaterSupplyTap.execute(false, FunctionResultStateEnum.OFF)) {
            // Wait for pump to be automatically be stopped
        }
        else
        {
            // Stop the pump in case the tap is not closed successfully
            if (!pPump.execute(false, FunctionResultStateEnum.OFF)) {

                throw new Exception("Problem starting pump.");

            }
            throw new Exception("Water supply tap is not Closed.");
        }

        return super.off();
    }
}
