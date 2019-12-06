package joroutine.eventloop;

import joroutine.Context;
import joroutine.Continuation;

import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.LockSupport;

@SuppressWarnings("rawtypes")
public class EventLoop {
    private final CopyOnWriteArraySet<Thread> parkedThreads = new CopyOnWriteArraySet<>();
    private final PriorityBlockingQueue<Event> events = new PriorityBlockingQueue<>(50, Comparator.comparingLong(o -> o.dispatchAt));

    public Event poll() {
        while (true) {
            Event polled = null;
            try {
                polled = events.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            long delta = polled.dispatchAt - System.currentTimeMillis();

            synchronized (this) {
                if (delta > 0) {
                    events.add(polled);
                } else {
                    return polled;
                }

                parkedThreads.add(Thread.currentThread());
            }

            LockSupport.parkNanos(this, delta * 1000);
        }
    }

    public void push(Context context, Continuation continuation) {
        pushMessage(new Event(continuation, context));
    }

    public void push(Context context, Continuation continuation, long delay) {
        pushMessage(new Event(continuation, System.currentTimeMillis() + delay, context));
    }

    private void pushMessage(Event e) {
        synchronized (this) {
            events.add(e);
            for (Thread parkedThread : parkedThreads) {
                LockSupport.unpark(parkedThread);
            }
            parkedThreads.clear();
        }
    }
}
