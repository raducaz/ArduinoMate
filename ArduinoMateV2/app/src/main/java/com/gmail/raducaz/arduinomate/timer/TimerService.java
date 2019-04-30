package com.gmail.raducaz.arduinomate.timer;

import android.util.Log;

import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.commands.ArduinoCommander;
import com.gmail.raducaz.arduinomate.commands.CommandProbePressure;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.tcpserver.TcpServerInboundHandler;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
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
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                    try {
                        ArduinoCommander arduinoCommander = new ArduinoCommander("192.168.100.100", 8080);
                        CommandProbePressure.execute(arduinoCommander);
                    }
                    catch (Exception exc) {
                        Log.e(TAG, exc.getMessage());
                    }
                }
            }, 1000, 1000);
        }
    }
}

