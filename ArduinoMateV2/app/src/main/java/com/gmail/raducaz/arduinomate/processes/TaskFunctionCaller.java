package com.gmail.raducaz.arduinomate.processes;

import android.util.Log;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionStateUpdater;

import java.util.concurrent.ExecutorService;

public class TaskFunctionCaller implements TaskInterface {

    private String TAG = "TaskFunctionCaller";

    private final FunctionEntity function;
    private long executionId;
    private FunctionExecutionEntity functionExecution;
    private final DataRepository mRepository;

    public TaskFunctionCaller(DataRepository dataRepository, FunctionEntity function) {
        this.function = function;

        mRepository = dataRepository;
    }

    public void execute() {

        functionExecution = new FunctionExecutionEntity();
        functionExecution.setFunctionId(function.getId());
        functionExecution.setName(function.getName());

        FunctionStateUpdater functionStateUpdater = new FunctionStateUpdater(mRepository, "Command sent to device ...",functionExecution);

        try {
            // Automatically insert the log as well = Execution started...
            functionStateUpdater.startFunctionExecution();

            DeviceEntity device = mRepository.loadDeviceSync(function.getDeviceId());
            ProcessExecutor processExecutor = new ProcessExecutor(device.getIp(), device.getPort());

            switch (function.getName())
            {
                case "GeneratorOnOff":
                    ProcessStartGenerator.execute(processExecutor);
                break;
                default:

            }


        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
            functionStateUpdater.insertExecutionLog(exc);
            functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.ERROR);
        }
    }

}
