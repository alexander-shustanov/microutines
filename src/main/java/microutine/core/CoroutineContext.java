package microutine.core;

import microutine.coroutine.Dispatchers;

public class CoroutineContext {
    private static final ThreadLocal<CoroutineContext> contexts = new ThreadLocal<>();

    public static final CoroutineContext DEFAULT = new CoroutineContext(Dispatchers.DEFAULT) {
        @Override
        void set() {
            contexts.remove();
        }
    };

    protected final Dispatcher dispatcher;

    public CoroutineContext(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    void set() {
        contexts.set(this);
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public static CoroutineContext getCurrent() {
        CoroutineContext context = contexts.get();
        if (context == null)
            return DEFAULT;
        return context;
    }
}
