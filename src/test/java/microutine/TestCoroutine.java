package microutine;

import microutine.core.SuspendableWithResult;
import microutine.coroutine.CoroutineScope;
import microutine.coroutine.CoroutineSuspendable;
import microutine.coroutine.Deferred;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCoroutine {
    @Test
    public void millionOfCoroutines() {
        int million = 1_000_000;

        AtomicInteger counter = new AtomicInteger(0);

        CoroutineScope.runBlocking(new CoroutineSuspendable() {
            @Override
            public void run(CoroutineScope scope) {
                long startTime = System.currentTimeMillis();

                CountDownLatch latch = new CountDownLatch(million);

                for (int i = 0; i < million; i++) {
                    scope.launch(new CoroutineSuspendable() {
                        @Override
                        public void run(CoroutineScope scope) {
                            scope.delay(1000);

                            counter.incrementAndGet();
                            latch.countDown();
                        }
                    });
                }

                scope.await(latch);

                System.out.println(System.currentTimeMillis() - startTime);
            }
        });

        Assert.assertEquals(million, counter.get());
    }

    @Test
    public void millionDeferred() {
        for (int i = 0; i < 10; i++) {

            int million = 1_000_000;

            CoroutineScope.runBlocking(new CoroutineSuspendable() {
                @Override
                public void run(CoroutineScope scope) {
                    long startTime = System.currentTimeMillis();
                    long sum = 0;

                    List<Deferred<Long>> deferredValues = new ArrayList<>();

                    for (int i = 1; i <= million; i++) {
                        deferredValues.add(scope.async(createIthAsync(i)));
                    }

                    for (Deferred<Long> deferredValue : deferredValues) {
                        sum += deferredValue.await();
                    }

                    System.out.println(sum);

                    System.out.println(System.currentTimeMillis() - startTime);
                }
            });
        }
    }

    private SuspendableWithResult<CoroutineScope, Long> createIthAsync(int i) {
        return new SuspendableWithResult<CoroutineScope, Long>() {
            @Override
            public Long run(CoroutineScope scope) {
                scope.delay(1000);
                return ((long) i);
            }
        };
    }
}
