package com.gmail.raducaz.arduinomate.remote;

import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.io.Serializable;

public class RemoteResetCommand implements Serializable {
    FunctionEntity function;

    public RemoteResetCommand(FunctionEntity function){
        this.function = function;
    }
}
