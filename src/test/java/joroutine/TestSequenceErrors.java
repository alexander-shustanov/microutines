package joroutine;

import joroutine.sequence.Sequence;
import joroutine.sequence.SequenceScope;
import joroutine.sequence.SequenceSuspendable;
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
