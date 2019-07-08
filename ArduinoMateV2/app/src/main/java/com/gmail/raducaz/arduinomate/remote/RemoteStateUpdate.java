package com.gmail.raducaz.arduinomate.remote;

import java.io.Serializable;

public class RemoteStateUpdate implements Serializable {
    Serializable entity;
    String methodName;

    public RemoteStateUpdate(Serializable entity, String methodName)
    {
        this.entity = entity;
        this.methodName = methodName;
    }
}
