package com.gmail.raducaz.arduinomate;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.gmail.raducaz.arduinomate.db.AppDatabase;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;
import com.gmail.raducaz.arduinomate.mocks.MockArduinoServerWorker;
import com.gmail.raducaz.arduinomate.remote.CommandToControllerConsumerWorker;
import com.gmail.raducaz.arduinomate.remote.StateFromControllerConsumerWorker;
import com.gmail.raducaz.arduinomate.tcpserver.TcpServerWorker;
import com.gmail.raducaz.arduinomate.timer.TimerService;
import com.gmail.raducaz.arduinomate.ui.ActivityMain;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

public class ArduinoMateApp extends Application {
    private String TAG = "ArduinoMateApp";

    public SettingsEntity settings;

    public static final String STATES_EXCHANGE = "states";
    public static final String COMMAND_QUEUE = "commands";
    public static Connection AmqConnection;

    public void setUri(String uri) {
        this.uri = uri;
    }

    String uri = "";

    private AppExecutors mAppExecutors;
    private TimerService timerService;
    private WorkManager mWorkManager;

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

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
        //Logger.addLogAdapter(new DiskLogAdapter());
        FormatStrategy formatStrategy = CsvFormatStrategy.newBuilder()
                .tag("custom")
                .build();
        Logger.addLogAdapter(new DiskLogAdapter(formatStrategy));

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Logger.i("Logger started");

        mAppExecutors = new AppExecutors();

        DataRepository repository = getRepository();

        // When changing database structure, need to uncomment this and comment all below, run once, then comment it again.
        // Clear app data, uninstall app (make sure AppManifest has allowBackup = false and AppDatabase.build has fallbackDestructive().
        //return;

        try {
            ExecutorService executor = mAppExecutors.networkIO();
            Callable<SettingsEntity> callable = new Callable<SettingsEntity>() {
                @Override
                public SettingsEntity call() {
                    SettingsEntity settings = repository.getSettingsSync();
                    while(settings == null)
                    {
                        settings = repository.getSettingsSync();
                    }
                    return settings;
                }
            };
            Future<SettingsEntity> future = executor.submit(callable);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

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
            Logger.e(TAG + exc.getMessage());
        }

        uri = settings.getAmqUri();

        try {

            ExecutorService executor = mAppExecutors.networkIO();
            Callable<Connection> callable = new Callable<Connection>() {
                @Override
                public Connection call() throws Exception{
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setUri(uri);
                    //Recommended settings
                    factory.setRequestedHeartbeat(30);
                    factory.setConnectionTimeout(10000);

                    return factory.newConnection();
                }
            };
            Future<Connection> future = executor.submit(callable);
            AmqConnection = future.get();

            AmqConnection.addBlockedListener(
                    new BlockedListener() {
                        @Override
                        public void handleBlocked(String reason) throws IOException {
                            int i = 0;
                        }

                        @Override
                        public void handleUnblocked() throws IOException {
                            int i = 0;
                        }
                    }
            );
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

                            Logger.e("InitAMQConnection" + cause.getMessage());
                        }
                    }
            );
        }
        catch (Exception exc)
        {
            Logger.e("InitAMQConnection"+ exc.getMessage());
            //Snackbar.make(this, exc.getMessage(), Snackbar.LENGTH_LONG).show();
        }
//        // Start the tcp server or AMQ state consumer and Mocks service - More complicated way
//        TaskServerServiceInitializer serviceInitializer = new TaskServerServiceInitializer(this);
//        new TaskExecutor().execute();

        if(!settings.getIsController()) {

            // Start AMQ state consumer
            try
            {
//                StateFromControllerConsumerService consumerService = StateFromControllerConsumerService.getInstance(getRepository(),
//                        AmqConnection, STATES_EXCHANGE);
//                this.getNetworkExecutor().execute(consumerService);

                Data.Builder builder = new Data.Builder();
                builder.putString("exchangeName", STATES_EXCHANGE);
                Data input = builder.build();

                mWorkManager = WorkManager.getInstance(this.getApplicationContext());
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(StateFromControllerConsumerWorker.class)
                        .setInputData(input)
                        .build();
                mWorkManager.enqueue(workRequest);
            }
            catch (Exception exc)
            {
                Logger.e("StartStateConsumer"+ exc.getMessage());
            }
        }
        else
        {
            // Start tcp service server
            try {
//                TcpServerService tcpServerService = TcpServerService.getInstance(getRepository());
//                this.getNetworkExecutor().execute(tcpServerService);

                Data.Builder builder = new Data.Builder();
                builder.putInt("PORT", 9090);
                Data input = builder.build();

                mWorkManager = WorkManager.getInstance(this.getApplicationContext());
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TcpServerWorker.class)
                        .setInputData(input)
                        .build();
                mWorkManager.enqueue(workRequest);
            }
            catch (Exception exc)
            {
                Logger.e("StartTcpServer"+ exc.getMessage());
            }

            // If controller that permits remote control
            if(settings.getPermitRemoteControl()) {

                // Start AMQ remote command consumer
                try {
//                    CommandToControllerConsumerService consumerService = CommandToControllerConsumerService.getInstance(AmqConnection,
//                            COMMAND_QUEUE, getRepository());
//                    this.getNetworkExecutor().execute(consumerService);

                    Data.Builder builder = new Data.Builder();
                    builder.putString("uri", uri);
                    builder.putString("queue", COMMAND_QUEUE);
                    Data input = builder.build();

                    mWorkManager = WorkManager.getInstance(this.getApplicationContext());
                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CommandToControllerConsumerWorker.class)
                            .setInputData(input)
                            .addTag("COMMAND_WORKER")
                            .build();
                    mWorkManager.enqueue(workRequest);

                } catch (Exception exc) {
                    Logger.e("StartCommandConsumer"+ exc);
                }
            }

            // Start the timer trigger
            try {
                timerService = TimerService.getInstance(this.getRepository());
                this.getNetworkExecutor().execute(timerService);

//                mWorkManager = WorkManager.getInstance(this.getApplicationContext());
//                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TimerWorker.class)
//                        .build();
//                mWorkManager.enqueue(workRequest);
            }
            catch (Exception exc)
            {
                Logger.e("StartTimer"+ exc.getMessage());
            }

            if(settings.getIsTestingMode())
            {
                // MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK  Start the arduino mocks
                try {

//                    MockArduinoServerService mockArduinoServerService = new MockArduinoServerService(getRepository(), 8080, "Generator");
//                    this.getNetworkExecutor().execute(mockArduinoServerService);
//
//                    mockArduinoServerService = new MockArduinoServerService(getRepository(), 8081, "Tap");
//                    this.getNetworkExecutor().execute(mockArduinoServerService);
//
//                    mockArduinoServerService = new MockArduinoServerService(getRepository(), 8082, "Boiler");
//                    this.getNetworkExecutor().execute(mockArduinoServerService);

                    mWorkManager = WorkManager.getInstance(this.getApplicationContext());

                    Data.Builder builder = new Data.Builder();
                    builder.putInt("PORT", 8080);
                    builder.putString("NAME", "Generator");
                    Data input = builder.build();

                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MockArduinoServerWorker.class)
                            .setInputData(input)
                            .build();
                    mWorkManager.enqueue(workRequest);

                    builder = new Data.Builder();
                    builder.putInt("PORT", 8081);
                    builder.putString("NAME", "Tap");
                    input = builder.build();

                    workRequest = new OneTimeWorkRequest.Builder(MockArduinoServerWorker.class)
                            .setInputData(input)
                            .build();
                    mWorkManager.enqueue(workRequest);

                    builder = new Data.Builder();
                    builder.putInt("PORT", 8082);
                    builder.putString("NAME", "Boiler");
                    input = builder.build();

                    workRequest = new OneTimeWorkRequest.Builder(MockArduinoServerWorker.class)
                            .setInputData(input)
                            .build();
                    mWorkManager.enqueue(workRequest);
                }
                catch (Exception exc)
                {
                    Logger.e("StartMocks"+ exc.getMessage());
                }
                // MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK  Start the arduino mocks
            }
        }

        // TODO determine if Worker finished (stopped) and restart again - maybe is better to use a periodic Worker instead
//        LiveData<List<WorkInfo>> commandWorkerState;
//        commandWorkerState = mWorkManager.getWorkInfosByTagLiveData("COMMAND_WORKER");
//        commandWorkerState.observe(this, listOfWorkInfos -> {
//            if(listOfWorkInfos == null || listOfWorkInfos.isEmpty())
//            {
//                return;
//            }
//
//            WorkInfo workInfo = listOfWorkInfos.get(0);
//            boolean finished = workInfo.getState().isFinished();
//            if(finished)
//            {
//                // Start worker again
//            }
//        });


    }


}
