package com.gmail.raducaz.arduinomate.events;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionCaller;
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
            // Example - if pin 1 has a new value of 1 then call function GeneratorOnOff
            if (newPinStates.containsKey("1") && newPinStates.get("1").equals(1)) {
                FunctionEntity functionEntity = dataRepository.loadFunctionSync(deviceEntity.getId(), "GeneratorOnOff");
                if(functionEntity.getIsAutoEnabled()) {
                    TaskFunctionCaller functionCaller = new TaskFunctionCaller(dataRepository, functionEntity);
                    new TaskExecutor().execute(functionCaller);
                }
            }
        }
        catch (Exception exc) {
            Log.e(TAG, exc.getMessage());
        }
    }
}
