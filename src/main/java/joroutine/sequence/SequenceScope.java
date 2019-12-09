package joroutine.sequence;

import joroutine.core.Suspend;
import joroutine.core.Scope;

public class SequenceScope<T> implements Scope {
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
