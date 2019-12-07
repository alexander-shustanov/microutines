package joroutine;

import joroutine.eventloop.Delay;

import java.util.concurrent.CountDownLatch;

import static joroutine.CoroutineContext.getCurrent;

@SuppressWarnings("rawtypes")
class ScopeImpl implements CoroutineScope {
    Continuation continuation;

    @Override
    @Suspend
    public void delay(long millis) {
        Delay.INSTANCE.schedule(getCurrent(), continuation, millis);
    }

    @Override
    @Suspend
    public void await(Deferred deferred) {
        throw new RuntimeException("Not implemented");
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
