package com.gmail.raducaz.arduinomate.remote;

import java.io.Serializable;

public class RemoteStateConsumerQueue implements Serializable {
    String queueName;

    public RemoteStateConsumerQueue(String queueName)
    {
        this.queueName = queueName;
    }
}
