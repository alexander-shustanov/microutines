package microutine.sequence;

import microutine.core.CoroutineScope;
import microutine.core.Suspend;

public class SequenceScope<T> implements CoroutineScope {
    private Sequence<T> sequence;

    public SequenceScope(Sequence<T> sequence) {
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
