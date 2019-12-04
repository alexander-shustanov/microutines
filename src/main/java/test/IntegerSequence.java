package test;

import joroutine.Sequence;
import joroutine.SequenceScope;
import joroutine.Suspendable;

public class IntegerSequence extends Suspendable<SequenceScope<Integer>> {

    @Override
    public void run(SequenceScope<Integer> scope) {
        int i = 0;

        Sequence<Integer> nested = new Sequence<>(new Suspendable<SequenceScope<Integer>>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                scope.yield(11);
                scope.yield(50);
            }
        });

        for (Integer integer : nested) {
            scope.yield(integer * 2);
            scope.yield(integer * 4);
        }

        for (; i < 100; i++) {
            scope.yield(i += 10);
            scope.yield(i += 10);
            scope.yield(i += 10);
        }
        scope.yield(i += 10);
    }
}