package com.gmail.raducaz.arduinomate.timer;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.mocks.MockArduinoClient;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionCaller;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
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
            // Start in 1 second and run repeatedly every 5s
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                    try {

                        //MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK
                        MockArduinoClient arduinoClient = new MockArduinoClient(dataRepository, "127.0.0.1", 9090);
                        arduinoClient.SendMockPinStates("Generator");
                        arduinoClient.SendMockPinStates("Tap");
                        //MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK


                        // Check the pressure periodically by probing, if pressure low start generator and pump
                        DeviceGeneratorFunctions deviceGeneratorFunctions = new DeviceGeneratorFunctions(dataRepository, "Generator");

                        if(deviceGeneratorFunctions.isPressureLow())
                        {
                            try {
                                TaskFunctionCaller functionCaller = new TaskFunctionCaller(dataRepository,
                                        "Tap",
                                        "HouseWaterOnOff",
                                        FunctionResultStateEnum.ON,
                                        "Pressure is LOW");
                                new TaskExecutor().execute(functionCaller);
                            }
                            catch (Exception exc) {
                                Log.e(TAG, exc.getMessage());
                            }
                        }

                        //TODO: Check Level of water in garden tanks: if is maximum then Close Main Tap => close pump and generator automatically

                    }
                    catch (Exception exc) {
                        Log.e(TAG, exc.getMessage());
                    }
                }
            }, 1000, 5000);
        }
    }
}

