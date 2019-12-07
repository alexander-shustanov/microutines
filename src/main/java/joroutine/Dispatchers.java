package joroutine;

import java.util.concurrent.Executors;

public class Dispatchers {
    public static final Dispatcher IO = new PoolDispatcher("IO", Executors.newScheduledThreadPool(2));

    public static final Dispatcher DEFAULT = new PoolDispatcher("DEFAULT", Executors.newFixedThreadPool(8));
}
