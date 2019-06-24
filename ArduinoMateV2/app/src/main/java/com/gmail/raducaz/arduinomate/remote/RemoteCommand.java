package com.gmail.raducaz.arduinomate.remote;

import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.io.Serializable;

public class RemoteCommand implements Serializable {
    String deviceName;
    String functionName;
    FunctionResultStateEnum desiredFunctionState;

    public RemoteCommand(String deviceName, String functionName, FunctionResultStateEnum desiredFunctionState){
        this.deviceName = deviceName;
        this.functionName = functionName;
        this.desiredFunctionState = desiredFunctionState;
    }
}
