package yaroutine.core;

import yaroutine.coroutine.CoroutineScope;
import yaroutine.coroutine.CoroutineScopeImpl;
import yaroutine.coroutine.CoroutineSuspendable;
import yaroutine.coroutine.Dispatchers;

import java.util.function.Consumer;

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
        Continuation continuation = Magic.createContinuation(suspendable, new CoroutineScopeImpl());
        dispatcher.dispatch(this, continuation);
    }

    public <T> void launch(SuspendableWithResult<CoroutineScope, T> suspendable, Consumer<T> completion) {
        CoroutineScopeImpl scope = new CoroutineScopeImpl();
        Continuation continuation = Magic.createContinuation(suspendable, scope);
        ContinuationWithCompletion wrappedContinuation = new ContinuationWithCompletion(continuation,  completion);
        dispatcher.dispatch(this, wrappedContinuation);
    }

    public void launch(CoroutineSuspendable suspendable, Runnable completion) {
        CoroutineScopeImpl scope = new CoroutineScopeImpl();
        Continuation continuation = Magic.createContinuation(suspendable, scope);
        ContinuationWithCompletion wrappedContinuation = new ContinuationWithCompletion(continuation, o -> completion.run());
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
