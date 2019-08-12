package com.gmail.raducaz.arduinomate.tcpserver;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gmail.raducaz.arduinomate.AppExecutors;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.AppDatabase;
import com.orhanobut.logger.Logger;

import java.io.IOException;
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

public class TcpServerWorker extends Worker {

    private String TAG = "TcpServerWorker";

    private DataRepository dataRepository;

    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "192.168.11.99");//Not used
    static int PORT = Integer.parseInt(System.getProperty("port", "9090"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));
    // Configure SSL.
    final SslContext sslCtx = null;
    // TODO: Configure SSL if needed
//        if (SSL) {
//            SelfSignedCertificate ssc = new SelfSignedCertificate();
//            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
//        } else {
//            sslCtx = null;
//        }

    public TcpServerWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);

        AppExecutors executors = new AppExecutors();
        dataRepository = DataRepository.getInstance(AppDatabase.getInstance(appContext, executors));

        PORT = getInputData().getInt("PORT", 8080);
    }

    @NonNull
    @Override
    public Result doWork() {
        final StringDecoder DECODER = new StringDecoder();
        final StringEncoder ENCODER = new StringEncoder();

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
                                p.addLast(new TcpServerInboundHandler(dataRepository));
                            }
                        });

                // Start the server.
                ChannelFuture f = b.bind(PORT).sync();

                // Wait until the server socket is closed.
                f.channel().closeFuture().sync();

                return Result.success();
            }
            catch(InterruptedException exc)
            {
                Logger.e(TAG+ exc.getMessage());

                return Result.failure();
            }
            catch (Exception generalExc)
            {
                Logger.e(TAG+ generalExc.getMessage());

                return Result.failure();
            }
            finally {
                // Shut down all event loops to terminate all threads.
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }
}

