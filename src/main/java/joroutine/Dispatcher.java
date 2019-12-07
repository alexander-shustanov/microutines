package joroutine;

@SuppressWarnings("rawtypes")
public abstract class Dispatcher {
    private String name;

    public Dispatcher(String name) {
        this.name = name;
    }

    public final void dispatch(CoroutineContext context, Continuation continuation) {
        doDispatch(() -> {
            CoroutineContext saved = CoroutineContext.getCurrent();
            context.set();
            try {
                return continuation.run();
            } finally {
                saved.set();
            }
        });
    }

    protected abstract void doDispatch(Continuation continuation);
}
