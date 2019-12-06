package joroutine;

public interface Scope {
    default void launch(Suspendable suspendable) {
        Context.EMPTY.launch(suspendable);
    }

    default void launch(Context context, Suspendable suspendable) {
        context.launch(suspendable);
    }

    @Suspend
    default void delay(long millis) {
        throw new RuntimeException("Unsupported");
    }

    @Suspend
    default void launchAwait(Context context, Suspendable suspendable) {
        throw new RuntimeException("Unsupported");
    }

    default <R> Deferred<R> async(SuspendableWithResult<?, R> suspendable) {
        return null;
    }

    @Suspend
    default void await(Deferred deferred) {
        throw new RuntimeException();
    }
}
