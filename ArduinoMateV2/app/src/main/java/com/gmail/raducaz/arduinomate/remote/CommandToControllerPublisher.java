package com.gmail.raducaz.arduinomate.remote;

import android.util.Log;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.Serializable;

public class CommandToControllerPublisher {

    private Connection connection;
    private String queueName;
    public CommandToControllerPublisher(Connection  connection, String queueName)
    {
        this.connection = connection;
        this.queueName = queueName;
    }

    public boolean SendCommand(byte[] cmdMessage, int secondsMessageValidity) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            if(channel == null)
                throw new Exception("Channel could not be created.");

            channel.queueDeclare(queueName,false,false,false,null);

            AMQP.BasicProperties prop = null;
            if(secondsMessageValidity>0) {
                String expiration = Integer.toString(secondsMessageValidity * 1000);
                prop = new AMQP.BasicProperties.Builder()
                        .expiration(expiration)
                        .build();
            }
            channel.basicPublish("", queueName, prop, cmdMessage);

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

    public boolean SendCommand(Serializable command, int secondsMessageValidity) {
        try {

//            //Send the state receive queue Name so that controller knows about me (the client)
//            // Send the consumer queue name to Controller
//            byte[] cmdMessage = SerializerDeserializerUtility.Serialize(new RemoteStateConsumerQueue(ArduinoMateApp.STATES_QUEUE));
//            SendCommand(cmdMessage, secondsMessageValidity);

            byte[] cmdMessage = SerializerDeserializerUtility.Serialize(command);
            SendCommand(cmdMessage, secondsMessageValidity);
        }
        catch (Exception exc)
        {
            Log.e("CommandToController", exc.getMessage());
        }

        return true;
    }
    public boolean SendCommand(String command, int secondsMessageValidity) {
        try {
            byte[] cmdMessage = command.getBytes("UTF-8");
            SendCommand(cmdMessage, secondsMessageValidity);
        }
        catch (Exception exc)
        {
            Log.e("CommandToController", exc.getMessage());
        }

        return true;
    }
}
