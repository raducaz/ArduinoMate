package com.gmail.raducaz.arduinomate.mocks;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.MockPinStateEntity;
import com.gmail.raducaz.arduinomate.model.MockPinState;
import com.gmail.raducaz.arduinomate.service.DeviceStateUpdater;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

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

public class MockArduinoServerInboundHandler extends SimpleChannelInboundHandler<String> {

    private String TAG = "MockArduinoServerInboundHandler";
    private static MockArduinoServerInboundHandler sInstance;

    private int port; // This is to identify the device mock number
    private String deviceName;
    private DataRepository dataRepository;

    protected ChannelHandlerContext ctx;
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public MockArduinoServerInboundHandler(DataRepository dataRepository, int port, String deviceName) {
        this.dataRepository = dataRepository;
        this.port = port;
        this.deviceName = deviceName;
    }

    public int getPort()
    {
        return port;
    }
    public String getDeviceName()
    {
        return deviceName;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        this.ctx = ctx;

        Channel incoming = ctx.channel();
        channels.add(ctx.channel());

        Log.d(TAG, "ChannelActive-RemoteAddress " + incoming.remoteAddress().toString());

        // Don't do it, alters the result
        //final ChannelFuture f = incoming.writeAndFlush("[Server] - " + incoming.remoteAddress() + " has joined!\r\n");

//        final ChannelFuture f = incoming.writeAndFlush("\r\n");

//        f.addListener(new ChannelFutureListener() {
//            public void operationComplete(ChannelFuture future) {
//                Log.d(TAG, "Complete");
//            }
//        });

        for(Channel channel : channels) {
            // Send to all
        }

        Log.d(TAG, "ChannelActive-Channels " + channels.size());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        Channel incoming = ctx.channel();

        // Don't do it, alters the result
//        incoming.writeAndFlush("[Server] - " + incoming.remoteAddress() + " has left!\r\n");

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

            Log.d(TAG, "ChannelRead0-MSG " + msg + " from " + incoming.remoteAddress());

            String result = ProcessCommand(msg);

            // We do not need to write a ChannelBuffer here.
            // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
            ChannelFuture future = ctx.write(result);

            if (msg.endsWith("]") || msg.endsWith("\n")) {
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

    private String ProcessCommand(String msg)
    {
        try
        {
            JSONArray cmd = new JSONArray(msg);

            for (int i=0;i<cmd.length();i++) {

                JSONObject obj = cmd.getJSONObject(i);
                String key = "";
                Iterator<String> keys = obj.keys();

                while (keys.hasNext()) {

                    int pin = 0;
                    key = keys.next();
                    //keys.remove();

                    if (key.startsWith("=") ||
                            key.startsWith("~")) // Cmd set
                    {
                        pin = getPin(1, key);
                        if (obj.has("@")) // Cmd set temp for x ms
                        {
                            if (key.startsWith("~")) {
                                setDigitalPinStateTemp(pin, obj.optDouble(key), obj.optInt("@"), pin>13);
                            } else {
                                if(pin<=13)
                                    setDigitalPinStateTemp(pin, obj.optDouble(key),obj.optInt("@"), false);
                                else
                                    setAnalogPinStateTemp(pin, obj.optDouble(key),obj.optInt("@"));
                            }
                        }
                        else // Cmd permanent set
                        {
                            if (key.startsWith("~")) {
                                setDigitalPinState(pin, obj.optDouble(key), pin>13);
                            } else {
                                if(pin<=13)
                                    setDigitalPinState(pin, obj.optDouble(key), false);
                                else
                                    setAnalogPinState(pin, obj.optDouble(key));
                            }
                        }
                    }
                    if (key.startsWith("?") ||
                            key.startsWith("#")) // Cmd get
                    {
                        pin = getPin(1, key);
                        if (key.startsWith("#")) { // get digital
                            obj.put(key, getDigitalPinState(pin, pin>13));
                        }
                        else {
                            obj.put(key, pin<=13 ? getDigitalPinState(pin, false) : getAnalogPinState(pin));
                        }
                    }
                    if (key.startsWith("!")) // Cmd set
                    {
                        Thread.sleep(obj.getInt(key));
                    }
                    if (key.equals("F1")) // Cmd function
                    {
                        obj.put(key,getAnalogPinState(15));
                    }
                    if (key.equals("F0")) // Cmd function restart
                    {
                        setAnalogPinState(20, 3);
                    }

                }

                obj.put(">", 1);
            }

            return cmd.toString();
        }
        catch (Exception exc)
        {
            return msg;
        }
    }

    private int getPin(int startIndex, String key) {

        String sPin="";
        boolean isAnalogPin = (key.charAt(startIndex) == 'A' || key.charAt(startIndex) == 'a');

        byte i = 0; //Start from second character id analog
        int index = startIndex + (isAnalogPin ? 1 : 0);

        sPin = key.substring(index);

        int pinNo = Integer.parseInt(sPin);

        //return isAnalogPin ? (pinNo<2 ? pinNo+16 : pinNo+18) : pinNo;
        return isAnalogPin ? pinNo + 14 : pinNo; //A0 is 14, A1 is 15...
    }

    private double getDigitalPinState(int pinNo, boolean isAnalog)
    {
        MockPinStateEntity pin = dataRepository.loadMockDevicePinStateSync(deviceName, pinNo);
        double state = pin.getState();
        return isAnalog ? (state >= 1023 ? 1 : 0) : state;
    }
    private double getAnalogPinState(int pinNo)
    {
        MockPinStateEntity pin = dataRepository.loadMockDevicePinStateSync(deviceName, pinNo);
        return pin.getState();
    }
    private void setDigitalPinState(int pinNo, double state, boolean isAnalog)
    {
        MockPinStateEntity pin = dataRepository.loadMockDevicePinStateSync(deviceName, pinNo);
        pin.setState(isAnalog ? (state>=1?1023:0) : (state>0?1:0));
        dataRepository.updateMockPinState(pin);
    }
    private void setAnalogPinState(int pinNo, double state)
    {
        MockPinStateEntity pin = dataRepository.loadMockDevicePinStateSync(deviceName, pinNo);
        pin.setState(state);
        dataRepository.updateMockPinState(pin);
    }

    private void setDigitalPinStateTemp(int pinNo, double state, int interval, boolean isAnalog)
    {
        try {
            MockPinStateEntity pin = dataRepository.loadMockDevicePinStateSync(deviceName, pinNo);
            double initialState = pin.getState();
            pin.setState(isAnalog ? (state>=1?1023:0) : (state>0?1:0));
            dataRepository.updateMockPinState(pin);

            Thread.sleep(interval);

            pin.setState(initialState);
            dataRepository.updateMockPinState(pin);
        }
        catch (Exception exc)
        {
            Log.e(TAG, exc.getMessage(), exc);
        }

    }
    private void setAnalogPinStateTemp(int pinNo, double state, int interval)
    {
        try {
            MockPinStateEntity pin = dataRepository.loadMockDevicePinStateSync(deviceName, pinNo);
            double initialState = pin.getState();
            pin.setState(state);
            dataRepository.updateMockPinState(pin);

            Thread.sleep(interval);

            pin.setState(initialState);
            dataRepository.updateMockPinState(pin);
        }
        catch (Exception exc)
        {
            Log.e(TAG, exc.getMessage(), exc);
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
