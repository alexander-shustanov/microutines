package joroutine;

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


    @Test(expected = RuntimeException.class)
    public void testAsyncNotSupported() {
        new Sequence<Integer>(new Suspendable<SequenceScope<Integer>>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                scope.launch(Context.EMPTY, this);
            }
        }).forEach(integer -> {
        });
    }
}
