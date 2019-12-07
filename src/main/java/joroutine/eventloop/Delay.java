package joroutine.eventloop;

import joroutine.Continuation;
import joroutine.CoroutineContext;

import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.LockSupport;

@SuppressWarnings("rawtypes")
public class Delay {
    private final CopyOnWriteArraySet<Thread> parkedThreads = new CopyOnWriteArraySet<>();
    private final PriorityBlockingQueue<Event> events = new PriorityBlockingQueue<>(50, Comparator.comparingLong(o -> o.dispatchAt));

    public static final Delay INSTANCE = new Delay();

    private Delay() {
        Thread myScheduleThread = new Thread(() -> {
            while (true) {
                Event event = poll();
                event.context.getDispatcher().dispatch(event.context, event.continuation);
            }
        });
        myScheduleThread.setName("Delay");
        myScheduleThread.setDaemon(true);
        myScheduleThread.start();
    }

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

    public void schedule(CoroutineContext context, Continuation continuation, long delay) {
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
