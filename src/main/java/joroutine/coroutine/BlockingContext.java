package joroutine.coroutine;

import java.util.concurrent.CountDownLatch;

public class BlockingContext extends CoroutineContext {
    public static final BlockingContext INSTANCE = new BlockingContext();

    private BlockingContext() {
        super(Dispatchers.DEFAULT);
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public void launch(CoroutineSuspendable suspendable) {
        if (CoroutineContext.getCurrent() == this) {
            throw new RuntimeException("Blocking context inside blocking context");
        }

        CountDownLatch latch = new CountDownLatch(1);
        CoroutineContext.EMPTY.launch(suspendable, latch::countDown);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
