package com.gmail.raducaz.arduinomate.remote;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class StateFromControllerPublisher {


    public static boolean SendState(Connection connection, String exchangeName, RemoteStateUpdate stateUpdate) {
        try {
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchangeName, "fanout");

            byte[] cmdMessage = SerializerDeserializerUtility.Serialize(stateUpdate);
            channel.basicPublish(exchangeName, "", null, cmdMessage);

        } catch (Exception exc) {
            Log.e("StateFromController", exc.getMessage());
        }

        return true;
    }
}
