package com.example.amqtest;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.amqtest.MyApplication.EXCHANGE_NAME;

public class ConsumerService2 implements Runnable {

    private static final String EXCHANGE_NAME = "states";
    String uri = "amqp://lttjuzsi:pjSXi8zN4wT8Pljaq14lIEAVWpQddzxS@bulldog.rmq.cloudamqp.com/lttjuzsi";

    private String TAG = "ConsumerService";

    private static ConsumerService2 sInstance;
    private boolean isRunning;
    private final ExecutorService pool;

    private ConsumerService2() {

        // Initialize a dynamic pool that starts the required no of threads according to the no of tasks submitted
        pool = Executors.newFixedThreadPool(1);
    }
    public static ConsumerService2 getInstance() throws IOException {
        if (sInstance == null) {
            synchronized (ConsumerServiceHandler.class) {
                if (sInstance == null) {
                    sInstance = new ConsumerService2();
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
                connection.addShutdownListener(
                        new ShutdownListener() {
                            @Override
                            public void shutdownCompleted(ShutdownSignalException cause) {
                                if(cause.isHardError()) {
                                    Connection conn = (Connection) cause.getReference();
                                    if (!cause.isInitiatedByApplication()) {
                                        Method reason = cause.getReason();
                                    }
                                }
                                else
                                {
                                    Channel ch = (Channel)cause.getReference();
                                }
                            }
                        }
                );


                final Channel channel = connection.createChannel();

                channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                String queueName = channel.queueDeclare().getQueue();
//                String queueName = channel.queueDeclare(EXCHANGE_NAME, false, false, false, null).getQueue();
                channel.queueBind(queueName, EXCHANGE_NAME, "");

                boolean autoAck = false;
                channel.basicConsume(queueName, autoAck, "a-consumer-tag",
                        new DefaultConsumer(channel) {
                            @Override
                            public void handleDelivery(String consumerTag,
                                                       Envelope envelope,
                                                       AMQP.BasicProperties properties,
                                                       byte[] body)
                                    throws IOException
                            {
                                long deliveryTag = envelope.getDeliveryTag();

                                String message = new String(body);
                                Log.i("ConsumerService2", message);

                                // positively acknowledge a single delivery, the message will
                                // be discarded
                                channel.basicAck(deliveryTag, false);
                            }

                        }

                        );


//                QueueingConsumer consumer = new QueueingConsumer(channel);
//                channel.basicConsume(queue, true, consumer);
//                while (true) {
//                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
//                    String message = new String(delivery.getBody());
//                    Log.i("ConsumerService", message);
//                }

            }
            catch (Exception exc)
            {
                Log.e("ConsumerService2", exc.getMessage());
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

