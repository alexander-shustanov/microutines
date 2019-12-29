package yaroutine.core;

@SuppressWarnings("rawtypes")
class ContinuationHolder {
    private static final ThreadLocal<Continuation> continuations = new ThreadLocal<>();

    static Continuation getCurrent() {
        return continuations.get();
    }

    static void set(Continuation continuation) {
        continuations.set(continuation);
    }
}
