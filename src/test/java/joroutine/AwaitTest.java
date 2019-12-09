package joroutine;

import joroutine.core.SuspendableWithResult;
import joroutine.coroutine.BlockingContext;
import joroutine.coroutine.CoroutineScope;
import joroutine.coroutine.CoroutineSuspendable;
import joroutine.coroutine.Deferred;
import org.junit.Assert;
import org.junit.Test;

public class AwaitTest {
    @Test
    public void testAwait() {
        BlockingContext.getCurrent().launch(new CoroutineSuspendable() {
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

                Assert.assertEquals(300, scope.await(first) + scope.await(second));
            }
        });
    }
}
