package com.example.amqtest;

import android.app.Application;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class MyApplication extends Application {
    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();

        try {
            ConsumerService consumerService = ConsumerService.getInstance().getInstance();
            this.getNetworkExecutor().execute(consumerService);
        }
        catch (IOException exc)
        {
            //TODO: handle it somehow
        }

        try {
            ConsumerService2 consumerService = ConsumerService2.getInstance().getInstance();
            this.getNetworkExecutor().execute(consumerService);
        }
        catch (IOException exc)
        {
            //TODO: handle it somehow
        }
    }

    public ExecutorService getNetworkExecutor()
    {
        return mAppExecutors.networkIO();
    }

}
