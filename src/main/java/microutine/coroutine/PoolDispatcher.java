package microutine.coroutine;

import microutine.core.Dispatcher;

import java.util.concurrent.ExecutorService;

public class PoolDispatcher extends Dispatcher {
    private final ExecutorService executor;

    public PoolDispatcher(String name, ExecutorService executor) {
        super(name);
        this.executor = executor;
    }

    @Override
    protected void doDispatch(Runnable block) {
        executor.submit(block);
    }
}
