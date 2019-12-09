package joroutine.coroutine;

import joroutine.core.Continuation;

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
            context.set();
            try {
                continuation.run(resumeWith);
            } finally {
                saved.set();
            }
        });
    }

    protected abstract void doDispatch(Runnable runnable);
}
