package joroutine;

import joroutine.sequence.Sequence;
import joroutine.sequence.SequenceScope;
import joroutine.sequence.SequenceSuspendable;
import org.junit.Test;

public class ExceptionInSequenceTest {
    @Test(expected = RuntimeException.class)
    public void theTest() {
        Sequence<Integer> sequence = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                yield(100);
                yield(200);
                throw new RuntimeException();
            }
        });

        for (Integer integer : sequence) {

        }
    }

    @Test(expected = RuntimeException.class)
    public void anotherTest() {
        Sequence<Integer> integers = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                yield(100);
                yield(200);

                yieldAll(new Sequence<>(new SequenceSuspendable<Integer>() {
                    @Override
                    public void run(SequenceScope<Integer> scope) {
                        yield(220);
                        yield(240);
                        throw new RuntimeException();
                    }
                }));

                yield(300);
            }
        });

        for (Integer integer : integers) {

        }
    }
}
