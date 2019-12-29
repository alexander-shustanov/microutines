package yaroutine.coroutine;

import yaroutine.core.Continuation;
import yaroutine.core.CoroutineContext;
import yaroutine.core.Suspend;
import yaroutine.core.SuspendableWithResult;
import yaroutine.coroutine.delay.Delay;

import java.util.concurrent.CountDownLatch;

import static yaroutine.core.CoroutineContext.getCurrent;

@SuppressWarnings("rawtypes")
public class CoroutineScopeImpl implements CoroutineScope {
    @Override
    @Suspend
    public void delay(long millis) {
        Delay.INSTANCE.schedule(getCurrent(), Continuation.getCurrent(), millis);
    }

    @Override
    @Suspend
    public <R> R await(Deferred<R> deferred) {
        CoroutineContext myContext = getCurrent();
        Continuation<Object> myContinuation = Continuation.getCurrent();

        synchronized (deferred) {
            if (deferred.isDone()) {
                myContext.getDispatcher().dispatch(myContext, myContinuation, deferred.getValue());
                return (R) Continuation.SUSPEND;
            }
            deferred.addWaiter(r -> {
                myContext.getDispatcher().dispatch(myContext, myContinuation, r);
            });

            return (R) Continuation.SUSPEND;
        }
    }

    @Override
    public <R> Deferred<R> async(CoroutineContext context, SuspendableWithResult<CoroutineScope, R> suspendable) {
        Deferred<R> deferred = new Deferred<>();
        context.launch(suspendable, deferred::accept);
        return deferred;
    }

    @Override
    public void await(CountDownLatch latch) {
        CoroutineContext context = getCurrent();
        context.launch(createLatchChecker(latch, context, Continuation.getCurrent()));
    }

    private CoroutineSuspendable createLatchChecker(CountDownLatch latch, CoroutineContext context, Continuation continuation) {
        return new CoroutineSuspendable() {
            @Override
            public void run(CoroutineScope scope) {
                if (latch.getCount() == 0) {
                    context.getDispatcher().dispatch(context, continuation);
                } else {
                    context.launch(createLatchChecker(latch, context, continuation));
                }
            }
        };
    }

    @Suspend
    @Override
    public void launchAwait(CoroutineContext context, CoroutineSuspendable suspendable) {
        CoroutineContext currentContext = getCurrent();
        Continuation<Object> myContinuation = Continuation.getCurrent();

        context.launch(suspendable, () -> {
            currentContext.getDispatcher().dispatch(currentContext, myContinuation);
        });
    }
}
