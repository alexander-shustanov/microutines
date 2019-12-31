package microutine;

import microutine.sequence.Sequence;
import microutine.sequence.SequenceScope;
import microutine.sequence.SequenceSuspendable;
import org.junit.Test;

public class TestSequenceErrors {
    @Test(expected = RuntimeException.class)
    public void testOverflow() {
        Sequence<Void> voids = new Sequence<>(new SequenceSuspendable<Void>() {
            @Override
            public void run(SequenceScope<Void> scope) {
                yieldAll(new Sequence<>(this));
                yield(null);
            }
        });

        for (Void aVoid : voids) {
            System.out.println(aVoid);
        }
    }
}
