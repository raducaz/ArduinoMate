package com.example.amqtest;

import android.app.Application;
import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MyApplication extends Application {
    private AppExecutors mAppExecutors;

    public static Connection AmqConnection;
    public static String EXCHANGE_NAME = "states";
    String uri = "amqp://lttjuzsi:pjSXi8zN4wT8Pljaq14lIEAVWpQddzxS@bulldog.rmq.cloudamqp.com/lttjuzsi";

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();

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


        try {
            ConsumerService consumerService = ConsumerService.getInstance().getInstance();
            this.getNetworkExecutor().execute(consumerService);
        }
        catch (IOException exc)
        {
            //TODO: handle it somehow
        }

//        try {
////            ConsumerService2 consumerService = ConsumerService2.getInstance().getInstance();
////            this.getNetworkExecutor().execute(consumerService);
////        }
////        catch (IOException exc)
////        {
////            //TODO: handle it somehow
////        }
    }

    public ExecutorService getNetworkExecutor()
    {
        return mAppExecutors.networkIO();
    }

}
