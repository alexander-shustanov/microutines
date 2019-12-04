package joroutine;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Sequence<T> implements Iterable<T> {
    private static final Object STOP = new Object();

    Object next = STOP;
    Continuation nextStep;
    Iterator<T> iterator;

    public Sequence(Suspendable<SequenceContext<T>> suspendable) {
        this.nextStep = Magic.create(suspendable, new SequenceContext(this));
        this.iterator = new Iterator<T>() {
            @Override
            public boolean hasNext() {
                if (next == STOP) {
                    nextStep.run();
                }
                return next != STOP;
            }

            @Override
            public T next() {
                if (next == STOP) {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                try {
                    return ((T) next);
                } finally {
                    next = STOP;
                }
            }
        };
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

    public static class SequenceContext<T> extends Context {
        private Sequence<T> sequence;

        public SequenceContext(Sequence<T> sequence) {
            super(Dispatcher.UNSUPPORTED);
            this.sequence = sequence;
        }

        @Suspend
        public void yield(T t) {
            sequence.next = t;
        }

        @Suspend
        public Object yieldAll(Sequence<T> another) {
            if (another.iterator.hasNext()) {
                sequence.next = another.iterator.next();
                return Magic.SUSPEND;
            }
            return null;
        }
    }

}
