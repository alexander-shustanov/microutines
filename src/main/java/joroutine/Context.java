package joroutine;

@SuppressWarnings("rawtypes")
public class Context {
    private static final ThreadLocal<Context> contexts = new ThreadLocal<>();

    public static final Context EMPTY = new Context(Dispatchers.DEFAULT) {
        @Override
        void set() {
            contexts.set(null);
        }
    };

    protected final Dispatcher dispatcher;

    public Context(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void launch(Suspendable suspendable) {
        Continuation continuation = Magic.createContinuation(suspendable, new ScopeImpl());
        dispatcher.schedule(this, continuation);
    }

    public void launch(Suspendable suspendable, Runnable completion) {
        ScopeImpl scope = new ScopeImpl();
        Continuation continuation = Magic.createContinuation(suspendable, scope);
        ContinuationWithCompletion wrappedContinuation = new ContinuationWithCompletion(continuation, completion);
        scope.continuation = wrappedContinuation;
        dispatcher.schedule(this, wrappedContinuation);
    }

    void set() {
        contexts.set(this);
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public static Context getCurrent() {
        Context context = contexts.get();
        if (context == null)
            return EMPTY;
        return context;
    }


}
