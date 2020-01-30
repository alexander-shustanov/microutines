package microutine;

import microutine.sequence.Sequence;
import microutine.sequence.SequenceScope;
import microutine.sequence.SequenceSuspendable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class YieldTest {
    @Test
    public void testIntSequence() {
        Sequence<Integer> sequence = new Sequence<Integer>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                yield(10);
                yield(20);
                yield(30);
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
        Sequence<Integer> sequence = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                while (!end.get()) {
                    yield(next.get());
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
        Sequence<Integer> sequence = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                Sequence<Integer> nested = new Sequence<>(new SequenceSuspendable<Integer>() {
                    @Override
                    public void run(SequenceScope<Integer> scope) {
                        yield(100);
                        yield(200);
                    }
                });

                yield(0);

                for (Integer integer : nested) {
                    yield(integer);
                }

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
    public void stream() {
        Sequence<Integer> integers = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                yield(100);
                yield(200);
                yield(300);
            }
        });

        List<Integer> list = StreamSupport.stream(integers.spliterator(), false)
                .map(t -> t * 2)
                .collect(Collectors.toList());

        assertEquals(200, ((int) list.get(0)));
        assertEquals(400, ((int) list.get(1)));
        assertEquals(600, ((int) list.get(2)));
    }

    @Test
    public void sequenceSuspendable() {
        Sequence<Integer> sequence = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                yield(100);
                yield(200);
                yield(300);
            }
        });

        List<Integer> list = new ArrayList<>();
        for (Integer integer : sequence) {
            list.add(integer);
        }

        assertEquals(100, (int) list.get(0));
        assertEquals(200, (int) list.get(1));
        assertEquals(300, (int) list.get(2));
    }

    @Test
    public void fibonacci() {
        Sequence<Integer> sequence = new Sequence<>(new SequenceSuspendable<Integer>() {
            @Override
            public void run(SequenceScope<Integer> scope) {
                yield(1);
                yield(1);

                int a = 1;
                int b = 1;

                while (true) {
                    b += a;
                    yield(b);

                    a += b;
                    yield(a);
                }
            }
        });

        //noinspection OptionalGetWithoutIsPresent
        Integer tenthFibonacci = StreamSupport.stream(sequence.spliterator(), false)
                .skip(9).findFirst().get();

        assertEquals(55, ((int) tenthFibonacci));
    }
}
