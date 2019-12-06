package joroutine;

import java.util.concurrent.CountDownLatch;

public class BlockingContext extends Context {
    public static final BlockingContext INSTANCE = new BlockingContext();

    private BlockingContext() {
        super(Dispatchers.DEFAULT);
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public void launch(Suspendable suspendable) {
        if (Context.getCurrent() != EMPTY) {
            throw new RuntimeException("Blocking context inside blocking context");
        }

        CountDownLatch latch = new CountDownLatch(1);
        super.launch(suspendable, latch::countDown);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
