package com.example.amqtest;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RemoteStateSender {

    private static final String EXCHANGE_NAME = "states";
    String uri = "amqp://lttjuzsi:pjSXi8zN4wT8Pljaq14lIEAVWpQddzxS@bulldog.rmq.cloudamqp.com/lttjuzsi";

    public void SendState(String stateMessage){

        Connection connection = null;
        Channel channel = null;

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(uri);

            //Recommended settings
            factory.setRequestedHeartbeat(30);
            factory.setConnectionTimeout(30000);


            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            channel.basicPublish(EXCHANGE_NAME, "", null, stateMessage.getBytes("UTF-8"));
        }
        catch (Exception exc){
            Log.e("RemoteStateSender", exc.getMessage());
        }
        finally {
            try{
                channel.close();
                connection.close();
            }
            catch (Exception exc)
            {
                Log.e("RemoteStateSender", exc.getMessage());
            }
        }
    }

}
