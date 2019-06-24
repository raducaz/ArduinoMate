package com.gmail.raducaz.arduinomate.processes;

import android.util.Log;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.remote.CommandToControllerPublisher;
import com.gmail.raducaz.arduinomate.remote.RemoteResetCommand;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.concurrent.ExecutorService;

public class TaskFunctionReset implements TaskInterface {

    private String TAG = "TaskFunctionReset";

    private final FunctionEntity function;
    private final DataRepository mRepository;
    public TaskFunctionReset(final DataRepository repository, FunctionEntity function) {
        this.function = function;

        mRepository = repository;
    }
    // Use this constructor to reset all functions
    public TaskFunctionReset(final DataRepository repository) {
        function = null;

        mRepository = repository;
    }

    public void execute() {

        try {
            // If client redirect the command to controller
            if(!mRepository.getSettingsSync().getIsController())
            {
                CommandToControllerPublisher sender = new CommandToControllerPublisher(ArduinoMateApp.AmqConnection,
                        ArduinoMateApp.COMMAND_QUEUE);

                RemoteResetCommand cmd = new RemoteResetCommand(function);
                sender.SendCommand(cmd);
            } else {
                if (function != null) {
                    mRepository.deletePinStatesByFunction(function.getId());
                    mRepository.deleteFunctionExecutions(function.getId());
                    mRepository.deleteExecutionLogs(function.getId());

                    // Reset also the function states
                    function.setCallState(FunctionCallStateEnum.READY.getId());
                    function.setResultState(FunctionResultStateEnum.NA.getId());
                    mRepository.updateFunction(function);
                } else {
                    //Reset all !!
                    mRepository.deleteAllPinStates();
                    mRepository.deleteAllFunctionExecutions();
                    mRepository.deleteAllExecutionLogs();

                    // Reset also the function states
                    mRepository.updateAllFunctionStates(FunctionCallStateEnum.READY.getId(), FunctionResultStateEnum.NA.getId());
                }
            }

        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
        }
    }

}
