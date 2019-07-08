package com.gmail.raducaz.arduinomate.telnet;

//import com.google.common.base.Preconditions;
import android.util.Log;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Simplistic telnet client.
 */
public final class TelnetClient {

    public static final int DEFAULT_COMMAND_TIMEOUT = 20000;
//    private static final Logger LOG = LoggerFactory.getLogger(TelnetClient.class);
    private final String host;
    private final int port;
    private final String user;
    private final String pw;
    private EventLoopGroup group;

    /**
     * To create instance use Builder class.
     *
     * @param host
     * @param port
     * @param user
     * @param pw
     */
    public TelnetClient(String host, int port, String user, String pw) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.pw = pw;
    }

    /**
     * Runs only one command on remote server. Returns output in responseContext.
     *
     * @param command
     * @param responseContext
     */
    public void executeCommand(String command, Map<String, Object> responseContext) {
//        Preconditions.checkNotNull(command);
//        Preconditions.checkNotNull(responseContext);
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        try {
            final Channel channel = initConnection(queue);
            ChannelFuture lastWriteFuture = channel.writeAndFlush(command + "\n");
            // Get command output
            String commandOutput = queue.poll(DEFAULT_COMMAND_TIMEOUT, TimeUnit.MILLISECONDS);
            //TODO: Determine why there is \r\n added inside the received commandOutput
            commandOutput = (commandOutput ==null? "" : commandOutput).replace("\r\n", "");
            responseContext.put(command, commandOutput);
            lastWriteFuture.sync();
        } catch (InterruptedException ex) {
            Log.e("Exception was caught {}", ex.getMessage());
        } catch (Exception generalExc) {
            Log.e("Exception was caught {}", generalExc.getMessage());
            throw generalExc;
        }
        finally {
            closeConnecton();
        }
    }

    public void executeCommands(Iterable<String> commands, Map<String, Object> responseContext) {
//        Preconditions.checkNotNull(commands);
//        Preconditions.checkNotNull(responseContext);
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        try {
            final Channel channel = initConnection(queue);
            ChannelFuture lastWriteFuture = null;
            for (String command : commands) {
                lastWriteFuture = channel.writeAndFlush(command + "\n");
                // Get command output
                String commandOutput = queue.poll(DEFAULT_COMMAND_TIMEOUT, TimeUnit.MILLISECONDS);
                responseContext.put(command, commandOutput);
            }
            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } catch (InterruptedException ex) {
            Log.e("Exception was caught {}", ex.getMessage());
        } finally {
            closeConnecton();
        }
    }

    /**
     * @param queue
     * @return
     */
    private Channel initConnection(BlockingQueue<String> queue) {
        group = new NioEventLoopGroup();
        Channel result = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new TelnetClientInitializer(user, pw, queue));
            // Start the connection attempt.
            result = b.connect(host, port).sync().channel();
//            if (!"SUCCESS".equals(queue.poll(DEFAULT_COMMAND_TIMEOUT, TimeUnit.MILLISECONDS))) {
//                LOG.error("Authorization issue");
//                result = null;
//                throw new RuntimeException("Authorization issue");
//            }
        } catch (InterruptedException ex) {
            Log.e("Exception was caught {}", ex.getMessage());
        }
        return result;
    }

    private void closeConnecton() {
        group.shutdownGracefully();
    }
}