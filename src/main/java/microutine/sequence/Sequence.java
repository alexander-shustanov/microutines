package microutine.sequence;

import microutine.core.Continuation;
import microutine.core.Magic;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Sequence<T> implements Iterable<T> {
    private static final Object STOP = new Object();

    Object next = STOP;
    Continuation nextStep;
    Iterator<T> iterator = new Iterator<T>() {
        @Override
        public boolean hasNext() {
            if (next == STOP) {
                if (nextIterator != null) {
                    if (nextIterator.hasNext()) {
                        next = nextIterator.next();
                        return true;
                    } else {
                        nextIterator = null;
                    }
                }
                nextStep.run(null);
                if (nextIterator != null)
                    return hasNext();
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
    Iterator<T> nextIterator;

    public Sequence(SequenceSuspendable<T> suspendable) {
        this.nextStep = Magic.createContinuation(suspendable, new SequenceScope<>(this));
        suspendable.setSequence(this);
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }
}

