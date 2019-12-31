package microutine.coroutine;

import microutine.core.CoroutineContext;
import microutine.core.SuspendableWithResult;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class BlockingContext extends CoroutineContext {
    public static final BlockingContext INSTANCE = new BlockingContext();

    private BlockingContext() {
        super(Dispatchers.DEFAULT);
    }

    public <R> R launch(SuspendableWithResult<CoroutineScope, R> suspendable) {
        if (CoroutineContext.getCurrent() == this) {
            throw new RuntimeException("Blocking context inside blocking context");
        }

        AtomicReference<R> result = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(1);
        CoroutineContext.EMPTY.launch(suspendable, r -> {
            result.set(r);
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return result.get();
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
