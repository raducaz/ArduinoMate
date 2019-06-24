package com.gmail.raducaz.arduinomate.remote;
import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StateFromControllerConsumerService implements Runnable {

    private String TAG = "ControllerStateConsumerService";

    private Connection connection;
    private String exchangeName;
    private DataRepository mRepository;

    private static StateFromControllerConsumerService sInstance;
    private boolean isRunning;
    private final ExecutorService pool;

    private StateFromControllerConsumerService(DataRepository repository, Connection amqConnection, String exchangeName) {

        // Initialize a dynamic pool that starts the required no of threads according to the no of tasks submitted
        pool = Executors.newFixedThreadPool(1);

        this.mRepository = repository;
        this.connection = amqConnection;
        this.exchangeName = exchangeName;
    }
    public static StateFromControllerConsumerService getInstance(DataRepository repository, Connection amqConnection, String exchangeName) throws IOException {
        if (sInstance == null) {
            synchronized (ControllerStateConsumerServiceHandler.class) {
                if (sInstance == null) {
                    sInstance = new StateFromControllerConsumerService(repository, amqConnection, exchangeName);
                }
            }
        }
        return sInstance;
    }

    public void run() {

        if(!isRunning) {
            pool.execute(new ControllerStateConsumerServiceHandler(connection, exchangeName));
            isRunning = true;
        }
    }

    public class ControllerStateConsumerServiceHandler implements Runnable {

        private Connection connection;
        private String exchangeName;
        public ControllerStateConsumerServiceHandler(Connection amqConnection, String exchangeName)
        {
            this.connection = amqConnection;
            this.exchangeName = exchangeName;
        }

        public void run() {

            try {

                final Channel channel = connection.createChannel();

                channel.exchangeDeclare(exchangeName, "fanout");
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, exchangeName, "");

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
                                // Don;t process redelivered messages
                                if(envelope.isRedeliver()) {
                                    channel.basicAck(deliveryTag, false);
                                    return;
                                }

                                String message = new String(body);
                                Log.i(TAG, message);

                                try {
                                    RemoteStateUpdate stateUpdate = (RemoteStateUpdate) SerializerDeserializerUtility.Deserialize(body);
                                    ProcessState(stateUpdate);

                                    // positively acknowledge a single delivery, the message will
                                    // be discarded
                                    channel.basicAck(deliveryTag, false);
                                }
                                catch (Exception exc)
                                {
                                    Log.e(TAG, "", exc);
                                }
                            }

                        }
                );

            }
            catch (Exception exc)
            {
                Log.e(TAG, exc.getMessage());
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

        public void ProcessState(RemoteStateUpdate stateUpdate)
        {
            if(stateUpdate.methodName == "updatePinStateLastUpdate")
            {
                PinStateEntity pinState = (PinStateEntity) stateUpdate.entity;
                mRepository.updatePinStateLastUpdate(pinState.getId());
            }
            if(stateUpdate.methodName == "updatePinStateToDate")
            {
                PinStateEntity pinState = (PinStateEntity) stateUpdate.entity;
                mRepository.updatePinStateToDate(pinState.getId());
            }
            if(stateUpdate.methodName == "insertPinState")
            {
                PinStateEntity pinState = (PinStateEntity) stateUpdate.entity;
                mRepository.insertPinState(pinState);
            }


        }
    }
}

