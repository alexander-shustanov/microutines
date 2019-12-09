package joroutine.core;

import java.util.function.Consumer;

public class ContinuationWithCompletion<S extends Scope, R> implements Continuation<R> {
    private final Continuation<R> delegate;
    private final Consumer<R> completion;

    public ContinuationWithCompletion(Continuation<R> delegate, Consumer<R> completion) {
        this.delegate = delegate;
        this.completion = completion;
    }

    @Override
    public R run(Object resumeWith) {
        R result = delegate.run(resumeWith);
        if (delegate.isDone()) {
            completion.accept(result);
        }
        return result;
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }
}
