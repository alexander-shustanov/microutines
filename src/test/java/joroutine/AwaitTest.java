package joroutine;

import joroutine.core.SuspendableWithResult;
import joroutine.coroutine.BlockingContext;
import joroutine.coroutine.CoroutineScope;
import joroutine.coroutine.Deferred;
import org.junit.Assert;
import org.junit.Test;

public class AwaitTest {
    @Test
    public void testAwait() {
        int result = BlockingContext.INSTANCE.launch(new SuspendableWithResult<CoroutineScope, Integer>() {
            @Override
            public Integer run(CoroutineScope scope) {
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

                Deferred<Integer> forth = scope.async(new SuspendableWithResult<CoroutineScope, Integer>() {
                    @Override
                    public Integer run(CoroutineScope scope) {
                        scope.delay(500);
                        return 400;
                    }
                });

                Assert.assertEquals(400, ((int) scope.await(forth)));
                return scope.await(first) + scope.await(second) + scope.await(third);
            }
        });

        Assert.assertEquals(700, result);
    }

    @Test
    public void testNestedAwait() {
        int result = BlockingContext.INSTANCE.launch(new SuspendableWithResult<CoroutineScope, Integer>() {
            @Override
            public Integer run(CoroutineScope scope) {
                Deferred<Integer> async = scope.async(new SuspendableWithResult<CoroutineScope, Integer>() {
                    @Override
                    public Integer run(CoroutineScope scope) {
                        scope.delay(100);

                        return scope.await(scope.async(new SuspendableWithResult<CoroutineScope, Integer>() {
                            @Override
                            public Integer run(CoroutineScope scope) {
                                scope.delay(100);
                                return 200;
                            }
                        })) + 100;
                    }
                });
                return scope.await(async) + 100;
            }
        });

        Assert.assertEquals(400, result);
    }
}
