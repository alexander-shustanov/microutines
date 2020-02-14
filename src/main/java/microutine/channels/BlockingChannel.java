package microutine.channels;

import microutine.core.Continuation;
import microutine.core.CoroutineContext;
import microutine.core.Dispatcher;
import microutine.core.Suspend;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BlockingChannel<T> {
    private volatile AtomicReference<T> value = null;
    private List<Runnable> waiters = new ArrayList<>();

    private T next() {
        CoroutineContext context = CoroutineContext.getCurrent();
        Continuation continuation = Continuation.getCurrent();

        synchronized (this) {
            waiters.add(() -> {
                context.getElement(Dispatcher.KEY).dispatch(context, continuation, value.get());
            });

            if (value != null) {
                for (Runnable waiter : waiters) {
                    waiter.run();
                }
                waiters.clear();
                value = null;
            }
        }

        return (T) Continuation.SUSPEND;
    }

    @Suspend
    private void put(T t) {
        CoroutineContext context = CoroutineContext.getCurrent();
        Continuation continuation = Continuation.getCurrent();

        synchronized (this) {
            if (value == null) {
                value = new AtomicReference<>(t);
                if (waiters.isEmpty()) {
                    waiters.add(() -> {
                        context.getElement(Dispatcher.KEY).dispatch(context, continuation);
                    });
                } else {
                    for (Runnable waiter : waiters) {
                        waiter.run();
                    }
                    waiters.clear();
                    context.getElement(Dispatcher.KEY).dispatch(context, continuation);
                    value = null;
                }
            } else {
                waiters.add(() -> {
                    value = new AtomicReference<>(t);
                    context.getElement(Dispatcher.KEY).dispatch(context, continuation);
                });
            }
        }
    }

    public InputChannel<T> getInputChannel() {
        return BlockingChannel.this::next;
    }

    public OutputChannel<T> getOutputChannel() {
        return BlockingChannel.this::put;
    }

}
