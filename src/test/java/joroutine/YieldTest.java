package joroutine;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class YieldTest {
    @Test
    public void testIntSequence() {
        Sequence<Integer> sequence = new Sequence<Integer>(new Suspendable<SequenceScope<Integer>>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                scope.yield(10);
                scope.yield(20);
                scope.yield(30);
            }
        });

        List<Integer> list = new ArrayList<>();
        for (Integer integer : sequence) {
            list.add(integer);
        }

        assertEquals(10, (int) list.get(0));
        assertEquals(20, (int) list.get(1));
        assertEquals(30, (int) list.get(2));
    }

    @Test
    public void testOuterData() {
        AtomicInteger next = new AtomicInteger(100);
        AtomicBoolean end = new AtomicBoolean(false);
        Sequence<Integer> sequence = new Sequence<>(new Suspendable<SequenceScope<Integer>>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                while (!end.get()) {
                    scope.yield(next.get());
                }
            }
        });

        Iterator<Integer> iterator = sequence.iterator();

        next.set(100);
        assertEquals(100, ((int) iterator.next()));

        next.set(200);
        assertEquals(200, ((int) iterator.next()));

        next.set(300);
        assertEquals(300, ((int) iterator.next()));

        end.set(true);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testNested() {
        Sequence<Integer> sequence = new Sequence<>(new Suspendable<SequenceScope<Integer>>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                Sequence<Integer> nested = new Sequence<>(new Suspendable<SequenceScope<Integer>>() {
                    @Override
                    public void run(SequenceScope<Integer> scope) {
                        scope.yield(100);
                        scope.yield(200);
                    }
                });

                scope.yield(0);

                for (Integer integer : nested) {
                    scope.yield(integer);
                }

                scope.yield(300);
            }
        });

        Iterator<Integer> iterator = sequence.iterator();

        assertEquals(0, (int) iterator.next());
        assertEquals(100, (int) iterator.next());
        assertEquals(200, (int) iterator.next());
        assertEquals(300, (int) iterator.next());
    }

    @Test
    public void stream() {
        Sequence<Integer> integers = new Sequence<>(new Suspendable<SequenceScope<Integer>>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                scope.yield(100);
                scope.yield(200);
                scope.yield(300);
            }
        });

        List<Integer> list = StreamSupport.stream(integers.spliterator(), false)
                .map(t -> t * 2)
                .collect(Collectors.toList());

        assertEquals(200, ((int) list.get(0)));
        assertEquals(400, ((int) list.get(2)));
        assertEquals(600, ((int) list.get(3)));
    }
}
