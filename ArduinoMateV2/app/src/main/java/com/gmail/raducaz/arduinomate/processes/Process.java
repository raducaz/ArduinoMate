package com.gmail.raducaz.arduinomate.processes;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.model.FunctionExecution;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionStateUpdater;

public abstract class Process {

    //TODO: This should not be mandatory for a process - but now neededto determine the associated function
    String functionName;
    DeviceEntity deviceEntity;
    FunctionEntity function;
    FunctionExecutionEntity functionExecution;
    FunctionStateUpdater functionStateUpdater;
    DataRepository dataRepository;


    public Process(DataRepository dataRepository, String deviceIp, String functionName)
    {
        this.functionName = functionName;
        this.dataRepository = dataRepository;
        this.deviceEntity = dataRepository.loadDeviceSync(deviceIp);
        this.function = dataRepository.loadFunctionSync(deviceEntity.getId(), functionName);
    }

    public boolean execute(boolean isAutoExecution, FunctionResultStateEnum desiredResult) {

        functionExecution = new FunctionExecutionEntity();
        functionExecution.setFunctionId(function.getId());
        functionExecution.setName(function.getName());

        functionStateUpdater = new FunctionStateUpdater(dataRepository, "Function started ...",functionExecution);

        try {
            if (isAutoExecution && !function.getIsAutoEnabled()) {
                functionStateUpdater.insertExecutionLog("Function is not auto enabled.Execution stopped");
                functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.READY);

                return false;
            }

            // Automatically insert the log as well = Execution started...
            functionStateUpdater.startFunctionExecution();

            if (desiredResult == FunctionResultStateEnum.OFF) {
                return off();
            } else if (desiredResult == FunctionResultStateEnum.ON) {
                return on();
            } else {

            }

        } catch (Exception exc) {

            Log.e(functionName, exc.getMessage());
            functionStateUpdater.insertExecutionLog(exc);
            functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.ERROR);
        }
        finally {
            return false;
        }
    }

    protected boolean on() throws Exception
    {
        // Function result state needs to be handled here - will not be communicated by arduino
        // This will automatically set the Execution Log as well
        functionExecution = functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.READY); // Success
        functionExecution.setResultState(FunctionResultStateEnum.ON.getId());
        return true;
    }

    protected boolean off() throws Exception
    {
        // Function result state needs to be handled here - will not be communicated by arduino
        // This will automatically set the Execution Log as well
        functionExecution = functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.READY); // Success
        functionExecution.setResultState(FunctionResultStateEnum.OFF.getId());
        return true;
    }
}
