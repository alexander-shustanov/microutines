package joroutine;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Deferred<T> {
    AtomicReference<T> value = new AtomicReference<>();
    AtomicBoolean done = new AtomicBoolean(false);

    public void complete(T value) {
        this.value.set(value);
        done.set(true);
    }

    public T getValue() {
        return value.get();
    }

    public boolean isDone() {
        return done.get();
    }
}
