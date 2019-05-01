package com.gmail.raducaz.arduinomate;

import android.app.Application;

import com.gmail.raducaz.arduinomate.db.AppDatabase;
import com.gmail.raducaz.arduinomate.tcpserver.TcpServerService;
import com.gmail.raducaz.arduinomate.timer.TimerService;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

public class ArduinoMateApp extends Application {

    private AppExecutors mAppExecutors;
    private TcpServerService tcpServerService;
    private TimerService timerService;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();

        // Start the tcp server
        try {
            tcpServerService = TcpServerService.getInstance(this.getRepository());
            this.getNetworkExecutor().execute(tcpServerService);
        }
        catch (IOException exc)
        {
            //TODO: handle it somehow
        }

        // Start the timer trigger
        try {
            timerService = TimerService.getInstance(this.getRepository());
            this.getNetworkExecutor().execute(timerService);
        }
        catch (IOException exc)
        {
            //TODO: handle it somehow
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
