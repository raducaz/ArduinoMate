package com.example.amqtest;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerService implements Runnable {

    private static final String EXCHANGE_NAME = "states";
    String uri = "amqp://lttjuzsi:pjSXi8zN4wT8Pljaq14lIEAVWpQddzxS@bulldog.rmq.cloudamqp.com/lttjuzsi";

    private String TAG = "ConsumerService";

    private static ConsumerService sInstance;
    private boolean isRunning;
    private final ExecutorService pool;

    private ConsumerService() {

        // Initialize a dynamic pool that starts the required no of threads according to the no of tasks submitted
        pool = Executors.newFixedThreadPool(1);
    }
    public static ConsumerService getInstance() throws IOException {
        if (sInstance == null) {
            synchronized (ConsumerServiceHandler.class) {
                if (sInstance == null) {
                    sInstance = new ConsumerService();
                }
            }
        }
        return sInstance;
    }

    public void run() {

        if(!isRunning) {
            pool.execute(new ConsumerServiceHandler());
            isRunning = true;
        }
    }

    public class ConsumerServiceHandler implements Runnable {

        public void run() {

            Connection connection = null;
            try {
                String EXCHANGE_NAME = "states";

                ConnectionFactory factory = new ConnectionFactory();
                factory.setUri(uri);

                //Recommended settings
                factory.setRequestedHeartbeat(30);
                factory.setConnectionTimeout(30000);

                connection = factory.newConnection();
                final Channel channel = connection.createChannel();

                channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, EXCHANGE_NAME, "");

//                boolean autoAck = false;
//                channel.basicConsume(queueName, autoAck, "a-consumer-tag",
//                        new DefaultConsumer(channel) {
//                            @Override
//                            public void handleDelivery(String consumerTag,
//                                                       Envelope envelope,
//                                                       AMQP.BasicProperties properties,
//                                                       byte[] body)
//                                    throws IOException
//                            {
//                                long deliveryTag = envelope.getDeliveryTag();
//
//                                String message = new String(body);
//                                Log.i("ConsumerService", message);
//
//                                // positively acknowledge a single delivery, the message will
//                                // be discarded
//                                channel.basicAck(deliveryTag, false);
//                            }
//
//                        }
//
//                        );


//                QueueingConsumer consumer = new QueueingConsumer(channel);
//                channel.basicConsume(queueName, false, consumer);
//                while (true) {
//                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
//                    long deliveryTag = delivery.getEnvelope().getDeliveryTag();
//
//                    String message = new String(delivery.getBody());
//                    Log.i("ConsumerService", message);
//
//                    // positively acknowledge a single delivery, the message will
//                    // be discarded
//                    channel.basicAck(deliveryTag, false);
//                }

            }
            catch (Exception exc)
            {
                Log.e("ConsumerService", exc.getMessage());
            }
            finally {
//                try
//                {
//                    connection.close();
//                    Log.i("ConsumerService", "Connection closed");
//                }
//                catch (Exception exc){Log.e("",exc.getMessage());}
            }

        }
    }
}

