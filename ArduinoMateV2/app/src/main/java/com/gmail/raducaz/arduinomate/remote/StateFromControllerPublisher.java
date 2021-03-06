package com.gmail.raducaz.arduinomate.remote;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.RemoteQueueEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.Serializable;
import java.util.List;

public class StateFromControllerPublisher {

    public static boolean HasConsumers(DataRepository repository, Channel channel)
    {
        List<RemoteQueueEntity> consumerQueues = repository.getRemoteQueuesSync();
        for (RemoteQueueEntity queue : consumerQueues)
        {
            try {
                // This will throw RESOURCE_LOCKED - cannot obtain exclusive access to locked queue if queue is exclusive declared
                AMQP.Queue.DeclareOk result = channel.queueDeclarePassive(queue.getName());
                if(result.getConsumerCount()>0)
                    return true;
            }
            catch (Exception exc)
            {
                Log.e("HasConsumers", "Cannot declare passive queue" + queue.getName(), exc);

                String cause = exc.getCause().getMessage();
                if(cause.contains("NOT_FOUND - no queue"))
                {
                    repository.deleteRemoteQueue(queue.getName());
                }
                if(cause.contains("RESOURCE_LOCKED - cannot obtain exclusive access to locked queue"))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean SendState(DataRepository repository, Connection connection, String exchangeName, Serializable stateUpdate) {

        Channel channel = null;
        try {

            channel = connection.createChannel();
            if(!HasConsumers(repository, channel))
                throw  new Exception("No consumers, publish aborted");
            else
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
