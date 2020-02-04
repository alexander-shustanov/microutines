package test;

import microutine.sequence.Sequence;
import microutine.sequence.SequenceScope;
import microutine.sequence.SequenceSuspendable;

public class IntegerSequence extends SequenceSuspendable<Integer> {

    @Override
    public void run(SequenceScope<Integer> scope) {
        scope.yield(10);
        scope.yield(20);
        scope.yield(30);
    }
}