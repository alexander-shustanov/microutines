package joroutine.coroutine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
}
