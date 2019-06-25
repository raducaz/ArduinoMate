package com.gmail.raducaz.arduinomate.remote;

import java.io.Serializable;

public class RemotePinStateUpdate implements Serializable {
    String pinStates;

    public RemotePinStateUpdate(String pinStates)
    {
        this.pinStates = pinStates;
    }
}
