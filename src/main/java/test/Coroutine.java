package test;

import joroutine.Context;
import joroutine.Scope;
import joroutine.Suspendable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Coroutine extends Suspendable {
    private int numCoroutines;
    private CountDownLatch latch;


    public Coroutine(int numCoroutines) {
        this.numCoroutines = numCoroutines;
        this.latch = new CountDownLatch(numCoroutines);
    }

    @Override
    public void run(Scope scope) {
//        long start = 0;//System.currentTimeMillis();


        System.out.println("Hello");

        scope.delay(1000);

        AtomicInteger atomicInteger = new AtomicInteger(0);


        for (int i = 0; i < numCoroutines; i++) {
            scope.launch(Context.EMPTY, new Suspendable() {
                @Override
                public void run(Scope scope) {
                    scope.delay(500);
                    atomicInteger.incrementAndGet();
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(atomicInteger.get());

//                scope.launchAwait(Context.EMPTY, new Suspendable() {
//                    @Override
//                    public void run(Scope scope) {
//                        scope.delay(1000);
//                        System.out.println("World");
//                    }
//                });
    }
}
