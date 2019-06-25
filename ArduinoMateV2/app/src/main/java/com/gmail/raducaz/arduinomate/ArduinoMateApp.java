package com.gmail.raducaz.arduinomate;

import android.app.Application;
import android.util.Log;

import com.gmail.raducaz.arduinomate.db.AppDatabase;
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;
import com.gmail.raducaz.arduinomate.remote.CommandToControllerConsumerService;
import com.gmail.raducaz.arduinomate.remote.StateFromControllerConsumerService;
import com.gmail.raducaz.arduinomate.mocks.MockArduinoServerService;
import com.gmail.raducaz.arduinomate.tcpserver.TcpServerService;
import com.gmail.raducaz.arduinomate.timer.TimerService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

public class ArduinoMateApp extends Application {
    private String TAG = "ArduinoMateApp";

    private SettingsEntity settings;

    public static final String STATES_EXCHANGE = "states";
    public static final String COMMAND_QUEUE = "commands";
    public static Connection AmqConnection;
    String uri = "amqp://lttjuzsi:pjSXi8zN4wT8Pljaq14lIEAVWpQddzxS@bulldog.rmq.cloudamqp.com/lttjuzsi";

    private AppExecutors mAppExecutors;
    private TimerService timerService;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppExecutors = new AppExecutors();

        try {
            ExecutorService executor = mAppExecutors.networkIO();
            Callable<SettingsEntity> callable = new Callable<SettingsEntity>() {
                @Override
                public SettingsEntity call() {
                    return getRepository().getSettingsSync();
                }
            };
            Future<SettingsEntity> future = executor.submit(callable);

            //TODO: Ensure this is completed synchronous before moving on
            settings = future.get();
//            executor.shutdown();
//            try {
//                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
//                    executor.shutdownNow();
//                }
//            } catch (InterruptedException ex) {
//                executor.shutdownNow();
//                Thread.currentThread().interrupt();
//            }
        }
        catch (Exception exc) {
            Log.e(TAG, exc.getMessage());
        }

        try {

            ExecutorService executor = mAppExecutors.networkIO();
            Callable<Connection> callable = new Callable<Connection>() {
                @Override
                public Connection call() throws Exception{
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setUri(uri);
                    //Recommended settings
                    factory.setRequestedHeartbeat(30);
                    factory.setConnectionTimeout(30000);

                    return factory.newConnection();
                }
            };
            Future<Connection> future = executor.submit(callable);
            AmqConnection = future.get();

            AmqConnection.addShutdownListener(
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

                            Log.e("InitAMQConnection", "", cause);
                        }
                    }
            );
        }
        catch (Exception exc)
        {
            Log.e("InitAMQConnection", "", exc);
        }
//        // Start the tcp server or AMQ state consumer and Mocks service - More complicated way
//        TaskServerServiceInitializer serviceInitializer = new TaskServerServiceInitializer(this);
//        new TaskExecutor().execute();


        if(!settings.getIsController()) {

            // Start AMQ state consumer
            try
            {
                StateFromControllerConsumerService consumerService = StateFromControllerConsumerService.getInstance(getRepository(),
                        AmqConnection, STATES_EXCHANGE);
                this.getNetworkExecutor().execute(consumerService);
            }
            catch (Exception exc)
            {
                Log.e("StartStateConsumer", "", exc);
            }
        }
        else
        {
            // Start tcp service server
            try {
                TcpServerService tcpServerService = TcpServerService.getInstance(getRepository());
                this.getNetworkExecutor().execute(tcpServerService);
            }
            catch (IOException exc)
            {
                Log.e("StartTcpServer", "", exc);
            }

            // If controller that permits remote control
            if(settings.getPermitRemoteControl()) {

                // Start AMQ remote command consumer
                try {
                    CommandToControllerConsumerService consumerService = CommandToControllerConsumerService.getInstance(AmqConnection,
                            COMMAND_QUEUE, getRepository());
                    this.getNetworkExecutor().execute(consumerService);
                } catch (Exception exc) {
                    Log.e("StartCommandConsumer", "", exc);
                }
            }

            // Start the timer trigger
            try {
                timerService = TimerService.getInstance(this.getRepository());
                this.getNetworkExecutor().execute(timerService);
            }
            catch (IOException exc)
            {
                Log.e("StartTimer", exc.getMessage());
            }

            if(settings.getIsTestingMode())
            {
                // MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK  Start the arduino mocks
                try {

                    MockArduinoServerService mockArduinoServerService = new MockArduinoServerService(getRepository(), 8080, "Generator");
                    this.getNetworkExecutor().execute(mockArduinoServerService);

                    mockArduinoServerService = new MockArduinoServerService(getRepository(), 8081, "Tap");
                    this.getNetworkExecutor().execute(mockArduinoServerService);

                    mockArduinoServerService = new MockArduinoServerService(getRepository(), 8082, "Boiler");
                    this.getNetworkExecutor().execute(mockArduinoServerService);
                }
                catch (Exception exc)
                {
                    Log.e("StartMocks", "", exc);
                }
                // MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK  Start the arduino mocks
            }
        }



    }

    public Executor getDbExecutor()
    {
        return mAppExecutors.diskIO();
    }
    public ExecutorService getNetworkExecutor()
    {
        return mAppExecutors.networkIO();
    }
    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase());
    }


}
