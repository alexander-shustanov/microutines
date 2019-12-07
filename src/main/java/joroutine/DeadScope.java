package joroutine;

import java.util.concurrent.CountDownLatch;

public class DeadScope implements CoroutineScope {
    @Override
    public void await(CountDownLatch latch) {
        throw new RuntimeException();
    }
}
