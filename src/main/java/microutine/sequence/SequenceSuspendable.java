package microutine.sequence;

import microutine.core.Suspend;
import microutine.core.Suspendable;

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
