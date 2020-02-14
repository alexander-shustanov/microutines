package microutine.core;

@SuppressWarnings("rawtypes")
public abstract class Dispatcher implements CoroutineContext.ContextElement<Dispatcher> {
    public static CoroutineContext.ElementKey<Dispatcher> KEY = new CoroutineContext.ElementKey<>("Dispatcher");

    private String name;

    public Dispatcher(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public final void dispatch(CoroutineContext context, Continuation continuation) {
        dispatch(context, continuation, null);
    }

    public final void dispatch(CoroutineContext context, Continuation continuation, Object resumeWith) {
        if (context.getElement(Dispatcher.KEY) != this)
            throw new IllegalArgumentException("Must dispatch only with provided context dispatcher");

        doDispatch(() -> {
            CoroutineContext saved = CoroutineContext.getCurrent();
            context.set();
            try {
                synchronized (continuation) {
                    continuation.run(resumeWith);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                saved.set();
            }
        });
    }

    protected abstract void doDispatch(Runnable runnable);

    @Override
    public final CoroutineContext.ElementKey<Dispatcher> getKey() {
        return KEY;
    }
}
