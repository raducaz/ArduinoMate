package com.example.amqtest;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


/**
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
public class AppExecutors {

    private final Executor mDiskIO;

    private final ExecutorService mNetworkIO;

    private final Executor mMainThread;

    private AppExecutors(Executor diskIO, ExecutorService networkIO, Executor mainThread) {
        this.mDiskIO = diskIO;
        this.mNetworkIO = networkIO;
        this.mMainThread = mainThread;
    }

    public AppExecutors() {
        this(Executors.newSingleThreadExecutor(), Executors.newCachedThreadPool(),
                new MainThreadExecutor());
    }

    public Executor diskIO() {
        return mDiskIO;
    }

    public ExecutorService networkIO() {
        return mNetworkIO;
    }

    public Executor mainThread() {
        return mMainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}