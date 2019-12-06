package joroutine;

public class ContinuationWithCompletion<T extends Scope> implements Continuation<T> {
    private final Continuation<T> delegate;
    private final Runnable completion;

    public ContinuationWithCompletion(Continuation<T> delegate, Runnable completion) {
        this.delegate = delegate;
        this.completion = completion;
    }

    @Override
    public T run() {
        T result = delegate.run();
        if (delegate.isDone()) {
            completion.run();
        }
        return result;
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }
}
