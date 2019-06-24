package com.gmail.raducaz.arduinomate.remote;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class StateFromControllerPublisher {


    public static boolean SendState(Connection connection, String exchangeName, RemoteStateUpdate stateUpdate) {
        Channel channel = null;
        try {

            channel = connection.createChannel();
            if(channel == null)
                throw new Exception("Channel could not be created.");

            channel.exchangeDeclare(exchangeName, "fanout");

            byte[] cmdMessage = SerializerDeserializerUtility.Serialize(stateUpdate);
            channel.basicPublish(exchangeName, "", null, cmdMessage);

        } catch (Exception exc) {
            Log.e("StateFromController", exc.getMessage());
        }
        finally {
            if(channel!= null)
                try{ channel.close(); } catch (Exception exc){
                    Log.e("StateFromController", "Channel cannot close", exc);
                }
        }

        return true;
    }
}
