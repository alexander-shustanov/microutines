package joroutine;

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

    @Override
    public void async(Suspendable suspendable) {
        throw new RuntimeException("Async is unsupported for sequences");
    }
}
