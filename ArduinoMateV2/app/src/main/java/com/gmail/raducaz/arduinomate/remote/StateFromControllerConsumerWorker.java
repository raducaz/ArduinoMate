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
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.gmail.raducaz.arduinomate.model.ExecutionLog;
import com.gmail.raducaz.arduinomate.service.DeviceStateUpdater;
import com.orhanobut.logger.Logger;
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

public class StateFromControllerConsumerWorker extends Worker {

    private String TAG = "FromControllerStateConsumerWorker";

    private DataRepository mRepository;

    public StateFromControllerConsumerWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);

        AppExecutors executors = new AppExecutors();
        mRepository = DataRepository.getInstance(AppDatabase.getInstance(appContext, executors));
    }

    @NonNull
    @Override
    public Result doWork() {

        Connection connection = ArduinoMateApp.AmqConnection;
        String exchangeName = getInputData().getString("exchangeName");

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
                            Log.d(TAG, message);

                            try {
                                Serializable stateUpdate = (Serializable) SerializerDeserializerUtility.Deserialize(body);
                                ProcessState(stateUpdate);

                            } catch (Exception exc) {
                                Logger.e(TAG+ exc.getMessage());
                            } finally {
                                // positively acknowledge a single delivery, the message will
                                // be discarded
                                channel.basicAck(deliveryTag, false);
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

            return Result.success();
        } catch (Exception exc) {
            Logger.e(TAG+ exc.getMessage());

            return Result.failure();
        } finally {
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
            if (stateUpdate.methodName.equals("deleteExecutionLogsToDate")) {
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
            if (stateUpdate.methodName.equals("deletePinStatesToDate")) {
                PinStateEntity pinState = (PinStateEntity) stateUpdate.entity;
                mRepository.deletePinStatesToDate(pinState.getToDate());
            }
        }
        if (input instanceof RemotePinStateUpdate) {
            RemotePinStateUpdate stateUpdate = (RemotePinStateUpdate) input;
            String[] states = stateUpdate.pinStates.split("\r\n");
            for (String state : states) {
                try {
                    DeviceStateUpdater deviceStateUpdater = new DeviceStateUpdater(mRepository, state);
                    deviceStateUpdater.updatePinStates();
                } catch (Exception exc) {
                    Logger.e("ProcessPinStates"+ exc.getMessage());
                }
            }
        }

    }

}

