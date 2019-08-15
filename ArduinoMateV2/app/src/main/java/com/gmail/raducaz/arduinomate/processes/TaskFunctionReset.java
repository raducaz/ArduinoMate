package com.gmail.raducaz.arduinomate.processes;

import android.util.Log;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.commands.DeviceGenericFunctions;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.remote.CommandToControllerPublisher;
import com.gmail.raducaz.arduinomate.remote.RemoteResetCommand;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.orhanobut.logger.Logger;

import java.util.concurrent.ExecutorService;

public class TaskFunctionReset implements TaskInterface {

    private String TAG = "TaskFunctionReset";

    private final long functionId;
    private final DataRepository mRepository;
    private boolean alsoRestart = false;
    private boolean resetRemote = false;

    public TaskFunctionReset(final DataRepository repository, long functionId, boolean alsoRestart, boolean resetRemote) {
        this.functionId = functionId;
        this.alsoRestart = alsoRestart;
        this.resetRemote = resetRemote;

        mRepository = repository;
    }
    // Use this constructor to reset all functions
    public TaskFunctionReset(final DataRepository repository, boolean resetRemote) {
        functionId = 0;
        this.resetRemote = resetRemote;

        mRepository = repository;
    }

    public void execute() {

        try {
            FunctionEntity function = null;
            if(functionId>0) function = mRepository.loadFunctionSync(functionId);

            // If client redirect the command to controller
            if(!mRepository.getSettingsSync().getIsController() && resetRemote)
            {
                CommandToControllerPublisher sender = new CommandToControllerPublisher(ArduinoMateApp.AmqConnection,
                        ArduinoMateApp.COMMAND_QUEUE);

                RemoteResetCommand cmd = new RemoteResetCommand(function, alsoRestart);
                sender.SendCommand(cmd, 5);
                mRepository.insertExecutionLogOnLastFunctionExecution(function.getId(),
                        (alsoRestart? "Restart" : "Reset") + " cmd sent remotely...");
            } else {
                if (function != null) {
                    mRepository.deletePinStatesByFunction(function.getId());
                    mRepository.deleteFunctionExecutions(function.getId());
                    mRepository.deleteExecutionLogs(function.getId());

                    // Reset also the function states
                    function.setCallState(FunctionCallStateEnum.READY.getId());
                    function.setResultState(FunctionResultStateEnum.NA.getId());
                    mRepository.updateFunction(function);

                    // Restart device if alsoRestart
                    if(alsoRestart)
                    {
                        DeviceGenericFunctions deviceGenericFunctions = new DeviceGenericFunctions(mRepository, function.getDeviceId());
                        deviceGenericFunctions.restartDevice();
                        mRepository.insertExecutionLogOnLastFunctionExecution(function.getId(), "Device RESTARTED");
                    }
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

            Logger.e(TAG+ exc.getMessage());
        }
    }

}
