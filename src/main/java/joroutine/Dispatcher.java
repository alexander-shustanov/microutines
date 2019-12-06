package joroutine;

import joroutine.eventloop.Event;
import joroutine.eventloop.EventLoop;

@SuppressWarnings("rawtypes")
public abstract class Dispatcher {
    private final EventLoop eventLoop = new EventLoop();

    public Dispatcher(String name) {
        Thread myScheduleThread = new Thread(() -> {
            while (true) {
                Event event = eventLoop.poll();
                dispatch(event.context, event.continuation);
            }
        });
        myScheduleThread.setName("Scheduler: " + name);
        myScheduleThread.setDaemon(true);
        myScheduleThread.start();
    }

    public void schedule(Context context, Continuation continuation) {
        eventLoop.push(context, continuation);
    }

    public void schedule(Context context, Continuation continuation, long delay) {
        eventLoop.push(context, continuation, delay);
    }

    private void dispatch(Context context, Continuation continuation) {
        doDispatch(() -> {
            Context saved = Context.getCurrent();
            context.set();
            try {
                return continuation.run();
            } finally {
                saved.set();
            }
        });
    }

    protected abstract void doDispatch(Continuation continuation);
}
