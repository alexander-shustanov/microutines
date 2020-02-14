package microutine.coroutine;

import microutine.core.*;
import microutine.coroutine.delay.Delay;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static microutine.core.CoroutineContext.getCurrent;

@SuppressWarnings("rawtypes")
public class AsyncScopeImpl implements AsyncScope {
    public AsyncScopeImpl() {
    }

    @Override
    @Suspend
    public void delay(long millis) {
        Delay.INSTANCE.schedule(getCurrent(), Continuation.getCurrent(), millis);
    }

    @Override
    public <R> Deferred<R> async(CoroutineContext context, SuspendableWithResult<AsyncScope, R> suspendable) {
        Deferred<R> deferred = new Deferred<>();
        launch(context, suspendable, deferred::accept);
        return deferred;
    }

    @Override
    public void await(CountDownLatch latch) {
        CoroutineContext context = getCurrent();
        launch(getCurrent(), createLatchChecker(latch, context, Continuation.getCurrent()));
    }

    private <R> void launch(CoroutineContext context, SuspendableWithResult<AsyncScope, R> suspendable, Consumer<R> completion) {
        AsyncScopeImpl scope = new AsyncScopeImpl();
        Continuation<R> continuation = Magic.createContinuation(suspendable, scope);
        ContinuationWithCompletion<R> wrappedContinuation = new ContinuationWithCompletion<>(continuation, completion);

        AsyncScope.startCoroutine(context, wrappedContinuation);
    }

    @Override
    public void launch(CoroutineContext context, AsyncSuspendable suspendable) {
        AsyncScopeImpl scope = new AsyncScopeImpl();
        Continuation continuation = Magic.createContinuation(suspendable, scope);
        AsyncScope.startCoroutine(context, continuation);
    }

    private AsyncSuspendable createLatchChecker(CountDownLatch latch, CoroutineContext context, Continuation continuation) {
        return new AsyncSuspendable() {
            @Override
            public void run(AsyncScope scope) {
                if (latch.getCount() == 0) {
                    context.getElement(Dispatcher.KEY).dispatch(context, continuation);
                } else {
                    launch(context, createLatchChecker(latch, context, continuation));
                }
            }
        };
    }
}
