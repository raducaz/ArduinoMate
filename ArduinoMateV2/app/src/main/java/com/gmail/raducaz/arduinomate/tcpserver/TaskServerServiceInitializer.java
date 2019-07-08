package com.gmail.raducaz.arduinomate.tcpserver;

import android.util.Log;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;
import com.gmail.raducaz.arduinomate.mocks.MockArduinoServerService;
import com.gmail.raducaz.arduinomate.processes.TaskInterface;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class TaskServerServiceInitializer implements TaskInterface {

    private String TAG = "TaskServerServiceInitializer";

    private final ArduinoMateApp mApplication;
    private final SettingsEntity settings;
    private final DataRepository mRepository;
    private final ExecutorService mExecutor;

    public TaskServerServiceInitializer(final ArduinoMateApp app) {
        mApplication = app;

        mRepository = mApplication.getRepository();
        settings = mRepository.getSettingsSync();
        mExecutor = mApplication.getNetworkExecutor();
    }

    public void execute() {

        try {
            if(settings == null) return;

            if(!settings.getIsController()) {

                // Start AMQ state consumer

            }
            else
            {
                // Start tcp service server
                try {
                    TcpServerService tcpServerService = TcpServerService.getInstance(mRepository);
                    mApplication.getNetworkExecutor().execute(tcpServerService);
                }
                catch (IOException exc)
                {
                    //TODO: handle it somehow
                }

                // TODO: If app in test mode - start the Arduino Mocks for tests
                if(settings.getIsTestingMode())
                {
                    // MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK  Start the arduino mocks
                    try {

                        MockArduinoServerService mockArduinoServerService = new MockArduinoServerService(mRepository, 8080, "Generator");
                        mApplication.getNetworkExecutor().execute(mockArduinoServerService);

                        mockArduinoServerService = new MockArduinoServerService(mRepository, 8081, "Tap");
                        mApplication.getNetworkExecutor().execute(mockArduinoServerService);

                        mockArduinoServerService = new MockArduinoServerService(mRepository, 8082, "Boiler");
                        mApplication.getNetworkExecutor().execute(mockArduinoServerService);
                    }
                    catch (Exception exc)
                    {
                        //TODO: handle it somehow
                    }
                    // MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK MOCK  Start the arduino mocks
                }
            }

            if(settings.getPermitRemoteControl()) {

                // Start AMQ client command consumer

            }


        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
        }
    }

}
