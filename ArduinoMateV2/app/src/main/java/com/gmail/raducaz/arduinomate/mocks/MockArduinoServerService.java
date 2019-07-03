package com.gmail.raducaz.arduinomate.mocks;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.MockPinStateEntity;
import com.gmail.raducaz.arduinomate.model.MockPinState;
import com.gmail.raducaz.arduinomate.tcpserver.TcpServerInboundHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

public class MockArduinoServerService implements Runnable {

    private String TAG = "MockArduinoServerService";

    private static MockArduinoServerService sInstance;
    private boolean isRunning;

    private DataRepository dataRepository;

    final boolean SSL = System.getProperty("ssl") != null;
    final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    int PORT;
    String NAME;

    // Configure SSL.
    final SslContext sslCtx = null;
    // TODO: Configure SSL if needed
//        if (SSL) {
//            SelfSignedCertificate ssc = new SelfSignedCertificate();
//            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
//        } else {
//            sslCtx = null;
//        }

    private final ExecutorService pool;

    public MockArduinoServerService(DataRepository dataRepository, int port, String name) {

        // Initialize a dynamic pool that starts the required no of threads according to the no of tasks submitted
        pool = Executors.newFixedThreadPool(1);
        this.dataRepository = dataRepository;
        this.PORT = port;
        this.NAME = name;

    }
    public static MockArduinoServerService getInstance(DataRepository dataRepository, int port, String name) throws IOException {
        if (sInstance == null) {
            synchronized (MockArduinoServerInboundHandler.class) {
                if (sInstance == null) {
                    sInstance = new MockArduinoServerService(dataRepository, port, name);
                }
            }
        }
        return sInstance;
    }

    public void run() {

        if(!isRunning) {
            pool.execute(new MockArduinoServerRunner());
            isRunning = true;
        }

    }

    public int getPort() {
        return PORT;
    }
    public String getName() {
        return  NAME;
    }
    public DataRepository getDataRepository() {
        return dataRepository;
    }

    public void generateMockPins()
    {
        // Clear all the mock pins
        dataRepository.deleteMockPinStatesByDevice(getName());

        // pin 20 is for device state
        for (int i=0;i<=20;i++)
        {
            MockPinStateEntity pinState = new MockPinStateEntity();
            pinState.setDeviceName(getName());
            pinState.setNumber(i);

            dataRepository.insertMockPinState(pinState);
        }

        switch (getName())
        {
            case "Generator":
                setPinState(2, 1); //digitalWrite(ContactGenerator, HIGH); // Cuplat = contact OFF
                setPinState(6, 1);//                digitalWrite(ActuatorNormal, HIGH); // Decuplat
                setPinState(7, 1);//                digitalWrite(ActuatorInversat, HIGH); // Decuplat
                setPinState(3, 1);//                digitalWrite(ContactRetea220V, HIGH); // Decuplat
                setPinState(8, 0);//                digitalWrite(ContactDemaror12V, LOW); // Decuplat
                setPinState(17, 1023);//                digitalWrite(PresostatProbeSender, HIGH); A3
                setPinState(18, 1023);//                digitalWrite(PresostatProbeReceiver, HIGH);A4
                break;
            case "Tap":
                break;
        }
    }
    private void setPinState(int pinNo, double state)
    {
        MockPinStateEntity pin = dataRepository.loadMockDevicePinStateSync(getName(), pinNo);
        pin.setState(state);
        dataRepository.updateMockPinState(pin);
    }
    public class MockArduinoServerRunner implements Runnable {

        private final StringDecoder DECODER = new StringDecoder();
        private final StringEncoder ENCODER = new StringEncoder();

        public void run() {

            // TODO: Initialization parameters can be sent here
            generateMockPins();

            // Configure the server.
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        //.option(ChannelOption.SO_BACKLOG, 100)
                        .option(ChannelOption.AUTO_READ, true)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {
                                ChannelPipeline p = ch.pipeline();
                                if (sslCtx != null) {
                                    p.addLast(sslCtx.newHandler(ch.alloc()));
                                }
                                //p.addLast(new LoggingHandler(LogLevel.INFO));
                                p.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                                p.addLast(DECODER);
                                p.addLast(ENCODER);
                                // Important to create a new instance for every new channel to accept multiple client connections
                                p.addLast(new MockArduinoServerInboundHandler(getDataRepository(), getPort(), getName()));
                            }
                        });

                // Start the server.
                ChannelFuture f = b.bind(PORT).sync();

                // Wait until the server socket is closed.
                f.channel().closeFuture().sync();
            }
            catch(InterruptedException exc)
            {
                Log.e(TAG, exc.getMessage());
            }
            catch (Exception generalExc)
            {
                Log.e(TAG, generalExc.getMessage());
            }
            finally {
                // Shut down all event loops to terminate all threads.
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }
    }
}

