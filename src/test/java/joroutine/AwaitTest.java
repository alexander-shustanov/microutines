package joroutine;

import joroutine.core.SuspendableWithResult;
import joroutine.coroutine.BlockingContext;
import joroutine.coroutine.CoroutineScope;
import joroutine.coroutine.CoroutineSuspendable;
import joroutine.coroutine.Deferred;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class AwaitTest {
    @Test
    public void testAwait() {
        AtomicInteger result = new AtomicInteger(); // for now, launch from blocking context can not return the result.

        BlockingContext.INSTANCE.launch(new CoroutineSuspendable() {
            @Override
            public void run(CoroutineScope scope) {
                Deferred<Integer> first = scope.async(new SuspendableWithResult<CoroutineScope, Integer>() {
                    @Override
                    public Integer run(CoroutineScope scope) {
                        scope.delay(1000);
                        return 100;
                    }
                });

                Deferred<Integer> second = scope.async(new SuspendableWithResult<CoroutineScope, Integer>() {
                    @Override
                    public Integer run(CoroutineScope scope) {
                        scope.delay(500);
                        return 200;
                    }
                });

                Deferred<Integer> third = scope.async(new SuspendableWithResult<CoroutineScope, Integer>() {
                    @Override
                    public Integer run(CoroutineScope scope) {
                        scope.delay(500);
                        return 400;
                    }
                });

                result.set(scope.await(first) + scope.await(second) + scope.await(third));
            }
        });

        Assert.assertEquals(700, result.get());
    }
}
