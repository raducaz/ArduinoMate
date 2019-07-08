package com.example.amqtest;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.AMQConnection;

import static com.example.amqtest.MyApplication.AmqConnection;
import static com.example.amqtest.MyApplication.EXCHANGE_NAME;

public class RemoteStateSender {

    Channel channel;
    public RemoteStateSender()
    {
        try {
            channel = AmqConnection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        }catch (Exception exc){}
    }

    public void SendState(String stateMessage){
        try {
            channel.basicPublish(EXCHANGE_NAME, "", null, stateMessage.getBytes("UTF-8"));
        }
        catch (Exception exc){
            Log.e("RemoteStateSender", exc.getMessage());
        }
        finally {
//            try{
//                channel.close();
//                connection.close();
//            }
//            catch (Exception exc)
//            {
//                Log.e("RemoteStateSender", exc.getMessage());
//            }
        }
    }

}
