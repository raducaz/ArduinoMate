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

    private String deviceName = null;
    private String functionName;

    private DeviceEntity device;
    private FunctionEntity function;
    private final DataRepository mRepository;
    private  FunctionResultStateEnum desiredFunctionResult;
    private boolean isAutoExecution;

    // This is used by the UI
    public TaskFunctionCaller(DataRepository dataRepository, FunctionEntity function) {
        this.function = function;
        this.functionName = function.getName();

        mRepository = dataRepository;
        this.desiredFunctionResult = FunctionResultStateEnum.NA;
        isAutoExecution = false;

        // Do not add code to access data base here, as this contructor is called directly from UI
    }

    // Use this constructor to state the desired FunctionResultState after the execution
    public TaskFunctionCaller(DataRepository dataRepository, String deviceName, String functionName, FunctionResultStateEnum desiredFunctionResult) {
        this.deviceName = deviceName;
        this.functionName = functionName;

        mRepository = dataRepository;
        this.desiredFunctionResult = desiredFunctionResult;
        isAutoExecution = true;
    }

    public void execute() {
        try {
            if(deviceName == null) {
                device = mRepository.loadDeviceSync(function.getDeviceId());
                deviceName = device.getName();
            }

            switch (functionName)
            {
                case "GeneratorOnOff":
                    new ProcessGeneratorOnOff(mRepository, deviceName).execute(isAutoExecution, desiredFunctionResult);
                break;
                case "PowerOnOff":
                    new ProcessPowerOnOff(mRepository, deviceName).execute(isAutoExecution, desiredFunctionResult);
                    break;
                case "PumpOnOff":
                    new ProcessPumpOnOff(mRepository, deviceName).execute(isAutoExecution, desiredFunctionResult);
                    break;
                case "HouseWaterOnOff":
                    new ProcessHouseWaterOnOff(mRepository, deviceName).execute(isAutoExecution, desiredFunctionResult);
                    break;
                default:

            }
        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
        }
    }

}
