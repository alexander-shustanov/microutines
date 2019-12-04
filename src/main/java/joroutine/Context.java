package joroutine;

public class Context {
    private final Dispatcher dispatcher;

    public Context(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Suspend
    public void delay(Suspendable suspendable, int milliseconds) {
        Continuation continuation = Magic.getContinuation(suspendable);
        Magic.getContext(suspendable).dispatcher.scheduleWithDelay(continuation, milliseconds);
    }

    public void launch(Suspendable suspendable) {

    }
}
