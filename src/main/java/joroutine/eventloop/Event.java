package joroutine.eventloop;

import joroutine.Context;
import joroutine.Continuation;

@SuppressWarnings("rawtypes")
public class Event {
    public final long dispatchAt;
    public final Continuation continuation;
    public final Context context;

    public Event(Continuation continuation, long dispatchAt, Context context) {
        this.dispatchAt = dispatchAt;
        this.continuation = continuation;
        this.context = context;
    }

    public Event(Continuation continuation, Context context) {
        this.context = context;
        this.dispatchAt = -1;
        this.continuation = continuation;
    }
}
