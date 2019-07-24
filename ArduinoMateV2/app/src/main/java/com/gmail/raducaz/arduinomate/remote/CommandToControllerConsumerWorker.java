package com.gmail.raducaz.arduinomate.remote;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gmail.raducaz.arduinomate.AppExecutors;
import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.AppDatabase;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionCaller;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionReset;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionSync;
import com.gmail.raducaz.arduinomate.ui.TaskExecutor;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandToControllerConsumerWorker extends Worker {

    private String TAG = "CommandConsumerServiceWork";

    private DataRepository mRepository;

    public CommandToControllerConsumerWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);

        AppExecutors executors = new AppExecutors();
        mRepository = DataRepository.getInstance(AppDatabase.getInstance(appContext, executors));
    }

    @NonNull
    @Override
    public Result doWork() {

        Connection connection;
        String queueName = getInputData().getString("queue");

        try {
//            ConnectionFactory factory = new ConnectionFactory();
//            factory.setUri(uri);
//            //Recommended settings
//            factory.setRequestedHeartbeat(30);
//            factory.setConnectionTimeout(10000);

            connection = ArduinoMateApp.AmqConnection;//factory.newConnection();

            final Channel channel = connection.createChannel();
            channel.basicQos(1); // process one at a time

            channel.queueDeclare(queueName, false, false, false, null);

            boolean autoAck = false;
            channel.basicConsume(queueName, autoAck,
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
                                Object bodyObject = SerializerDeserializerUtility.Deserialize(body);

                                if (bodyObject instanceof RemoteFunctionCommand) {
                                    RemoteFunctionCommand command = (RemoteFunctionCommand) bodyObject;
                                    TaskFunctionCaller functionCaller = new TaskFunctionCaller(
                                            mRepository,
                                            command.deviceName,
                                            command.functionName,
                                            command.desiredFunctionState,
                                            "Remote on demand");
                                    functionCaller.setAutoExecution(false);
                                    functionCaller.setOnDemand(true);
                                    new TaskExecutor().execute(functionCaller);
                                }
                                if (bodyObject instanceof RemoteResetCommand) {
                                    RemoteResetCommand command = (RemoteResetCommand) bodyObject;
                                    TaskFunctionReset functionReset = new TaskFunctionReset(
                                            mRepository,
                                            command.function != null ? command.function.getId() : 0,
                                            command.alsoRestart);

                                    new TaskExecutor().execute(functionReset);
                                }
                                if (bodyObject instanceof RemoteSyncCommand) {
                                    RemoteSyncCommand command = (RemoteSyncCommand) bodyObject;
                                    TaskFunctionSync functionSync = new TaskFunctionSync(
                                            mRepository,
                                            command.function != null ? command.function.getId() : 0);

                                    new TaskExecutor().execute(functionSync);
                                }
                                if (bodyObject instanceof RemoteStateConsumerQueue) {
                                    RemoteStateConsumerQueue command = (RemoteStateConsumerQueue) bodyObject;
                                    mRepository.insertRemoteQueue(command.queueName);
                                }
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

            return Result.success();

        }
        catch (Exception exc)
        {
            Log.e(TAG, exc.getMessage());
            return Result.failure();
        }
        finally {
        }
    }
}

