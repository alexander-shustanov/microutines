package microutine;

import microutine.core.SuspendableWithResult;
import microutine.coroutine.AsyncScope;
import microutine.coroutine.AsyncSuspendable;
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

        AsyncScope.runBlocking(new AsyncSuspendable() {
            @Override
            public void run(AsyncScope scope) {
                long startTime = System.currentTimeMillis();

                CountDownLatch latch = new CountDownLatch(million);

                for (int i = 0; i < million; i++) {
                    scope.launch(new AsyncSuspendable() {
                        @Override
                        public void run(AsyncScope scope) {
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

            AsyncScope.runBlocking(new AsyncSuspendable() {
                @Override
                public void run(AsyncScope scope) {
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

    private SuspendableWithResult<AsyncScope, Long> createIthAsync(int i) {
        return new SuspendableWithResult<AsyncScope, Long>() {
            @Override
            public Long run(AsyncScope scope) {
                scope.delay(1000);
                return ((long) i);
            }
        };
    }
}
