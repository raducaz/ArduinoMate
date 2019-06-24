package com.gmail.raducaz.arduinomate.remote;
import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionCaller;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.gmail.raducaz.arduinomate.ui.TaskExecutor;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandToControllerConsumerService implements Runnable {

    private String TAG = "CommandToControllerConsumerService";

    private Connection connection;
    private String exchangeName;
    private DataRepository mRepository;

    private static CommandToControllerConsumerService sInstance;
    private boolean isRunning;
    private final ExecutorService pool;

    private CommandToControllerConsumerService(Connection amqConnection, String exchangeName, DataRepository repository) {

        // Initialize a dynamic pool that starts the required no of threads according to the no of tasks submitted
        pool = Executors.newFixedThreadPool(1);

        this.connection = amqConnection;
        this.exchangeName = exchangeName;
        this.mRepository = repository;
    }
    public static CommandToControllerConsumerService getInstance(Connection amqConnection, String exchangeName, DataRepository repository) throws IOException {
        if (sInstance == null) {
            synchronized (CommandToControllerConsumerServiceHandler.class) {
                if (sInstance == null) {
                    sInstance = new CommandToControllerConsumerService(amqConnection, exchangeName, repository);
                }
            }
        }
        return sInstance;
    }

    public void run() {

        if(!isRunning) {
            pool.execute(new CommandToControllerConsumerServiceHandler(connection, exchangeName, mRepository));
            isRunning = true;
        }
    }

    public class CommandToControllerConsumerServiceHandler implements Runnable {

        private Connection connection;
        private String exchangeName;
        private DataRepository mRepository;

        public CommandToControllerConsumerServiceHandler(Connection amqConnection, String exchangeName, DataRepository repository)
        {
            this.connection = amqConnection;
            this.exchangeName = exchangeName;
            this.mRepository = repository;
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
                                    RemoteCommand command = (RemoteCommand) SerializerDeserializerUtility.Deserialize(body);

                                    TaskFunctionCaller functionCaller = new TaskFunctionCaller(
                                            mRepository,
                                            command.deviceName,
                                            command.functionName,
                                            command.desiredFunctionState,
                                            "Remote on demand");
                                    new TaskExecutor().execute(functionCaller);
                                }
                                catch (Exception exc) {
                                    Log.e(TAG, exc.getMessage());
                                }

                                // positively acknowledge a single delivery, the message will
                                // be discarded
                                channel.basicAck(deliveryTag, false);
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
    }
}

