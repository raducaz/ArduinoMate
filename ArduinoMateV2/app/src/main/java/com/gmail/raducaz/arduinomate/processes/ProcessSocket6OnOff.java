package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.commands.DeviceGenericFunctions;
import com.gmail.raducaz.arduinomate.commands.DeviceSocketFunctions;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.Calendar;

public class ProcessSocket6OnOff extends Process {

    public ProcessSocket6OnOff(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "Socket6");
    }
    public ProcessSocket6OnOff(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "Socket6");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceSocketFunctions deviceFunctions = new DeviceSocketFunctions(dataRepository, "SocketX4");

        deviceFunctions.soket6ON();

        return super.on(isOnDemand);
    }

    @Override
    protected boolean off(boolean isOnDemand) throws Exception {
        DeviceSocketFunctions deviceFunctions = new DeviceSocketFunctions(dataRepository, "SocketX4");

        deviceFunctions.soket6OFF();

        return super.off(isOnDemand);
    }
}
