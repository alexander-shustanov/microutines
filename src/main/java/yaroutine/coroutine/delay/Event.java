package yaroutine.coroutine.delay;

import yaroutine.core.CoroutineContext;
import yaroutine.core.Continuation;

@SuppressWarnings("rawtypes")
public class Event {
    public final long dispatchAt;
    public final Continuation continuation;
    public final CoroutineContext context;

    public Event(Continuation continuation, long dispatchAt, CoroutineContext context) {
        this.dispatchAt = dispatchAt;
        this.continuation = continuation;
        this.context = context;
    }

    public Event(Continuation continuation, CoroutineContext context) {
        this.context = context;
        this.dispatchAt = -1;
        this.continuation = continuation;
    }
}
