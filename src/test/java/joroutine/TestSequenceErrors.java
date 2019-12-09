package joroutine;

import joroutine.core.Suspendable;
import joroutine.sequence.Sequence;
import joroutine.sequence.SequenceScope;
import org.junit.Test;

public class TestSequenceErrors {
    @Test(expected = RuntimeException.class)
    public void testOverflow() {
        Sequence<Void> voids = new Sequence<>(new Suspendable<SequenceScope<Void>>() {
            @Override
            public void run(SequenceScope<Void> scope) {
                scope.yieldAll(new Sequence<>(this));

                scope.yield(null);
            }
        });

        for (Void aVoid : voids) {
            System.out.println(aVoid);
        }
    }
}
