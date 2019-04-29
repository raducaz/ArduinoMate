package com.gmail.raducaz.arduinomate.service;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class TcpClientService implements Callable<String> {
    private String TAG = "TcpClientService";

    TcpClientInboundHandler tcpInboundHandler;

    static final boolean SSL = System.getProperty("ssl") != null;
    static String HOST = System.getProperty("host", "192.168.11.100");
    static int PORT = 8080;
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    private final ExecutorService pool;

    public TcpClientService(String serverIp, int serverPort, TcpClientInboundHandler tcpClientInboundHandler) {

        // Initialize a dynamic pool that starts the required no of threads according to the no of tasks submitted
        pool = Executors.newCachedThreadPool();
        this.tcpInboundHandler = tcpClientInboundHandler;

        HOST = serverIp;
        PORT = serverPort;
    }

    public String call() throws Exception
    {
        //Old style
        //pool.execute(new TcpClientServiceHandler());
        //Old style

        Future<?> future = pool.submit(new TcpClientServiceHandler());

        try {
            return (String)future.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupted status
        } catch (ExecutionException e) {
            Throwable exception = e.getCause();
            // Forward to exception reporter
            throw new Exception(exception);
        }

        return null;
    }

    /* Use it to stop execution of the Thread if needed */
    public void shutdownAndAwaitTermination() {

        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }

    }


    public class TcpClientServiceHandler implements Runnable {

        private final StringDecoder DECODER = new StringDecoder();
        private final StringEncoder ENCODER = new StringEncoder();

        public void run() {

            // Configure the client.
            NioEventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        //.option(ChannelOption.TCP_NODELAY, true) // incompatible with autoread option
                        .option(ChannelOption.AUTO_READ, true)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {
                                ChannelPipeline p = ch.pipeline();
                                p.addLast( new DelimiterBasedFrameDecoder(65536, Delimiters.lineDelimiter()));
                                p.addLast(DECODER);
                                p.addLast(ENCODER);
                                p.addLast(tcpInboundHandler);
                            }
                        });

                // Start the client.
                ChannelFuture f = b.connect(HOST, PORT).sync();

                // Wait until the connection is closed.
                f.channel().closeFuture().sync();

            } catch (InterruptedException exc) {
                Log.e(TAG, exc.getMessage());
            } catch (Exception generalExc) {
                Log.e(TAG, generalExc.getMessage());
                throw generalExc;
            } finally {
                // Shut down the event loop to terminate all threads.
                group.shutdownGracefully();
            }
        }
    }
}

