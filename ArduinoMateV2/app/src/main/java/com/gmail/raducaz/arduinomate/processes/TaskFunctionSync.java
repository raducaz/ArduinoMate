package com.gmail.raducaz.arduinomate.processes;

import android.util.Log;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGenericFunctions;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;
import com.gmail.raducaz.arduinomate.model.ExecutionLog;
import com.gmail.raducaz.arduinomate.remote.CommandToControllerPublisher;
import com.gmail.raducaz.arduinomate.remote.RemoteResetCommand;
import com.gmail.raducaz.arduinomate.remote.RemoteStateUpdate;
import com.gmail.raducaz.arduinomate.remote.RemoteSyncCommand;
import com.gmail.raducaz.arduinomate.remote.StateFromControllerPublisher;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.List;

import static com.gmail.raducaz.arduinomate.ArduinoMateApp.AmqConnection;
import static com.gmail.raducaz.arduinomate.ArduinoMateApp.STATES_EXCHANGE;

public class TaskFunctionSync implements TaskInterface {

    private String TAG = "TaskFunctionSync";

    private final long functionId;
    private final DataRepository mRepository;

    public TaskFunctionSync(final DataRepository repository, long functionId) {
        this.functionId = functionId;

        mRepository = repository;
    }
    // Use this constructor to reset all functions
    public TaskFunctionSync(final DataRepository repository) {
        functionId = 0;

        mRepository = repository;
    }

    private void SendStateToRemoteClients(RemoteStateUpdate stateUpdate)
    {
        SettingsEntity settings = mRepository.getSettingsSync();
        if(settings.getIsController() && settings.getPermitRemoteControl()) {
            StateFromControllerPublisher.SendState(mRepository, AmqConnection, STATES_EXCHANGE, stateUpdate);
        }
    }

    public void execute() {

        try {
            FunctionEntity function = null;
            if(functionId>0) function = mRepository.loadFunctionSync(functionId);

            // If client redirect the command to controller
            if(!mRepository.getSettingsSync().getIsController())
            {
                CommandToControllerPublisher sender = new CommandToControllerPublisher(ArduinoMateApp.AmqConnection,
                        ArduinoMateApp.COMMAND_QUEUE);

                RemoteSyncCommand cmd = new RemoteSyncCommand(function);
                sender.SendCommand(cmd, 5);

                mRepository.insertExecutionLogOnLastFunctionExecution(function.getId(),
                        "Sync cmd sent remotely...");
            } else {
                // Execute on controller the sync command

                if(function != null)
                {
                    syncFunction(function);
                }
                else
                {
                    List<FunctionEntity> functions = mRepository.loadAllFunctionsSync();
                    for(FunctionEntity f :functions)
                    {
                        syncFunction(f);
                    }
                }
            }

        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
        }
    }

    public void syncFunction(FunctionEntity function)
    {
        // Get last execution for function
        FunctionExecutionEntity functionExecutionEntity = mRepository.loadLastFunctionExecutionSync(function.getId());
        SendStateToRemoteClients(new RemoteStateUpdate(functionExecutionEntity, "insertFunctionExecution"));

        // Get logs for last execution
        List<ExecutionLogEntity> logs = mRepository.loadExecutionLogSync(functionExecutionEntity.getId());
        for (ExecutionLogEntity log: logs) {
            SendStateToRemoteClients(new RemoteStateUpdate(log, "insertExecutionLog"));
        }

        // Get function states
        SendStateToRemoteClients(new RemoteStateUpdate(function, "updateFunction"));
    }

}
