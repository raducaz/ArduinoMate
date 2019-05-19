package com.gmail.raducaz.arduinomate.events;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionCaller;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.gmail.raducaz.arduinomate.ui.TaskExecutor;

import java.util.Map;

public class DeviceStateChangeEvent {

    private String TAG = "DeviceStateChangeEvent";

    DataRepository dataRepository;
    DeviceEntity deviceEntity;
    public DeviceStateChangeEvent(DataRepository dataRepository, DeviceEntity deviceEntity)
    {
        this.dataRepository = dataRepository;
        this.deviceEntity = deviceEntity;
    }
    public void trigger(Map<String, Double> oldPinStates, Map<String, Double> newPinStates)
    {
        // TODO: Handle changes here

        try {
            if(deviceEntity.getName().equals("Generator")) {
                // if current is below a threshold generator must be stopped
                if (newPinStates.containsKey("A1") && (newPinStates.get("A1") < 0.07)
                        && oldPinStates.containsKey("A1") && (oldPinStates.get("A1") > 0.07)) {
                    TaskFunctionCaller functionCaller = new TaskFunctionCaller(dataRepository,
                            deviceEntity.getName(),
                            "GeneratorOnOff",
                            FunctionResultStateEnum.OFF,
                            "NO current consumption");
                    new TaskExecutor().execute(functionCaller);
                }
            }
        }
        catch (Exception exc) {
            Log.e(TAG, exc.getMessage());
        }
    }
}
