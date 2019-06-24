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
        Channel channel = null;
        try {
            channel = connection.createChannel();
            if(channel == null)
                throw new Exception("Channel could not be created.");

            channel.queueDeclare(queueName,false,false,false,null);

            byte[] cmdMessage = SerializerDeserializerUtility.Serialize(command);
            channel.basicPublish("", queueName, null, cmdMessage);

        } catch (Exception exc) {
            Log.e("CommandToController", exc.getMessage());
        }
        finally {
            if(channel!= null)
                try{ channel.close(); } catch (Exception exc){
                    Log.e("CommandToController", "Channel cannot close", exc);
                }
        }

        return true;
    }
}
