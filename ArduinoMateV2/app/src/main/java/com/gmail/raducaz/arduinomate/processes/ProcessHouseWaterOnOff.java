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
        ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, deviceEntity.getName());

        if(pWaterSupplyTap.execute(false, FunctionResultStateEnum.OFF)) {
            if (!pPump.execute(false, FunctionResultStateEnum.ON)) {

                throw new Exception("Problem starting pump.");

            }
        }

        return super.on();
    }

    @Override
    protected boolean off() throws Exception {
        ProcessWaterSupplyTapOnOff pWaterSupplyTap = new ProcessWaterSupplyTapOnOff(dataRepository, deviceEntity.getName());
        ProcessPumpOnOff pPump = new ProcessPumpOnOff(dataRepository, deviceEntity.getName());

        //Endure the tap is Close so the pressure is maintained
        if(pWaterSupplyTap.execute(false, FunctionResultStateEnum.OFF)) {
            if (!pPump.execute(false, FunctionResultStateEnum.ON)) {

                throw new Exception("Problem starting pump.");

            }
        }
        return super.off();
    }
}
