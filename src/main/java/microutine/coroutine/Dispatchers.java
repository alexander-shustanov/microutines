package microutine.coroutine;

import microutine.core.Dispatcher;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Dispatchers {
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        ThreadFactory delegate = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = delegate.newThread(r);
            thread.setUncaughtExceptionHandler(EXCEPTION_HANDLER);
            return thread;
        }
    };

    private static final Thread.UncaughtExceptionHandler EXCEPTION_HANDLER = (t, e) -> {
        System.err.println("Exception in " + t.getName());
        e.printStackTrace(System.err);
    };

    public static final Dispatcher IO = new PoolDispatcher("IO", Executors.newScheduledThreadPool(2, THREAD_FACTORY));

    public static final Dispatcher DEFAULT = new PoolDispatcher("DEFAULT", Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), THREAD_FACTORY));

}
