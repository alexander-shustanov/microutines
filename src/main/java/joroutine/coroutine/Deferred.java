package joroutine.coroutine;

import joroutine.core.Continuation;
import joroutine.core.CoroutineContext;
import joroutine.core.Suspend;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static joroutine.core.CoroutineContext.getCurrent;

public class Deferred<T> {
    private List<Consumer<T>> waiters = new ArrayList<>();

    private volatile T value = null;
    private volatile boolean done = false;

    public void accept(T value) {
        synchronized (this) {
            this.value = value;
            done = true;

            for (Consumer<T> waiter : waiters) {
                waiter.accept(value);
            }
        }
    }

    public void addWaiter(Consumer<T> waiter) {
        waiters.add(waiter);
    }

    public boolean isDone() {
        return done;
    }

    public T getValue() {
        return value;
    }

    @Suspend
    public T await() {
        CoroutineContext myContext = getCurrent();
        Continuation<Object> myContinuation = Continuation.getCurrent();

        synchronized (this) {
            if (isDone()) {
                myContext.getDispatcher().dispatch(myContext, myContinuation, getValue());
                return (T) Continuation.SUSPEND;
            }
            addWaiter(r -> {
                myContext.getDispatcher().dispatch(myContext, myContinuation, r);
            });

            return (T) Continuation.SUSPEND;
        }
    }
}
