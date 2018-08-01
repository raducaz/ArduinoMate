package com.gmail.raducaz.arduinomate.service;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.model.FunctionExecution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionStateUpdater {
    DataRepository dataRepository;
    DeviceStateInfo deviceStateInfo;

    FunctionExecutionEntity functionExecution;

    public FunctionStateUpdater(DataRepository dataRepository, FunctionExecutionEntity functionExecution) {
        this.dataRepository = dataRepository;
        this.functionExecution = functionExecution;
        this.deviceStateInfo = new DeviceStateInfo(null);
    }

    public FunctionStateUpdater(DataRepository dataRepository, String msg, FunctionExecutionEntity functionExecution) {
        this.dataRepository = dataRepository;
        deviceStateInfo = new DeviceStateInfo(msg);
        this.functionExecution = functionExecution;
    }

    public FunctionStateUpdater(DataRepository dataRepository, DeviceStateInfo deviceStateInfo, FunctionExecutionEntity functionExecution) {
        this.dataRepository = dataRepository;
        this.deviceStateInfo = deviceStateInfo;
        this.functionExecution = functionExecution;
    }
    private void Initialize(){

    }

    public void insertExecutionLog()
    {
        String message = deviceStateInfo.getMessage();

        if (message!=null) {
            insertExecutionLog(message);
        }
    }

    public long insertExecutionLog(String msg){
        ExecutionLogEntity log = new ExecutionLogEntity();
        log.setExecutionId(functionExecution.getId());
        log.setLog(msg);
        log.setDate(DateConverter.toDate(System.currentTimeMillis()));
        return dataRepository.insertExecutionLog(log);
    }
    public long startFunctionExecution(){
        functionExecution.setCallState(FunctionCallStateEnum.EXECUTING.getId());
        functionExecution.setStartDate(DateConverter.toDate(System.currentTimeMillis()));
        long executionId = dataRepository.insertFunctionExecution(functionExecution);
        functionExecution.setId(executionId);

        // Automatically insert log as well
        insertExecutionLog("Command sent to device ... ");

        return executionId;
    }
    public void updateFunctionExecution(FunctionCallStateEnum callState) {
        functionExecution.setEndDate(DateConverter.toDate(System.currentTimeMillis()));
        functionExecution.setCallState(callState.getId());
        functionExecution.setResultState(deviceStateInfo.getFunctionState().getId());

        dataRepository.updateFunctionExecution(functionExecution);

        if (callState.equals(FunctionCallStateEnum.READY)) {
            insertExecutionLog("Execution completed");
        }
        if (callState.equals(FunctionCallStateEnum.ERROR)) {
            insertExecutionLog("Execution failed");
        }
    }
}
