package com.gmail.raducaz.arduinomate.processes;

import android.util.Log;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.remote.CommandToControllerPublisher;
import com.gmail.raducaz.arduinomate.remote.RemoteFunctionCommand;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

/// Entry point for all function executions
public class TaskFunctionCaller implements Runnable {

    private String TAG = "TaskFunctionCaller";

    private ArduinoMateApp application;
    private String deviceName = null;
    private String functionName;

    private DeviceEntity device;
    private long functionId;
    private final DataRepository mRepository;
    private FunctionResultStateEnum desiredFunctionResult;

    public void setAutoExecution(boolean autoExecution) {
        isAutoExecution = autoExecution;
    }

    private boolean isAutoExecution;

    public void setOnDemand(boolean onDemand) {
        isOnDemand = onDemand;
    }

    private boolean isOnDemand;
    private String reasonDetails;

    // This is used by the UI
    public TaskFunctionCaller(DataRepository dataRepository, long functionId) {
        this.functionId = functionId;

        mRepository = dataRepository;
        this.desiredFunctionResult = FunctionResultStateEnum.NA;
        isAutoExecution = false;
        isOnDemand = true;
        reasonDetails = "On demand execution";
        // Do not add code to access data base here, as this contructor is called directly from UI
    }

    // Use this constructor to state the desired FunctionResultState after the execution
    public TaskFunctionCaller(DataRepository dataRepository, String deviceName, String functionName, FunctionResultStateEnum desiredFunctionResult, String reasonDetails) {

        this.deviceName = deviceName;
        this.functionName = functionName;

        mRepository = dataRepository;
        this.desiredFunctionResult = desiredFunctionResult;
        isAutoExecution = true;
        isOnDemand = false;
        this.reasonDetails = reasonDetails;
    }

    public void run() {
        FunctionEntity function = null;
        try {
            if(functionId > 0)
            {
                function = mRepository.loadFunctionSync(functionId);
                functionName = function.getName();
            }

            if(deviceName == null) {
                device = mRepository.loadDeviceSync(function.getDeviceId());
                deviceName = device.getName();
            }

            //If application is a client (not controller) redirect the command to controller
            if(!mRepository.getSettingsSync().getIsController())
            {
                CommandToControllerPublisher sender = new CommandToControllerPublisher(ArduinoMateApp.AmqConnection,
                        ArduinoMateApp.COMMAND_QUEUE);

                // By default send OFF command if the current state of the function is not certain
                FunctionResultStateEnum orderedState = FunctionResultStateEnum.OFF;
                if(function!=null)
                {
                    if(function.getResultState() == FunctionResultStateEnum.OFF.getId())
                        orderedState = FunctionResultStateEnum.ON;
                }

                RemoteFunctionCommand cmd = new RemoteFunctionCommand(deviceName,
                        functionName,
                        orderedState);

                sender.SendCommand(cmd, 5);
                mRepository.insertExecutionLogOnLastFunctionExecution(function.getId(), "Exec cmd sent remotely...");
            } else {

                switch (functionName) {
                    case "SocOnOff":
                        new ProcessSocOnOff(mRepository, deviceName).execute(isAutoExecution, isOnDemand, desiredFunctionResult, reasonDetails);
                        break;
                    case "IgnitionOnOff":
                        new ProcessIgnitionOnOff(mRepository, deviceName).execute(isAutoExecution, isOnDemand, desiredFunctionResult, reasonDetails);
                        break;
                    case "GeneratorOnOff":
                        new ProcessGeneratorOnOff(mRepository, deviceName).execute(isAutoExecution, isOnDemand, desiredFunctionResult, reasonDetails);
                        break;
                    case "PowerOnOff":
                        new ProcessPowerOnOff(mRepository, deviceName).execute(isAutoExecution, isOnDemand, desiredFunctionResult, reasonDetails);
                        break;
                    case "PumpOnOff":
                        new ProcessPumpOnOff(mRepository, deviceName).execute(isAutoExecution, isOnDemand, desiredFunctionResult, reasonDetails);
                        break;
                    case "HouseWaterOnOff":
                        new ProcessHouseWaterOnOff(mRepository, deviceName).execute(isAutoExecution, isOnDemand, desiredFunctionResult, reasonDetails);
                        break;
                    case "GardenWaterOnOff":
                        new ProcessGardenWaterOnOff(mRepository, deviceName).execute(isAutoExecution, isOnDemand, desiredFunctionResult, reasonDetails);
                        break;
                    case "WaterSupplyTapOnOff":
                        new ProcessWaterSupplyTapOnOff(mRepository, deviceName).execute(isAutoExecution, isOnDemand, desiredFunctionResult, reasonDetails);
                        break;
                    case "LeftIrrigationOnOff":
                        new ProcessLeftIrrigationOnOff(mRepository, deviceName).execute(isAutoExecution, isOnDemand, desiredFunctionResult, reasonDetails);
                        break;
                    case "RightIrrigationOnOff":
                        new ProcessRightIrrigationOnOff(mRepository, deviceName).execute(isAutoExecution, isOnDemand, desiredFunctionResult, reasonDetails);
                        break;
                    case "BoilerOnOff":
                        new ProcessBoilerOnOff(mRepository, deviceName).execute(isAutoExecution, isOnDemand, desiredFunctionResult, reasonDetails);
                        break;
                    default:
                }
            }
        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
        }
    }

}
