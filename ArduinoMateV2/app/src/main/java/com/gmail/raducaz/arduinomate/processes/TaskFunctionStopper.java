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

import java.util.List;

public class TaskFunctionStopper implements TaskInterface {

    private String TAG = "TaskFunctionStopper";

    private final DataRepository mRepository;
    private final DeviceEntity deviceEntity;

    public TaskFunctionStopper(final DataRepository repository, DeviceEntity deviceEntity) {

        mRepository = repository;
        this.deviceEntity = deviceEntity;
    }

    public void execute() {

        try {

            if (deviceEntity != null) {
                List<FunctionEntity> deviceFunctions = mRepository.loadFunctionsSync(deviceEntity.getId());
                for (FunctionEntity f : deviceFunctions)
                {
                    f.setCallState(0);
                    f.setResultState(0);
                    mRepository.updateFunction(f);
                    mRepository.insertExecutionLogOnLastFunctionExecution(f.getId(), "DEVICE RESTARTED");
//                    TaskFunctionCaller caller = new TaskFunctionCaller(mRepository, deviceEntity.getName(),
//                            f.getName(), FunctionResultStateEnum.OFF, "Device restarted");
//                    caller.setAutoExecution(false);
//                    caller.setOnDemand(true);
//                    caller.run();
                }

                //mRepository.updateDeviceFunctionsStates(deviceEntity.getId(), FunctionCallStateEnum.READY.getId(), FunctionResultStateEnum.OFF.getId());
            }


        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
        }
    }

}
