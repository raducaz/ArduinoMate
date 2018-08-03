package com.gmail.raducaz.arduinomate.service;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;

import io.netty.channel.ChannelHandlerContext;

/**
 * Handler implementation for the echo client.  It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class FunctionChannelClientInboundHandler extends TcpClientInboundHandler {

    private String TAG = "FunctionChannelClientInboundHandler";

    private DataRepository mRepository;

    private FunctionEntity function;
    private FunctionExecutionEntity functionExecution;

    /**
     * Creates a client-side handler.
     */
    public FunctionChannelClientInboundHandler(FunctionEntity function,
                                               FunctionExecutionEntity functionExecution,
                                               DataRepository repository,
                                               Long responseWaitTimeout) {
        this.function = function;
        this.functionExecution = functionExecution;
        mRepository = repository;
        super.responseTimeout = responseWaitTimeout;
    }
    public FunctionChannelClientInboundHandler(FunctionEntity function,
                                               FunctionExecutionEntity functionExecution,
                                               DataRepository repository) {
        this(function, functionExecution, repository, (long) 30);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        String functionText = function.getName();
        channelActive(ctx, functionText);

        FunctionStateUpdater fu = new FunctionStateUpdater(mRepository, functionExecution);
        fu.insertExecutionLog("Command received by device");

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, String msg) throws Exception {

        try {
            FunctionStateUpdater functionStateUpdater = new FunctionStateUpdater(mRepository, msg, functionExecution);

            if (msg.endsWith("END") || msg.endsWith("END\r\n")) {
                // This will automatically set the Execution Log as well
                functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.READY); // Success
            } else {
                DeviceStateUpdater deviceStateUpdater = new DeviceStateUpdater(mRepository, msg, function.getDeviceId());
                deviceStateUpdater.updatePinStates();

                functionStateUpdater.insertExecutionLog();
                functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.EXECUTING); // Success
            }

            // Don't want to confirm to server that client received the message
            //ctx.write(msg);

            super.channelRead(ctx, msg);
        }
        catch (Exception exc)
        {
            // TODO: handle exceptions by logging them at application level log
            Log.e(TAG, exc.getMessage());
            throw exc;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {

        super.channelReadComplete(ctx);
    }

    public void onResponseTimeout()
    {
        FunctionStateUpdater fu = new FunctionStateUpdater(mRepository, functionExecution);
        fu.insertExecutionLog("Response timeout");
        fu.updateFunctionExecution(FunctionCallStateEnum.ERROR); // Error
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        FunctionStateUpdater fu = new FunctionStateUpdater(mRepository, functionExecution);
        fu.insertExecutionLog(cause);
        fu.updateFunctionExecution(FunctionCallStateEnum.ERROR); // Error

        super.exceptionCaught(ctx, cause);
    }

}

