package microutine.coroutine;

import microutine.core.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public interface CoroutineScope extends Scope {

    default void launch(CoroutineSuspendable suspendable) {
        launch(CoroutineContext.getCurrent(), suspendable);
    }

    void launch(CoroutineContext context, CoroutineSuspendable suspendable);

    @Suspend
    default void delay(long millis) {
        throw new RuntimeException("Unsupported");
    }

    default <R> Deferred<R> async(CoroutineContext context, SuspendableWithResult<CoroutineScope, R> suspendable) {
        return null;
    }

    default <R> Deferred<R> async(SuspendableWithResult<CoroutineScope, R> suspendable) {
        return async(CoroutineContext.DEFAULT, suspendable);
    }

    @Suspend
    void await(CountDownLatch latch);

    static void runBlocking(CoroutineSuspendable suspendable) {
        CountDownLatch latch = new CountDownLatch(1);

        CoroutineScopeImpl scope = new CoroutineScopeImpl();
        Continuation<Void> continuation = Magic.createContinuation(suspendable, scope);
        ContinuationWithCompletion<Void> wrappedContinuation = new ContinuationWithCompletion<>(continuation, o -> latch.countDown());
        CoroutineContext.DEFAULT.getDispatcher().dispatch(CoroutineContext.DEFAULT, wrappedContinuation);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static <R> R runBlocking(SuspendableWithResult<CoroutineScope, R> suspendable) {
        AtomicReference<R> result = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(1);

        CoroutineScopeImpl scope = new CoroutineScopeImpl();
        Continuation continuation = Magic.createContinuation(suspendable, scope);
        ContinuationWithCompletion wrappedContinuation = new ContinuationWithCompletion<R>(continuation, r -> {
            result.set(r);
            latch.countDown();
        });
        CoroutineContext.DEFAULT.getDispatcher().dispatch(CoroutineContext.DEFAULT, wrappedContinuation);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return result.get();
    }
}
