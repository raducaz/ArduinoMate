package com.gmail.raducaz.arduinomate.remote;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.RemoteQueueEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.util.List;

public class StateFromControllerPublisher {

    public static boolean HasConsumers(DataRepository repository, Channel channel)
    {
        List<RemoteQueueEntity> consumerQueues = repository.getRemoteQueuesSync();
        for (RemoteQueueEntity queue : consumerQueues)
        {
            try {
                AMQP.Queue.DeclareOk result = channel.queueDeclarePassive(queue.getName());
                if(result.getConsumerCount()>0)
                    return true;
            }
            catch (Exception exc)
            {
                Log.e("HasConsumers", "Cannot declare passive queue" + queue.getName(), exc);
            }
        }

        return false;
    }

    public static boolean SendState(DataRepository repository, Connection connection, String exchangeName, RemoteStateUpdate stateUpdate) {

        Channel channel = null;
        try {

            channel = connection.createChannel();
            if(!HasConsumers(repository, channel))
                throw  new Exception("No consumers, publish aborted");

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
