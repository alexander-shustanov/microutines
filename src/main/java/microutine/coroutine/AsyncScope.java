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

    @Suspend
    default Object call(AsyncSuspendable suspendable) {
        CoroutineContext oldContext = CoroutineContext.getCurrent();
        Continuation callerContinuation = Continuation.getCurrent();

        Continuation<Object> newContinuation = Magic.createContinuation(suspendable, this);
        Continuation<Object> continuation = new Continuation<Object>() {
            boolean wasSuspended = false;

            @Override
            public Object run(Object resumeWith) {
                Object result = newContinuation.run(resumeWith);
                if (result == Continuation.SUSPEND)
                    wasSuspended = true;
                else if (wasSuspended) {
                    oldContext.getElement(Dispatcher.KEY).dispatch(oldContext, callerContinuation);
                }

                return result;
            }
        };

        CoroutineContext newContext = oldContext.with(continuation);

        newContext.set();
        Object result = continuation.run(null);
        oldContext.set();

        return result;
    }

    @Suspend
    default <R> R call(SuspendableWithResult<AsyncScope, R> suspendable) {
        CoroutineContext oldContext = CoroutineContext.getCurrent();
        Continuation callerContinuation = Continuation.getCurrent();

        Continuation<R> newContinuation = Magic.createContinuation(suspendable, this);
        Continuation<R> continuation = new Continuation<R>() {
            boolean wasSuspended = false;

            @Override
            public R run(Object resumeWith) {
                R result = newContinuation.run(resumeWith);
                if (result == Continuation.SUSPEND)
                    wasSuspended = true;
                else if (wasSuspended) {
                    oldContext.getElement(Dispatcher.KEY).dispatch(oldContext, callerContinuation);
                }

                return result;
            }
        };

        CoroutineContext newContext = oldContext.with(continuation);

        newContext.set();
        R result = continuation.run(null);
        oldContext.set();

        return result;
    }

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

        startCoroutine(CoroutineContext.DEFAULT, wrappedContinuation);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"rawtypes"})
    static <R> R runBlocking(SuspendableWithResult<AsyncScope, R> suspendable) {
        AtomicReference<R> result = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(1);

        AsyncScopeImpl scope = new AsyncScopeImpl();
        Continuation<R> continuation = Magic.createContinuation(suspendable, scope);
        ContinuationWithCompletion wrappedContinuation = new ContinuationWithCompletion<>(continuation, r -> {
            result.set(r);
            latch.countDown();
        });

        startCoroutine(CoroutineContext.DEFAULT, wrappedContinuation);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return result.get();
    }

    static void startCoroutine(CoroutineContext context, Continuation<?> continuation) {
        context = context.with(continuation);
        context.getElement(Dispatcher.KEY).dispatch(context, continuation);
    }
}
