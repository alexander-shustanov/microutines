package joroutine;

public class Context {
    private final Dispatcher dispatcher;

    public Context(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void delay(Suspendable suspendable, int milliseconds) {
        Magic.getContext(suspendable).dispatcher.scheduleWithDelay(suspendable, milliseconds);
    }
}
