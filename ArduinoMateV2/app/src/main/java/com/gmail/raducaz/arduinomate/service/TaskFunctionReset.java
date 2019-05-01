package com.gmail.raducaz.arduinomate.service;

import android.util.Log;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.processes.TaskInterface;

import java.util.concurrent.ExecutorService;

public class TaskFunctionReset implements TaskInterface {

    private String TAG = "TaskFunctionReset";

    private final ArduinoMateApp mApplication;
    private final FunctionEntity function;
    private long executionId;
    private FunctionExecutionEntity functionExecution;
    private final DataRepository mRepository;
    private final ExecutorService mExecutor;
    public TaskFunctionReset(final ArduinoMateApp app, FunctionEntity function) {
        mApplication = app;
        this.function = function;

        mRepository = mApplication.getRepository();
        mExecutor = mApplication.getNetworkExecutor();
    }

    public void execute() {

        try {
            mRepository.deletePinStatesByFunction(function.getId());
            mRepository.deleteFunctionExecutions(function.getId());
            mRepository.deleteExecutionLogs(function.getId());
        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
        }
    }

}
