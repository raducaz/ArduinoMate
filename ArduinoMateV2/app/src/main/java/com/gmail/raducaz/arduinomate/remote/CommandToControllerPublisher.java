package com.gmail.raducaz.arduinomate.remote;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class CommandToControllerPublisher {

    private Connection connection;
    private String queueName;
    public CommandToControllerPublisher(Connection  connection, String queueName)
    {
        this.connection = connection;
        this.queueName = queueName;
    }

    public boolean SendCommand(RemoteCommand command) {
        try {
            Channel channel = connection.createChannel();
            channel.queueDeclare(queueName,false,false,false,null);

            byte[] cmdMessage = SerializerDeserializerUtility.Serialize(command);
            channel.basicPublish("", queueName, null, cmdMessage);

        } catch (Exception exc) {
            Log.e("StateFromController", exc.getMessage());
        }

        return true;
    }
}
