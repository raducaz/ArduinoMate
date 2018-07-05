package com.gmail.raducaz.arduinomate.service;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;

import java.io.IOException;
import com.gmail.raducaz.arduinomate.db.converter.DateConverter;

import java.util.concurrent.ExecutionException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ArduinoFunctionCaller {

    private final ArduinoMateApp mApplication;
    private final FunctionEntity function;
    private long executionId;
    private FunctionExecutionEntity functionExecution;
    private final DataRepository mRepository;
    private final ExecutorService mExecutor;
    public ArduinoFunctionCaller(final ArduinoMateApp app, FunctionEntity function) {
        mApplication = app;
        this.function = function;

        mRepository = ((ArduinoMateApp) mApplication).getRepository();
        mExecutor = mApplication.getNetworkExecutor();
    }

    public void execute() {
        try {

            functionExecution = new FunctionExecutionEntity();
            functionExecution.setFunctionId(function.getId());
            functionExecution.setName(function.getName());
            functionExecution.setStartDate(DateConverter.toDate(System.currentTimeMillis()));
            executionId = mRepository.insertFunctionExecution(functionExecution);
            functionExecution.setId(executionId);

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

            functionExecution.setEndDate(DateConverter.toDate(System.currentTimeMillis()));
            functionExecution.setState(-1); // Error
            mRepository.updateFunctionExecution(functionExecution);
        }
    }

}
