package microutine.coroutine;

import microutine.core.*;
import microutine.coroutine.delay.Delay;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static microutine.core.CoroutineContext.getCurrent;

@SuppressWarnings("rawtypes")
public class CoroutineScopeImpl implements CoroutineScope {



    public CoroutineScopeImpl() {
    }

    @Override
    @Suspend
    public void delay(long millis) {
        Delay.INSTANCE.schedule(getCurrent(), Continuation.getCurrent(), millis);
    }

    @Override
    public <R> Deferred<R> async(CoroutineContext context, SuspendableWithResult<CoroutineScope, R> suspendable) {
        Deferred<R> deferred = new Deferred<>();
        launch(context, suspendable, deferred::accept);
        return deferred;
    }

    @Override
    public void await(CountDownLatch latch) {
        CoroutineContext context = getCurrent();
        launch(getCurrent(), createLatchChecker(latch, context, Continuation.getCurrent()));
    }

    private <R> void launch(CoroutineContext context, SuspendableWithResult<CoroutineScope, R> suspendable, Consumer<R> completion) {
        CoroutineScopeImpl scope = new CoroutineScopeImpl();
        Continuation<R> continuation = Magic.createContinuation(suspendable, scope);
        ContinuationWithCompletion<R> wrappedContinuation = new ContinuationWithCompletion<>(continuation, completion);
        context.getDispatcher().dispatch(context, wrappedContinuation);
    }

    @Override
    public void launch(CoroutineContext context, CoroutineSuspendable suspendable) {
        CoroutineScopeImpl scope = new CoroutineScopeImpl();
        Continuation continuation = Magic.createContinuation(suspendable, scope);
        context.getDispatcher().dispatch(context, continuation);
    }

    private CoroutineSuspendable createLatchChecker(CountDownLatch latch, CoroutineContext context, Continuation continuation) {
        return new CoroutineSuspendable() {
            @Override
            public void run(CoroutineScope scope) {
                if (latch.getCount() == 0) {
                    context.getDispatcher().dispatch(context, continuation);
                } else {
                    launch(context, createLatchChecker(latch, context, continuation));
                }
            }
        };
    }
}
