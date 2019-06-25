package com.gmail.raducaz.arduinomate.tcpserver;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.service.DeviceStateUpdater;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionStateUpdater;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Created by Radu.Cazacu on 12/1/2017.
 */

public class TcpServerInboundHandler extends SimpleChannelInboundHandler<String> {

    private String TAG = "TcpServerInboundHandler";
    private static TcpServerInboundHandler sInstance;

    private DataRepository dataRepository;

    protected ChannelHandlerContext ctx;
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public TcpServerInboundHandler(DataRepository repository) {
        dataRepository = repository;
    }

    public DataRepository getDataRepository()
    {
        return dataRepository;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        this.ctx = ctx;

        Channel incoming = ctx.channel();
        channels.add(ctx.channel());

        Log.d(TAG, "ChannelActive-RemoteAddress " + incoming.remoteAddress().toString());
        final ChannelFuture f = incoming.writeAndFlush("[Server] - " + incoming.remoteAddress() + " has joined!\r\n");

//        final ChannelFuture f = incoming.writeAndFlush("\r\n");

        f.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) {
                Log.d(TAG, "Complete");
            }
        });

        for(Channel channel : channels) {
            // Send to all
        }

        Log.d(TAG, "ChannelActive-Channels " + channels.size());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        Channel incoming = ctx.channel();
        incoming.writeAndFlush("[Server] - " + incoming.remoteAddress() + " has left!\r\n");

        for(Channel channel : channels) {
            // Send to all
        }
        Log.d(TAG, "ChannelInactive-RemoteAddress " + incoming.remoteAddress().toString());
        Log.d(TAG, "ChannelInactive-Channels " + channels.size());
        channels.remove(incoming);
        Log.d(TAG, "ChannelInactive-Channels " + channels.size());
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        try {
            Channel incoming = ctx.channel();

            // This is an ip like 10.2.2.10 is virtual cannot be used...
            //String deviceIp = incoming.remoteAddress().toString();

            DeviceStateUpdater deviceStateUpdater = new DeviceStateUpdater(dataRepository, msg);
            deviceStateUpdater.updatePinStates();

            if (!(msg.endsWith("END") || msg.endsWith("END\r\n"))) {
                DataRepository.appendMqStateUpdateBuffer(msg);
            }

            // Function State is not received anymore
//            FunctionStateUpdater functionStateUpdater = new FunctionStateUpdater(dataRepository, msg);
//            functionStateUpdater.insertExecutionLog();
//            //TODO: Get the FunctionCallState from the ResultState, result state can be Executing
//            functionStateUpdater.updateFunctionExecution(FunctionCallStateEnum.READY); // Success

            Log.d(TAG, "ChannelRead0-MSG " + msg + " from " + incoming.remoteAddress());

            // We do not need to write a ChannelBuffer here.
            // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
            ChannelFuture future = ctx.write("Received from " + incoming.remoteAddress() + ":" + msg);

            if (msg.endsWith("END") || msg.endsWith("END\r\n")) {
                Log.d(TAG, "ChannelRead0-END " + incoming.remoteAddress());
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
        catch (Exception exc)
        {
            // TODO: handle exceptions by logging them at application level log
            Log.e(TAG, "ChannelRead0-ERROR " + exc.getMessage());
            throw exc;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // TODO: Check if Don't want to confirm to server that client received the message - nothing to flush ?
        Log.d(TAG, "ChannelReadComplete");
        ctx.flush();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        cause.printStackTrace();
        Log.e(TAG, cause.getMessage(), cause);
        // Close the connection when an exception is raised.
        closeConnection();
    }

    public void closeConnection()
    {
        Log.d(TAG, "CloseConnection");
        if(ctx != null)
            ctx.close();
    }

}
