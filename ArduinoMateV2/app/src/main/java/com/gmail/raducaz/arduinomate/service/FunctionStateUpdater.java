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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionStateUpdater {
    DataRepository dataRepository;
    DeviceStateInfo deviceStateInfo;

    JSONObject _messageData = null;
    String _messageRaw = null;

    FunctionExecutionEntity functionExecution;

    public FunctionStateUpdater(DataRepository dataRepository, FunctionExecutionEntity functionExecution) {
        this.dataRepository = dataRepository;
        this.functionExecution = functionExecution;
        this.deviceStateInfo = new DeviceStateInfo(null);
    }
    public FunctionStateUpdater(DataRepository dataRepository, String msg) {
        this.dataRepository = dataRepository;
        this.deviceStateInfo = new DeviceStateInfo(msg);

        String fctName = deviceStateInfo.getFunctionName();
        String devName = deviceStateInfo.getDeviceName();
        if(devName != null && fctName != null) {
            DeviceEntity deviceEntity = this.dataRepository.loadDeviceByNameSync(devName);
            if(deviceEntity != null) {
                FunctionEntity functionEntity = this.dataRepository.loadFunctionSync(deviceEntity.getId(), fctName);

                if(functionEntity != null) {
                    this.functionExecution = this.dataRepository.loadLastUnfinishedFunctionExecution(functionEntity.getId());
                    if (functionExecution == null) {
                        functionExecution = new FunctionExecutionEntity();
                        functionExecution.setFunctionId(functionEntity.getId());
                        functionExecution.setName(functionEntity.getName());
                        this.startFunctionExecution();
                    }
                }
            }
        }
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
        if(functionExecution == null)
            return -1;

        ExecutionLogEntity log = new ExecutionLogEntity();
        log.setExecutionId(functionExecution.getId());
        log.setLog(msg);
        log.setDate(DateConverter.toDate(System.currentTimeMillis()));
        log.setFunctionName(functionExecution.getName());
        return dataRepository.insertExecutionLog(log);
    }
    public long insertExecutionLog(Exception exc){
        if(functionExecution == null)
            return -1;

        ExecutionLogEntity log = new ExecutionLogEntity();
        log.setExecutionId(functionExecution.getId());
        log.setLog(exc.getMessage() + " " + exc.getStackTrace().toString());
        log.setDate(DateConverter.toDate(System.currentTimeMillis()));
        log.setFunctionName(functionExecution.getName());
        return dataRepository.insertExecutionLog(log);
    }
    public long insertExecutionLog(Throwable cause){
        if(functionExecution == null)
            return -1;

        ExecutionLogEntity log = new ExecutionLogEntity();
        log.setExecutionId(functionExecution.getId());
        log.setLog(cause.getMessage() + " " + cause.getStackTrace().toString());
        log.setDate(DateConverter.toDate(System.currentTimeMillis()));
        log.setFunctionName(functionExecution.getName());
        return dataRepository.insertExecutionLog(log);
    }
    public long startFunctionExecution(){
        if(functionExecution == null)
            return -1;

        functionExecution.setCallState(FunctionCallStateEnum.EXECUTING.getId());
        functionExecution.setStartDate(DateConverter.toDate(System.currentTimeMillis()));
        functionExecution.setResultState(FunctionResultStateEnum.NA.getId());
        long executionId = dataRepository.insertFunctionExecution(functionExecution);
        functionExecution.setId(executionId);

        // Automatically insert log as well
        insertExecutionLog("Starting execution ... ");

        return executionId;
    }
    public FunctionExecutionEntity updateFunctionExecution(FunctionCallStateEnum callState) {
        if(functionExecution == null)
            return null;

        functionExecution.setCallState(callState.getId());

        if (callState.equals(FunctionCallStateEnum.READY)) {
            functionExecution.setEndDate(DateConverter.toDate(System.currentTimeMillis()));
            insertExecutionLog("Execution completed");
        }
        if (callState.equals(FunctionCallStateEnum.ERROR)) {
            functionExecution.setEndDate(DateConverter.toDate(System.currentTimeMillis()));
            insertExecutionLog("Execution failed");
        }
        dataRepository.updateFunctionExecution(functionExecution);

        return  functionExecution;
    }
    public FunctionExecutionEntity updateFunctionExecution(FunctionResultStateEnum resultState) {
        if(functionExecution == null)
            return null;

        //if(deviceStateInfo.getFunctionState() != FunctionResultStateEnum.NA)
        functionExecution.setResultState(resultState.getId());

        dataRepository.updateFunctionExecution(functionExecution);

        return  functionExecution;
    }
}
