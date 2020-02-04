package microutine.core;

@SuppressWarnings("rawtypes")
class CoroutineHolder {
    private static final ThreadLocal<Continuation> continuations = new ThreadLocal<>();

    static Continuation getContinuation() {
        return continuations.get();
    }

    static void set(Continuation continuation) {
        continuations.set(continuation);
    }
}
