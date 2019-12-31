package microutine;

import microutine.coroutine.BlockingContext;
import microutine.core.CoroutineContext;
import microutine.coroutine.CoroutineScope;
import microutine.coroutine.CoroutineSuspendable;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCoroutine {
    @Test
    public void millionOfCoroutines() {
        int million = 1_000_000;

        AtomicInteger counter = new AtomicInteger(0);

        BlockingContext.INSTANCE.launch(new CoroutineSuspendable() {
            @Override
            public void run(CoroutineScope scope) {
                long startTime = System.currentTimeMillis();

                CountDownLatch latch = new CountDownLatch(million);

                for (int i = 0; i < million; i++) {
                    CoroutineContext.EMPTY.launch(new CoroutineSuspendable() {
                        @Override
                        public void run(CoroutineScope scope) {
                            scope.delay(500);

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
}
