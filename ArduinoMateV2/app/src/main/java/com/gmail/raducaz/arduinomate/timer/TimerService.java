package com.gmail.raducaz.arduinomate.timer;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.DeviceGeneratorFunctions;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;
import com.gmail.raducaz.arduinomate.mocks.MockArduinoClient;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionCaller;
import com.gmail.raducaz.arduinomate.remote.RemotePinStateUpdate;
import com.gmail.raducaz.arduinomate.remote.RemoteStateUpdate;
import com.gmail.raducaz.arduinomate.remote.StateFromControllerPublisher;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;
import com.gmail.raducaz.arduinomate.ui.TaskExecutor;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.gmail.raducaz.arduinomate.ArduinoMateApp.AmqConnection;
import static com.gmail.raducaz.arduinomate.ArduinoMateApp.STATES_EXCHANGE;

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

                        if(getDataRepository().getSettingsSync().getIsTestingMode()) {
                            //MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK
                            MockArduinoClient arduinoClient = new MockArduinoClient(dataRepository, "127.0.0.1", 9090);
                            arduinoClient.SendMockPinStates("Generator");
                            arduinoClient.SendMockPinStates("Tap");
                            //MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK
                        }

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

                        FunctionEntity generatorOnOff = dataRepository.loadDeviceFunctionSync("Generator","GeneratorOnOff");
                        double temperature = deviceGeneratorFunctions.getCurrentTemperature();
                        if(temperature > 45)
                        {
                            try {
                                TaskFunctionCaller functionCaller = new TaskFunctionCaller(dataRepository,
                                        "Generator",
                                        "GeneratorOnOff",
                                        FunctionResultStateEnum.OFF,
                                        "Temperature is over 45");
                                new TaskExecutor().execute(functionCaller);
                            }
                            catch (Exception exc) {
                                Log.e(TAG, exc.getMessage());
                            }
                        }
                        generatorOnOff.setLog("Temperature is " + temperature);
                        dataRepository.updateFunction(generatorOnOff);


                        //TODO: Check Level of water in garden tanks: if is maximum then Close Main Tap => close pump and generator automatically


                        // Send State Update Buffer
                        SendStateToRemoteClients(new RemotePinStateUpdate(DataRepository.getMqStateUpdateBuffer()));
                        DataRepository.clearMqStateUpdateBuffer();

                    }
                    catch (Exception exc) {
                        Log.e(TAG, exc.getMessage());
                    }
                }
            }, 1000, 5000);
        }
    }

    private void SendStateToRemoteClients(RemotePinStateUpdate stateUpdate)
    {
        SettingsEntity settings = dataRepository.getSettingsSync();
        if(settings.getIsController() && settings.getPermitRemoteControl()) {
            StateFromControllerPublisher.SendState(dataRepository, AmqConnection, STATES_EXCHANGE, stateUpdate);
        }
    }
}

