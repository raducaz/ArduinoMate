package com.gmail.raducaz.arduinomate.remote;
import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.gmail.raducaz.arduinomate.model.ExecutionLog;
import com.gmail.raducaz.arduinomate.service.DeviceStateUpdater;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.gmail.raducaz.arduinomate.ArduinoMateApp.AmqConnection;
import static com.gmail.raducaz.arduinomate.ArduinoMateApp.COMMAND_QUEUE;

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
                channel.basicQos(1); // process one at a time

                channel.exchangeDeclare(exchangeName, "fanout");
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, exchangeName, "");

                boolean autoAck = false;
                channel.basicConsume(queueName, autoAck,
                        new DefaultConsumer(channel) {
                            @Override
                            public void handleDelivery(String consumerTag,
                                                       Envelope envelope,
                                                       AMQP.BasicProperties properties,
                                                       byte[] body)
                                    throws IOException {
                                long deliveryTag = envelope.getDeliveryTag();
                                // Don;t process redelivered messages
                                if (envelope.isRedeliver()) {
                                    channel.basicAck(deliveryTag, false);
                                    return;
                                }

                                String message = new String(body);
                                Log.i(TAG, message);

                                try {
                                    Serializable stateUpdate = (Serializable) SerializerDeserializerUtility.Deserialize(body);
                                    ProcessState(stateUpdate);

                                    // positively acknowledge a single delivery, the message will
                                    // be discarded
                                    channel.basicAck(deliveryTag, false);
                                } catch (Exception exc) {
                                    Log.e(TAG, "", exc);
                                }
                            }

                        }
                );

                // Send the consumer queue name to Controller
                if (!mRepository.getSettingsSync().getIsController()) {
                    CommandToControllerPublisher sender = new CommandToControllerPublisher(AmqConnection,
                            COMMAND_QUEUE);
                    sender.SendCommand(new RemoteStateConsumerQueue(queueName), 3600);

                }
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

        public void ProcessState(Serializable input) {

            if (input instanceof RemoteStateUpdate) {
                RemoteStateUpdate stateUpdate = (RemoteStateUpdate) input;

                if (stateUpdate.methodName.equals("insertDevice")) {
                    DeviceEntity entity = (DeviceEntity) stateUpdate.entity;
                    mRepository.insertDevice(entity);
                }
                if (stateUpdate.methodName.equals("updateDevice")) {
                    DeviceEntity entity = (DeviceEntity) stateUpdate.entity;
                    mRepository.updateDevice(entity);
                }
                if (stateUpdate.methodName.equals("insertFunction")) {
                    FunctionEntity entity = (FunctionEntity) stateUpdate.entity;
                    mRepository.insertFunction(entity);
                }
                if (stateUpdate.methodName.equals("updateFunction")) {
                    FunctionEntity entity = (FunctionEntity) stateUpdate.entity;
                    mRepository.updateFunction(entity);
                }
                if (stateUpdate.methodName.equals("updateDeviceFunctionsStates")) {
                    FunctionEntity entity = (FunctionEntity) stateUpdate.entity;
                    mRepository.updateDeviceFunctionsStates(entity.getDeviceId(), entity.getCallState(), entity.getResultState());
                }
                if (stateUpdate.methodName.equals("updateAllFunctionStates")) {
                    FunctionEntity entity = (FunctionEntity) stateUpdate.entity;
                    mRepository.updateAllFunctionStates(entity.getCallState(), entity.getResultState());
                }
                if (stateUpdate.methodName.equals("deleteFunctionExecutions")) {
                    FunctionEntity entity = (FunctionEntity) stateUpdate.entity;
                    mRepository.deleteFunctionExecutions(entity.getId());
                }
                if (stateUpdate.methodName.equals("deleteAllFunctionExecutions")) {
                    mRepository.deleteAllFunctionExecutions();
                }
                if (stateUpdate.methodName.equals("insertFunctionExecution")) {
                    FunctionExecutionEntity entity = (FunctionExecutionEntity) stateUpdate.entity;
                    mRepository.insertFunctionExecution(entity);
                }
                if (stateUpdate.methodName.equals("updateFunctionExecution")) {
                    FunctionExecutionEntity entity = (FunctionExecutionEntity) stateUpdate.entity;
                    mRepository.updateFunctionExecution(entity);
                }
                if (stateUpdate.methodName.equals("insertExecutionLog")) {
                    ExecutionLogEntity entity = (ExecutionLogEntity) stateUpdate.entity;
                    mRepository.insertExecutionLog(entity);
                }
                if (stateUpdate.methodName.equals("deleteExecutionLogs")) {
                    FunctionEntity entity = (FunctionEntity) stateUpdate.entity;
                    mRepository.deleteExecutionLogs(entity.getId());
                }
                if (stateUpdate.methodName.equals("deleteAllExecutionLogs")) {
                    mRepository.deleteAllExecutionLogs();
                }
                if(stateUpdate.methodName.equals("deleteExecutionLogsToDate"))
                {
                    ExecutionLog entity = (ExecutionLog) stateUpdate.entity;
                    mRepository.deleteExecutionLogsToDate(entity.getDate());
                }
                if (stateUpdate.methodName.equals("insertPinState")) {
                    PinStateEntity pinState = (PinStateEntity) stateUpdate.entity;
                    mRepository.insertPinState(pinState);
                }
                if (stateUpdate.methodName.equals("updatePinStateToDate")) {
                    PinStateEntity pinState = (PinStateEntity) stateUpdate.entity;
                    mRepository.updatePinStateToDate(pinState.getId());
                }
                if (stateUpdate.methodName.equals("updatePinStateLastUpdate")) {
                    PinStateEntity pinState = (PinStateEntity) stateUpdate.entity;
                    mRepository.updatePinStateLastUpdate(pinState.getId());
                }
                if (stateUpdate.methodName.equals("deletePinStatesByFunction")) {
                    FunctionEntity entity = (FunctionEntity) stateUpdate.entity;
                    mRepository.deletePinStatesByFunction(entity.getId());
                }
                if (stateUpdate.methodName.equals("deleteAllPinStates")) {
                    mRepository.deleteAllPinStates();
                }
                if(stateUpdate.methodName.equals("deletePinStatesToDate"))
                {
                    PinStateEntity pinState = (PinStateEntity) stateUpdate.entity;
                    mRepository.deletePinStatesToDate(pinState.getToDate());
                }
            }
            if (input instanceof RemotePinStateUpdate)
            {
                RemotePinStateUpdate stateUpdate = (RemotePinStateUpdate) input;
                String[] states = stateUpdate.pinStates.split("\r\n");
                for(String state : states)
                {
                    try {
                        DeviceStateUpdater deviceStateUpdater = new DeviceStateUpdater(mRepository, state);
                        deviceStateUpdater.updatePinStates();
                    }
                    catch (Exception exc)
                    {
                        Log.e("ProcessPinStates", "", exc);
                    }
                }
            }

        }
    }
}

