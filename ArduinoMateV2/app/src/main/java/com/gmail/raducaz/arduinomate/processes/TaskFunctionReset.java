package com.gmail.raducaz.arduinomate.processes;

import android.util.Log;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.processes.TaskInterface;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.concurrent.ExecutorService;

public class TaskFunctionReset implements TaskInterface {

    private String TAG = "TaskFunctionReset";

    private final ArduinoMateApp mApplication;
    private final FunctionEntity function;
    private final DataRepository mRepository;
    private final ExecutorService mExecutor;
    public TaskFunctionReset(final ArduinoMateApp app, FunctionEntity function) {
        mApplication = app;
        this.function = function;

        mRepository = mApplication.getRepository();
        mExecutor = mApplication.getNetworkExecutor();
    }
    // Use this constructor to reset all functions
    public TaskFunctionReset(final ArduinoMateApp app) {
        mApplication = app;
        function = null;

        mRepository = mApplication.getRepository();
        mExecutor = mApplication.getNetworkExecutor();
    }

    public void execute() {

        try {
            if(function != null) {
                mRepository.deletePinStatesByFunction(function.getId());
                mRepository.deleteFunctionExecutions(function.getId());
                mRepository.deleteExecutionLogs(function.getId());

                // Reset also the function states
                function.setCallState(FunctionCallStateEnum.READY.getId());
                function.setResultState(FunctionResultStateEnum.NA.getId());
                mRepository.updateFunction(function);
            }
            else
            {
                //Reset all !!
                mRepository.deleteAllPinStates();
                mRepository.deleteAllFunctionExecutions();
                mRepository.deleteAllExecutionLogs();

                // Reset also the function states
                mRepository.updateAllFunctionStates(FunctionCallStateEnum.READY.getId(), FunctionResultStateEnum.NA.getId());
            }

        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
        }
    }

}
