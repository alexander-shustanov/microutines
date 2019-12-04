package joroutine;

public interface Dispatcher {
    Dispatcher UNSUPPORTED = continuation -> {
        throw new RuntimeException("Unable to schedule on context");
    };

    Dispatcher IMMEDIATE = Continuation::run;

    void schedule(Continuation continuation);

    default void scheduleWithDelay(Continuation continuation, int milliseconds) {

    }
}
