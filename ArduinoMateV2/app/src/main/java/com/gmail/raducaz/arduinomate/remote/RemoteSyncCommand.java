package com.gmail.raducaz.arduinomate.remote;

import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;

import java.io.Serializable;

public class RemoteSyncCommand implements Serializable {
    FunctionEntity function;

    public RemoteSyncCommand(FunctionEntity function){
        this.function = function;
    }
}
