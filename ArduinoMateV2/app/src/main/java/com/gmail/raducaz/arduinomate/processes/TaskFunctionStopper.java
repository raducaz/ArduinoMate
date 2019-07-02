package com.gmail.raducaz.arduinomate.processes;

import android.util.Log;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGenericFunctions;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.remote.CommandToControllerPublisher;
import com.gmail.raducaz.arduinomate.remote.RemoteResetCommand;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

public class TaskFunctionStopper implements TaskInterface {

    private String TAG = "TaskFunctionStopper";

    private final DataRepository mRepository;
    private final DeviceEntity deviceEntity;

    // Use this constructor to reset all functions of a device
    public TaskFunctionStopper(final DataRepository repository, DeviceEntity deviceEntity) {

        mRepository = repository;
        this.deviceEntity = deviceEntity;
    }

    public void execute() {

        try {

            if (deviceEntity != null) {
                mRepository.updateDeviceFunctionsStates(deviceEntity.getId(), FunctionCallStateEnum.READY.getId(), FunctionResultStateEnum.OFF.getId());
            }


        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
        }
    }

}
