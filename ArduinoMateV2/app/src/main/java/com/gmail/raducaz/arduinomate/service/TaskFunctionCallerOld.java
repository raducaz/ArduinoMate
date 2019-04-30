package com.gmail.raducaz.arduinomate.service;

import android.util.Log;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.processes.TaskInterface;
import com.gmail.raducaz.arduinomate.tcpclient.FunctionChannelClientInboundHandler;
import com.gmail.raducaz.arduinomate.tcpclient.TcpClientService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TaskFunctionCallerOld implements TaskInterface {

    private String TAG = "TaskFunctionCaller";

    private final ArduinoMateApp mApplication;
    private final FunctionEntity function;
    private long executionId;
    private FunctionExecutionEntity functionExecution;
    private final DataRepository mRepository;
    private final ExecutorService mExecutor;
    public TaskFunctionCallerOld(final ArduinoMateApp app, FunctionEntity function) {
        mApplication = app;
        this.function = function;

        mRepository = mApplication.getRepository();
        mExecutor = mApplication.getNetworkExecutor();
    }

    public void execute() {

        functionExecution = new FunctionExecutionEntity();
        functionExecution.setFunctionId(function.getId());
        functionExecution.setName(function.getName());

        FunctionStateUpdater functionStateUpdater = new FunctionStateUpdater(mRepository, "Command sent to device ...",functionExecution);

        try {
            // Automatically insert the log as well = Execution started...
            functionStateUpdater.startFunctionExecution();

            DeviceEntity device = mRepository.loadDeviceSync(function.getDeviceId());

            //Old style
//            mExecutor.execute(new TcpClientService(device.getIp(), device.getPort(),
//                    new FunctionChannelClientInboundHandler(function, mRepository)
//            ));
            //Old style

            TcpClientService tcpClient = new TcpClientService(device.getIp(), device.getPort(),
                    new FunctionChannelClientInboundHandler(function, functionExecution, mRepository)
            );
            Future<?> future = mExecutor.submit(tcpClient);
            try {
                future.get();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Reset interrupted status
            } catch (ExecutionException e) {
                Throwable exception = e.getCause();
                // Forward to exception reporter
                throw new Exception(exception);
            }

        } catch (Exception exc) {

            Log.e(TAG, exc.getMessage());
            functionStateUpdater.insertExecutionLog(exc);
            functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.ERROR);
        }
    }

}
