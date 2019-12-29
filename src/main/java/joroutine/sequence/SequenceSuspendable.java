package joroutine.sequence;

import joroutine.core.Suspend;
import joroutine.core.Suspendable;

public abstract class SequenceSuspendable<T> extends Suspendable<SequenceScope<T>> {
    private Sequence<T> sequence;

    public void setSequence(Sequence<T> sequence) {
        this.sequence = sequence;
    }

    @Suspend
    public void yield(T t) {
        sequence.next = t;
    }

    @Suspend
    public void yieldAll(Sequence<T> another) {
        sequence.nextIterator = another.iterator();
    }
}
