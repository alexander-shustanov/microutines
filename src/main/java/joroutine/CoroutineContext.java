package joroutine;

@SuppressWarnings("rawtypes")
public class CoroutineContext {
    private static final ThreadLocal<CoroutineContext> contexts = new ThreadLocal<>();

    public static final CoroutineContext EMPTY = new CoroutineContext(Dispatchers.DEFAULT) {
        @Override
        void set() {
            contexts.set(null);
        }
    };

    protected final Dispatcher dispatcher;

    public CoroutineContext(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void launch(CoroutineSuspendable suspendable) {
        Continuation continuation = Magic.createContinuation(suspendable, new ScopeImpl());
        dispatcher.dispatch(this, continuation);
    }

    public void launch(Suspendable suspendable, Runnable completion) {
        ScopeImpl scope = new ScopeImpl();
        Continuation continuation = Magic.createContinuation(suspendable, scope);
        ContinuationWithCompletion wrappedContinuation = new ContinuationWithCompletion(continuation, completion);
        scope.continuation = wrappedContinuation;
        dispatcher.dispatch(this, wrappedContinuation);
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
            return EMPTY;
        return context;
    }
}
