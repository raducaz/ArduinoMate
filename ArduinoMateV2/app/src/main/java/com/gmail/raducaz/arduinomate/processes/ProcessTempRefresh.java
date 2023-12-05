package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGenericFunctions;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.Calendar;

public class ProcessTempRefresh extends Process {

    public ProcessTempRefresh(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "TempRefresh");
    }
    public ProcessTempRefresh(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "ProcessTempRefresh");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceGenericFunctions deviceFunctions = new DeviceGenericFunctions(dataRepository, "SocketX4");

        FunctionEntity tempRefresh = dataRepository.loadFunctionSync(deviceEntity.getId(),"TempRefresh");

        logInfo("Getting temperature");
        double temperature = deviceFunctions.getCurrentTemperature();

        logInfo("Temp is " + temperature);
        FunctionEntity f = dataRepository.loadFunctionSync(deviceEntity.getId(),"TempRefresh");
        f.setResultState(FunctionResultStateEnum.OFF.getId());
        f.setCallState(FunctionCallStateEnum.READY.getId());
        f.setDescription("Temperature: " + temperature);
        dataRepository.updateFunction(f);

        Calendar cal = Calendar.getInstance();
        int millisecond = cal.get(Calendar.MILLISECOND);
        int second = cal.get(Calendar.SECOND);
        int minute = cal.get(Calendar.MINUTE);
        int hourofday = cal.get(Calendar.HOUR_OF_DAY);
        dataRepository.updateFunctionLog(f.getId(),
                "At " + hourofday + ":" + minute + ":" + second + " temp is " + temperature); //Prevent updating with old CallState if using updateFunction

        return super.on(isOnDemand);
    }

}
