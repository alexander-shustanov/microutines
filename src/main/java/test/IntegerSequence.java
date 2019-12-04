package test;

import joroutine.Sequence;
import joroutine.Suspendable;

public class IntegerSequence extends Suspendable<Sequence.SequenceContext<Integer>> {

    @Override
    public void run(Sequence.SequenceContext<Integer> context) {
        int i = 0;

        Sequence<Integer> nested = new Sequence<>(new Suspendable<Sequence.SequenceContext<Integer>>() {
            @Override
            public void run(Sequence.SequenceContext<Integer> context) {
                context.yield(11);
                context.yield(50);
            }
        });

        for (Integer integer : nested) {
            context.yield(integer * 2);
            context.yield(integer * 4);
        }

        for (; i < 100; i++) {
            context.yield(i += 10);
            context.yield(i += 10);
            context.yield(i += 10);
        }
        context.yield(i += 10);
    }
}