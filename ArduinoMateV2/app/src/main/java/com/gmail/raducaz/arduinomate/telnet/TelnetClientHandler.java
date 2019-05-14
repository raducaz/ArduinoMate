package com.gmail.raducaz.arduinomate.telnet;

import android.util.Log;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * Handles a client-side channel.
 */
@Sharable
public class TelnetClientHandler extends SimpleChannelInboundHandler<String> {

//    private static final Logger LOG = LoggerFactory.getLogger(TelnetClientHandler.class);
    private static final int DEFAULT_REFRESH_TIMER = 300;
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
    private final BlockingQueue<String> queue;
    private ScheduledFuture<?> scheduleTask;
    private StringBuilder outputBuilder = new StringBuilder();

    public TelnetClientHandler(BlockingQueue queue) {
        this.queue = queue;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        outputBuilder.append(msg);
        outputBuilder.append("\r\n");
        updateTimer();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.e("Error ", cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        scheduledExecutor.shutdown();
        scheduledExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        super.channelInactive(ctx);
        scheduledExecutor.shutdownNow();
        Log.d("channelInactive", "Deactivated channel successfully");
    }

    public void updateTimer() {
        if (scheduleTask != null) {
            scheduleTask.cancel(true);
        }
        scheduleTask = scheduledExecutor.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    queue.offer(outputBuilder.toString(), 1, TimeUnit.SECONDS);
                    outputBuilder = new StringBuilder();
                } catch (InterruptedException ex) {
                    Log.w("updateTimer", "Interrupted task. Exception {}", ex);
                }
            }
        }, DEFAULT_REFRESH_TIMER, TimeUnit.MILLISECONDS);
    }
}
