import joroutine.Sequence;
import joroutine.Suspendable;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SequenceTest extends TestCase {
    public void testIntSequence() {
        Sequence<Integer> sequence = new Sequence<Integer>(new Suspendable<Sequence.SequenceContext<Integer>>() {
            @Override
            public void run(Sequence.SequenceContext<Integer> context) {
                context.yield(10);
                context.yield(20);
                context.yield(30);
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

    public void testOuterData() {
        AtomicInteger next = new AtomicInteger(100);
        AtomicBoolean end = new AtomicBoolean(false);
        Sequence<Integer> sequence = new Sequence<>(new Suspendable<Sequence.SequenceContext<Integer>>() {
            @Override
            public void run(Sequence.SequenceContext<Integer> context) {
                while (!end.get()) {
                    context.yield(next.get());
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

    public void testNested() {
        Sequence<Integer> sequence = new Sequence<>(new Suspendable<Sequence.SequenceContext<Integer>>() {
            @Override
            public void run(Sequence.SequenceContext<Integer> context) {
                Sequence<Integer> nested = new Sequence<>(new Suspendable<Sequence.SequenceContext<Integer>>() {
                    @Override
                    public void run(Sequence.SequenceContext<Integer> context) {
                        context.yield(100);
                        context.yield(200);
                    }
                });

                context.yield(0);

                for (Integer integer : nested) {
                    context.yield(integer);
                }

                context.yield(300);
            }
        });

        Iterator<Integer> iterator = sequence.iterator();

        assertEquals(0, (int) iterator.next());
        assertEquals(100, (int) iterator.next());
        assertEquals(200, (int) iterator.next());
        assertEquals(300, (int) iterator.next());
    }
}
