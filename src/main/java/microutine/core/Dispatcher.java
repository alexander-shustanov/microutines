package microutine.core;

@SuppressWarnings("rawtypes")
public abstract class Dispatcher {
    private String name;

    public Dispatcher(String name) {
        this.name = name;
    }

    public final void dispatch(CoroutineContext context, Continuation continuation) {
        dispatch(context, continuation, null);
    }

    public final void dispatch(CoroutineContext context, Continuation continuation, Object resumeWith) {
        doDispatch(() -> {
            CoroutineContext saved = CoroutineContext.getCurrent();
            Continuation savedContinuation = CoroutineHolder.getContinuation();
            context.set();
            CoroutineHolder.set(continuation);
            try {
                synchronized (continuation) {
                    continuation.run(resumeWith);
                }
            } finally {
                saved.set();
                CoroutineHolder.set(savedContinuation);
            }
        });
    }

    protected abstract void doDispatch(Runnable runnable);
}
