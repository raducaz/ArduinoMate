package com.gmail.raducaz.arduinomate.processes;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.Calendar;

public class ProcessDefault1Refresh extends Process {

    public ProcessDefault1Refresh(DataRepository dataRepository, String deviceName)
    {
        super(dataRepository, deviceName, "Default1Refresh");
    }
    public ProcessDefault1Refresh(DataRepository dataRepository, long deviceId)
    {
        super(dataRepository, deviceId, "Default1Refresh");
    }

    @Override
    protected boolean on(boolean isOnDemand) throws Exception {
        DeviceGeneratorFunctions deviceFunctions = new DeviceGeneratorFunctions(dataRepository, "Default1");

        FunctionEntity default1Refresh = dataRepository.loadFunctionSync(deviceEntity.getId(),"Default1Refresh");

        logInfo("Getting temperature");
        double temperature = deviceFunctions.getCurrentTemperature();

        logInfo("Temp is " + temperature);
        FunctionEntity f = dataRepository.loadFunctionSync(deviceEntity.getId(),"Default1Refresh");
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
