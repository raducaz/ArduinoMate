package com.gmail.raducaz.arduinomate.service;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;

import io.netty.channel.ChannelHandlerContext;

/**
 * Handler implementation for the echo client.  It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class FunctionChannelClientInboundHandler extends TcpClientInboundHandler {

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
        this(function, functionExecution, repository, (long) 10);
    }

    private void insertExecutionLog(String msg)
    {
        ExecutionLogEntity log = new ExecutionLogEntity();
        log.setExecutionId(functionExecution.getId());
        log.setLog(msg);
        log.setDate(DateConverter.toDate(System.currentTimeMillis()));
        long logId = mRepository.insertFunctionExecution(functionExecution);

        mRepository.insertExecutionLog(log);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        String functionText = function.getName();
        channelActive(ctx, functionText);
        insertExecutionLog("Execution start...");

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, String msg) {

        insertExecutionLog(msg);

        // Don't want to confirm to server that client received the message
        //ctx.write(msg);

        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {

        super.channelReadComplete(ctx);

        insertExecutionLog("Execution end.");
    }

    public void onResponseTimeout()
    {
        insertExecutionLog("Response timeout.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        insertExecutionLog(cause.getStackTrace().toString());

        super.exceptionCaught(ctx, cause);
    }

}

