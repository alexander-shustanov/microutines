package microutine;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Example implementation of lazy generation with plain java.
 */
public class DummySequenceTest {

    @Test
    public void dummySequenceTest() {
        DummySequence<Integer> sequence = DummySequence.first(() -> {
            final int i = 10;
            return DummySequence.next(10, () -> {
                final int i1 = i + 10;
                return DummySequence.next(i1, () -> DummySequence.end(i1 + 10));
            });
        });

        List<Integer> list = StreamSupport.stream(sequence.spliterator(), false)
                .collect(Collectors.toList());

        Assert.assertEquals(10, ((int) list.get(0)));
        Assert.assertEquals(20, ((int) list.get(1)));
        Assert.assertEquals(30, ((int) list.get(2)));
    }

    private static class DummySequence<T> implements Iterable<T>, Iterator<T> {
        private Step<T> step;

        public DummySequence(Step<T> step) {
            this.step = step;
        }

        @Override
        public Iterator<T> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            if (step instanceof EndStep)
                return false;
            step = step.nextStep();
            return true;
        }

        @Override
        public T next() {
            return step.getValue();
        }

        public static <T> DummySequence<T> first(Supplier<Step<T>> next) {
            return new DummySequence<>(new FirstStep<T>(next));
        }

        public static <T> Step<T> next(T value, Supplier<Step<T>> next) {
            return new IntermediateStep<>(value, next);
        }

        public static <T> Step<T> end(T value) {
            return new EndStep<>(value);
        }
    }

    private interface Step<T> {
        T getValue();

        Step<T> nextStep();
    }

    public static class FirstStep<T> implements Step<T> {
        Supplier<Step<T>> nextStep;

        public FirstStep(Supplier<Step<T>> next) {
            this.nextStep = next;
        }

        @Override
        public T getValue() {
            throw new IllegalStateException();
        }

        @Override
        public Step<T> nextStep() {
            return nextStep.get();
        }
    }

    public static class IntermediateStep<T> implements Step<T> {
        T value;
        Supplier<Step<T>> nextStep;

        public IntermediateStep(T value, Supplier<Step<T>> nextStep) {
            this.value = value;
            this.nextStep = nextStep;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public Step<T> nextStep() {
            return nextStep.get();
        }
    }

    public static class EndStep<T> implements Step<T> {
        T value;

        public EndStep(T value) {
            this.value = value;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public Step<T> nextStep() {
            throw new IllegalStateException();
        }
    }
}