package joroutine;

import joroutine.core.Suspendable;
import joroutine.sequence.Sequence;
import joroutine.sequence.SequenceScope;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class YieldAllTest {
    @Test
    public void testSingleLevel() {
        Sequence<Integer> nested = new Sequence<>(new Suspendable<SequenceScope<Integer>>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                scope.yield(100);
                scope.yield(200);
            }
        });

        Sequence<Integer> sequence = new Sequence<>(new Suspendable<SequenceScope<Integer>>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                scope.yield(0);

                scope.yieldAll(nested);

                scope.yield(300);
            }
        });

        Iterator<Integer> iterator = sequence.iterator();

        assertEquals(0, (int) iterator.next());
        assertEquals(100, (int) iterator.next());
        assertEquals(200, (int) iterator.next());
        assertEquals(300, (int) iterator.next());
    }

    public void testDoubleLevel() {

        Sequence<Integer> inner = new Sequence<>(new Suspendable<SequenceScope<Integer>>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                scope.yield(140);
                scope.yield(180);
            }
        });


        Sequence<Integer> middle = new Sequence<>(new Suspendable<SequenceScope<Integer>>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                scope.yield(100);
                scope.yieldAll(inner);
                scope.yield(200);
            }
        });

        Sequence<Integer> outer = new Sequence<>(new Suspendable<SequenceScope<Integer>>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                scope.yield(0);

                scope.yieldAll(middle);

                scope.yield(300);
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
