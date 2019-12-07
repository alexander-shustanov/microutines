package joroutine;

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
            }
        });

        Assert.assertEquals(million, counter.get());
    }
}
