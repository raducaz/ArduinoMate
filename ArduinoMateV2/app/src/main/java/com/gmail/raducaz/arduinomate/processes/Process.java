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

    public FunctionResultStateEnum getFunctionResultState()
    {
        return FunctionResultStateEnum.forInt(function.getResultState());
    }

    public FunctionCallStateEnum getFunctionCallState()
    {
        return FunctionCallStateEnum.forInt(function.getCallState());
    }

    public Process(DataRepository dataRepository, String deviceName, String functionName)
    {
        this.functionName = functionName;
        this.dataRepository = dataRepository;
        this.deviceEntity = dataRepository.loadDeviceByNameSync(deviceName);
        this.function = dataRepository.loadFunctionSync(deviceEntity.getId(), functionName);
    }
    public Process(DataRepository dataRepository, long deviceId, String functionName)
    {
        this.functionName = functionName;
        this.dataRepository = dataRepository;
        this.deviceEntity = dataRepository.loadDeviceSync(deviceId);
        this.function = dataRepository.loadFunctionSync(deviceEntity.getId(), functionName);

    }
    public boolean execute(boolean isAutoExecution, FunctionResultStateEnum desiredResult) {

        try {

            boolean isFunctionExecutionJustStarted = false;
            // Get the current execution (previous) to insert this log to it
            functionExecution = dataRepository.loadLastFunctionExecutionSync(function.getId());
            if(functionExecution == null) {

                startExecution();
                isFunctionExecutionJustStarted = true;
            }
            else
            {
                functionStateUpdater = new FunctionStateUpdater(dataRepository, "Function started ...", functionExecution);
            }

            if(!isFunctionExecutionJustStarted && functionExecution.getCallState()==FunctionCallStateEnum.EXECUTING.getId())
            {
                functionStateUpdater.insertExecutionLog("try concurrent execution ...");
                return true;
            }
            if (isAutoExecution && !function.getIsAutoEnabled()) {
                endExecution(FunctionCallStateEnum.READY, "Automatic execution is not enabled for this function.");
                return true;
            }
            if(desiredResult.getId()==FunctionResultStateEnum.ON.getId() &&
                    desiredResult.getId()==function.getResultState())
            {
                endExecution(FunctionCallStateEnum.READY, "already ON...");
                return true;
            }

            // From this point on - Start new execution - Automatically insert the log as well = Execution started...
            startExecution();


            if (desiredResult == FunctionResultStateEnum.OFF) {
                //Redo the off command - should not be a problem
                // Also this may solve problems like status is outdated but process is actually running
                // In this case it would be impossible to stop
                return off();

            } else if (desiredResult == FunctionResultStateEnum.ON) {

                // Double check - should not get here
                if(function.getResultState()==FunctionResultStateEnum.ON.getId()) {
                    return true; // don't do it again, it may break things (generator is already started..)
                    //TODO: Test if there is better to automatically run off before each on function ?
                }

                return on();

            } else {
                if(function.getResultState()==FunctionResultStateEnum.ON.getId()
                        || function.getResultState()==FunctionResultStateEnum.NA.getId()
                        || function.getResultState()==FunctionResultStateEnum.ERROR.getId())
                    return off();
                else
                    return on();
            }

        } catch (Exception exc) {

            Log.e(functionName, exc.getMessage());
            functionStateUpdater.insertExecutionLog(exc);
            functionExecution = functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.ERROR);
            return false;
        }
    }
    private void endExecution(FunctionCallStateEnum callState, String logMessage)
    {
        functionStateUpdater.insertExecutionLog(logMessage);
        functionStateUpdater.updateFunctionExecution(callState);
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

    private void startExecution()
    {
        functionExecution = new FunctionExecutionEntity();

        functionStateUpdater = new FunctionStateUpdater(dataRepository, "Function started ...", functionExecution);
        functionExecution.setFunctionId(function.getId());
        functionExecution.setName(function.getName());
        functionStateUpdater.startFunctionExecution();
    }
}