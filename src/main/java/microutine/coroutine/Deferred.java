package microutine.coroutine;

import microutine.core.Continuation;
import microutine.core.CoroutineContext;
import microutine.core.Dispatcher;
import microutine.core.Suspend;

import static microutine.core.CoroutineContext.getCurrent;

public class Deferred<T> {
    private Runnable waiter;

    private volatile T value = null;
    private volatile boolean done = false;

    public void accept(T value) {
        Runnable waiter;

        synchronized (this) {
            this.value = value;
            done = true;

            waiter = this.waiter;
        }

        if (waiter != null)
            waiter.run();
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
                return getValue();
            }
            waiter = () -> myContext.getElement(Dispatcher.KEY).dispatch(myContext, myContinuation, getValue());

            return Continuation.getSuspend();
        }
    }
}
