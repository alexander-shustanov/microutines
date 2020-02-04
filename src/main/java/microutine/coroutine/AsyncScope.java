package microutine.coroutine;

import microutine.core.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public interface AsyncScope extends CoroutineScope {
    AsyncScope GLOBAL_SCOPE = new AsyncScopeImpl();

    default void launch(AsyncSuspendable suspendable) {
        launch(CoroutineContext.getCurrent(), suspendable);
    }

    void launch(CoroutineContext context, AsyncSuspendable suspendable);

    @Suspend
    void delay(long millis);

    <R> Deferred<R> async(CoroutineContext context, SuspendableWithResult<AsyncScope, R> suspendable);

    default <R> Deferred<R> async(SuspendableWithResult<AsyncScope, R> suspendable) {
        return async(CoroutineContext.DEFAULT, suspendable);
    }

    @Suspend
    void await(CountDownLatch latch);

    static void runBlocking(AsyncSuspendable suspendable) {
        CountDownLatch latch = new CountDownLatch(1);

        AsyncScopeImpl scope = new AsyncScopeImpl();
        Continuation<Void> continuation = Magic.createContinuation(suspendable, scope);
        ContinuationWithCompletion<Void> wrappedContinuation = new ContinuationWithCompletion<>(continuation, o -> latch.countDown());
        CoroutineContext.DEFAULT.getDispatcher().dispatch(CoroutineContext.DEFAULT, wrappedContinuation);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static <R> R runBlocking(SuspendableWithResult<AsyncScope, R> suspendable) {
        AtomicReference<R> result = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(1);

        AsyncScopeImpl scope = new AsyncScopeImpl();
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
