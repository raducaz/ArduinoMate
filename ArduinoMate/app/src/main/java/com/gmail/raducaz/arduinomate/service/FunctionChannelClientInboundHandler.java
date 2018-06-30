package com.gmail.raducaz.arduinomate.service;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;

import io.netty.channel.ChannelHandlerContext;

/**
 * Handler implementation for the echo client.  It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class FunctionChannelClientInboundHandler extends TcpClientInboundHandler {

    private DataRepository dataRepository;

    private FunctionEntity function;

    /**
     * Creates a client-side handler.
     */
    public FunctionChannelClientInboundHandler(FunctionEntity function, DataRepository repository, Long responseWaitTimeout) {
        this.function = function;
        dataRepository = repository;
        super.responseTimeout = responseWaitTimeout;
    }
    public FunctionChannelClientInboundHandler(FunctionEntity function, DataRepository repository) {
        this(function, repository, (long) 10);
    }


    private void updateFunctionLog(String msg, boolean append)
    {
        if(append)
            function.setLog(function.getLog() + ">" + msg);
        else
            function.setLog(msg);

        dataRepository.updateFunction(function);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        String functionText = function.getText();
        channelActive(ctx, functionText);
        updateFunctionLog("Function call sent...", false);

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, String msg) {

        updateFunctionLog(msg, true);

        // Don't want to confirm to server that client received the message
        //ctx.write(msg);

        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        super.channelReadComplete(ctx);
    }

    public void onResponseTimeout()
    {
        updateFunctionLog("Response timeout.", false);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        updateFunctionLog(cause.getStackTrace().toString(), false);

        super.exceptionCaught(ctx, cause);
    }

}

