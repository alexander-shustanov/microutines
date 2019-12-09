package joroutine.coroutine;

import joroutine.core.Continuation;
import joroutine.core.Suspend;
import joroutine.core.SuspendableWithResult;
import joroutine.coroutine.delay.Delay;

import java.util.concurrent.CountDownLatch;

import static joroutine.coroutine.CoroutineContext.getCurrent;

@SuppressWarnings("rawtypes")
public class CoroutineScopeImpl implements CoroutineScope {
    public Continuation continuation;

    @Override
    @Suspend
    public void delay(long millis) {
        Delay.INSTANCE.schedule(getCurrent(), continuation, millis);
    }

    @Override
    @Suspend
    public <R> R await(Deferred<R> deferred) {
        CoroutineContext current = getCurrent();

        synchronized (deferred) {
            if (deferred.isDone()) {
                current.dispatcher.dispatch(current, continuation, deferred.getValue());
                return (R) Continuation.SUSPEND;
            }
            deferred.addWaiter(r -> {
                current.dispatcher.dispatch(current, continuation, r);
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
        context.launch(createLatchChecker(latch, context));
    }

    private CoroutineSuspendable createLatchChecker(CountDownLatch latch, CoroutineContext context) {
        return new CoroutineSuspendable() {
            @Override
            public void run(CoroutineScope scope) {
                if (latch.getCount() == 0) {
                    context.getDispatcher().dispatch(context, continuation);
                } else {
                    context.launch(createLatchChecker(latch, context));
                }
            }
        };
    }

    @Suspend
    @Override
    public void launchAwait(CoroutineContext context, CoroutineSuspendable suspendable) {
        CoroutineContext currentContext = getCurrent();
        context.launch(suspendable, () -> {
            currentContext.getDispatcher().dispatch(currentContext, continuation);
        });
    }
}
