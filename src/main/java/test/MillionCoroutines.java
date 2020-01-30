package test;

import microutine.coroutine.CoroutineScope;
import microutine.coroutine.CoroutineSuspendable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class MillionCoroutines extends CoroutineSuspendable {
    private int numCoroutines;
    private CountDownLatch latch;

    public MillionCoroutines(int numCoroutines) {
        this.numCoroutines = numCoroutines;
        this.latch = new CountDownLatch(numCoroutines);
    }

    @Override
    public void run(CoroutineScope scope) {
        long start = System.currentTimeMillis();

        System.out.println("Hello");

        scope.delay(1000);

        AtomicInteger atomicInteger = new AtomicInteger(0);

        for (int i = 0; i < numCoroutines; i++) {
            scope.launch(new CoroutineSuspendable() {
                @Override
                public void run(CoroutineScope scope) {
                    scope.delay(500);
                    atomicInteger.incrementAndGet();
                    latch.countDown();
                }
            });
        }

        scope.await(latch);

        scope.delay(3000);

        System.out.println("Atomic int: " + atomicInteger.get());

        System.out.println("Time elapsed: " + (System.currentTimeMillis() - start));

        scope.delay(1000);
        System.out.println("World");
    }
}
