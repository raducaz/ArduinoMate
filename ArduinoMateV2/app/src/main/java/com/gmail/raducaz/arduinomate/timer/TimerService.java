package com.gmail.raducaz.arduinomate.timer;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionCaller;
import com.gmail.raducaz.arduinomate.ui.TaskExecutor;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimerService implements Runnable {

    private String TAG = "TimerService";

    private static TimerService sInstance;
    private boolean isRunning;

    private DataRepository dataRepository;
    private final ExecutorService pool;

    private TimerService(DataRepository dataRepository) {

        // Initialize a dynamic pool that starts the required no of threads according to the no of tasks submitted
        pool = Executors.newFixedThreadPool(1);
        this.dataRepository = dataRepository;

        // TODO: Initialization parameters can be sent here
    }
    public static TimerService getInstance(DataRepository dataRepository) throws IOException {
        if (sInstance == null) {
            synchronized (TimerServiceHandler.class) {
                if (sInstance == null) {
                    sInstance = new TimerService(dataRepository);
                }
            }
        }
        return sInstance;
    }

    public void run() {

        if(!isRunning) {
            pool.execute(new TimerServiceHandler());
            isRunning = true;
        }
    }

    public DataRepository getDataRepository() {
        return dataRepository;
    }

    public class TimerServiceHandler implements Runnable {

        public void run() {

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                    try {
                        DeviceEntity deviceEntity = dataRepository.loadDeviceSync("192.168.100.100");
                        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(deviceEntity.getIp(), deviceEntity.getPort());
                        if(deviceGeneratorFunctions.isPressureLow())
                        {
                            try {
                                FunctionEntity functionEntity = dataRepository.loadFunctionSync(deviceEntity.getId(), "GeneratorOnOff");
                                if(functionEntity.getIsAutoEnabled()) {
                                    TaskFunctionCaller functionCaller = new TaskFunctionCaller(dataRepository, functionEntity);
                                    new TaskExecutor().execute(functionCaller);
                                }

                            }
                            catch (Exception exc) {
                                Log.e(TAG, exc.getMessage());
                            }
                        }
                    }
                    catch (Exception exc) {
                        Log.e(TAG, exc.getMessage());
                    }
                }
            }, 1000, 5000);
        }
    }
}

