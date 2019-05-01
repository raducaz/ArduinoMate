package com.gmail.raducaz.arduinomate.processes;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionStateUpdater;

public class TaskFunctionCaller implements TaskInterface {

    private String TAG = "TaskFunctionCaller";

    private final DeviceEntity device;
    private final FunctionEntity function;
    private long executionId;
    private FunctionExecutionEntity functionExecution;
    private final DataRepository mRepository;
    private  FunctionResultStateEnum desiredFunctionResult;
    private boolean isAutoExecution;

    // This is used by the UI
    public TaskFunctionCaller(DataRepository dataRepository, FunctionEntity function) {
        this.function = function;

        mRepository = dataRepository;
        this.desiredFunctionResult = FunctionResultStateEnum.NA;
        isAutoExecution = false;
        this.device = mRepository.loadDeviceSync(function.getDeviceId());
    }

    // Use this constructor to state the desired FunctionResultState after the execution
    public TaskFunctionCaller(DataRepository dataRepository, String deviceIp, String functionName, FunctionResultStateEnum desiredFunctionResult) {
        DeviceEntity deviceEntity = dataRepository.loadDeviceSync(deviceIp);
        FunctionEntity functionEntity = dataRepository.loadFunctionSync(deviceEntity.getId(), functionName);
        this.function = functionEntity;

        mRepository = dataRepository;
        this.device = mRepository.loadDeviceSync(function.getDeviceId());
        this.desiredFunctionResult = desiredFunctionResult;
        isAutoExecution = true;
    }

    public void execute() {
        try {
            switch (function.getName())
            {
                case "GeneratorOnOff":
                    new ProcessGeneratorOnOff(mRepository, device.getIp()).execute(isAutoExecution, desiredFunctionResult);
                break;
                case "PowerOnOff":
                    new ProcessPowerOnOff(mRepository, device.getIp()).execute(isAutoExecution, desiredFunctionResult);
                    break;
                case "PumpOnOff":
                    new ProcessPumpOnOff(mRepository, device.getIp()).execute(isAutoExecution, desiredFunctionResult);
                    break;
                default:

            }
        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
        }
    }

}
