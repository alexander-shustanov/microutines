package test;

import joroutine.Sequence;
import joroutine.Suspendable;

public class FloatSequence extends Suspendable<Sequence.SequenceContext<Float>> {
    @Override
    public void run(Sequence.SequenceContext<Float> context) {
        float i = 10;

        context.yield(i += 10);
        context.yield(i += 10);
        context.yield(i += 10);
    }
}