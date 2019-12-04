package joroutine;

public interface Dispatcher {
    Dispatcher UNSUPPORTED = suspendable -> {
        throw new RuntimeException("Unable to schedule on context");
    };

    void schedule(Suspendable suspendable);

    default void scheduleWithDelay(Suspendable suspendable, int milliseconds) {

    }
}
