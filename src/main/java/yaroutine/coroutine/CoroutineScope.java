package yaroutine.coroutine;

import yaroutine.core.CoroutineContext;
import yaroutine.core.Scope;
import yaroutine.core.Suspend;
import yaroutine.core.SuspendableWithResult;

import java.util.concurrent.CountDownLatch;

public interface CoroutineScope extends Scope {
    default void launch(CoroutineSuspendable suspendable) {
        CoroutineContext.EMPTY.launch(suspendable);
    }

    default void launch(CoroutineContext context, CoroutineSuspendable suspendable) {
        context.launch(suspendable);
    }

    @Suspend
    default void delay(long millis) {
        throw new RuntimeException("Unsupported");
    }

    @Suspend
    default void launchAwait(CoroutineContext context, CoroutineSuspendable suspendable) {
        throw new RuntimeException("Unsupported");
    }

    default <R> Deferred<R> async(CoroutineContext context, SuspendableWithResult<CoroutineScope, R> suspendable) {
        return null;
    }

    default <R> Deferred<R> async(SuspendableWithResult<CoroutineScope, R> suspendable) {
        return async(CoroutineContext.EMPTY, suspendable);
    }

    @Suspend
    default <R> R await(Deferred<R> deferred) {
        throw new RuntimeException();
    }

    @Suspend
    void await(CountDownLatch latch);
}
