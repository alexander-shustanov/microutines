package joroutine;

import joroutine.sequence.Sequence;
import joroutine.sequence.SequenceScope;
import joroutine.sequence.SequenceSuspendable;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class YieldAllTest {
    @Test
    public void testSingleLevel() {
        Sequence<Integer> nested = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                yield(100);
                yield(200);
            }
        });

        Sequence<Integer> sequence = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                yield(0);

                yieldAll(nested);

                yield(300);
            }
        });

        Iterator<Integer> iterator = sequence.iterator();

        assertEquals(0, (int) iterator.next());
        assertEquals(100, (int) iterator.next());
        assertEquals(200, (int) iterator.next());
        assertEquals(300, (int) iterator.next());
    }

    @Test
    public void testDoubleLevel() {
        Sequence<Integer> inner = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                yield(140);
                yield(180);
            }
        });


        Sequence<Integer> middle = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                yield(100);
                yieldAll(inner);
                yield(200);
            }
        });

        Sequence<Integer> outer = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                yield(0);
                yieldAll(middle);
                yield(300);
            }
        });

        Iterator<Integer> iterator = outer.iterator();

        assertEquals(0, (int) iterator.next());
        assertEquals(100, (int) iterator.next());
        assertEquals(140, (int) iterator.next());
        assertEquals(180, (int) iterator.next());
        assertEquals(200, (int) iterator.next());
        assertEquals(300, (int) iterator.next());

    }
}
