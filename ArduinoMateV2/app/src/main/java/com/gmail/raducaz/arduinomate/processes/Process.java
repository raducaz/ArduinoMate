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

        functionExecution = new FunctionExecutionEntity();
        functionExecution.setFunctionId(function.getId());
        functionStateUpdater = new FunctionStateUpdater(dataRepository, "Function started ...",functionExecution);
    }
    public Process(DataRepository dataRepository, long deviceId, String functionName)
    {
        this.functionName = functionName;
        this.dataRepository = dataRepository;
        this.deviceEntity = dataRepository.loadDeviceSync(deviceId);
        this.function = dataRepository.loadFunctionSync(deviceEntity.getId(), functionName);

        functionExecution = new FunctionExecutionEntity();
        functionExecution.setFunctionId(function.getId());
        functionStateUpdater = new FunctionStateUpdater(dataRepository, "Function started ...",functionExecution);
    }
    public void updateFunctionResultState(FunctionResultStateEnum resultState)
    {
        functionStateUpdater.updateFunctionExecution(resultState);
    }
    public boolean execute(boolean isAutoExecution, FunctionResultStateEnum desiredResult) {

        try {
            if (isAutoExecution && !function.getIsAutoEnabled()) {
                functionStateUpdater.insertExecutionLog("Function is not auto enabled.Execution stopped");
                functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.READY);

                return false;
            }

            // Automatically insert the log as well = Execution started...
            functionStateUpdater.startFunctionExecution();

            if (desiredResult == FunctionResultStateEnum.OFF) {
                return off(); //Redo the off command - should not be a problem
            } else if (desiredResult == FunctionResultStateEnum.ON) {
                if(function.getResultState()==FunctionResultStateEnum.ON.getId())
                    //throw new Exception("Function already ON. Reset function and retry.");
                    return true; // don't do it again, it may break things (generator is already started..)

                    //TODO: Test if there is better to automatically run off before each on function ?

                return on();
            } else {
                if(function.getResultState()==FunctionResultStateEnum.ON.getId())
                    return off();
                else
                    return on();
            }

        } catch (Exception exc) {

            Log.e(functionName, exc.getMessage());
            functionStateUpdater.insertExecutionLog(exc);
            functionExecution = functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.ERROR);
            //functionExecution = functionStateUpdater.updateFunctionExecution(FunctionResultStateEnum.ERROR);
            return false;
        }
    }

    protected boolean on() throws Exception
    {
        // Function result state needs to be handled here - will not be communicated by arduino
        // This will automatically set the Execution Log as well
        functionExecution = functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.READY); // Success
        functionExecution = functionStateUpdater.updateFunctionExecution(FunctionResultStateEnum.ON);
        return true;
    }

    protected boolean off() throws Exception
    {
        // Function result state needs to be handled here - will not be communicated by arduino
        // This will automatically set the Execution Log as well
        functionExecution = functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.READY); // Success
        functionExecution = functionStateUpdater.updateFunctionExecution(FunctionResultStateEnum.OFF);
        return true;
    }
}
