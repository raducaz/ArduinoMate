package com.gmail.raducaz.arduinomate.remote;

import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.io.Serializable;

public class RemoteResetCommand implements Serializable {
    FunctionEntity function;
    boolean alsoRestart;

    public RemoteResetCommand(FunctionEntity function, boolean alsoRestart){
        this.function = function;
        this.alsoRestart = alsoRestart;
    }
}
