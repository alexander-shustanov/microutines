package microutine;

import microutine.core.SuspendableWithResult;
import microutine.coroutine.CoroutineScope;
import microutine.coroutine.CoroutineSuspendable;
import microutine.coroutine.Deferred;
import org.junit.Assert;
import org.junit.Test;

import static microutine.coroutine.CoroutineScope.runBlocking;

public class AwaitTest {
    @Test
    public void testAwait() {
        int result = runBlocking(new SuspendableWithResult<CoroutineScope, Integer>() {
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

                Assert.assertEquals(400, ((int) forth.await()));
                return first.await() + second.await() + third.await();
            }
        });

        Assert.assertEquals(700, result);
    }

    @Test
    public void testNestedAwait() {
        int result = runBlocking(new SuspendableWithResult<CoroutineScope, Integer>() {
            @Override
            public Integer run(CoroutineScope scope) {
                Deferred<Integer> async = scope.async(new SuspendableWithResult<CoroutineScope, Integer>() {
                    @Override
                    public Integer run(CoroutineScope scope) {
                        scope.delay(100);

                        return scope.async(new SuspendableWithResult<CoroutineScope, Integer>() {
                            @Override
                            public Integer run(CoroutineScope scope) {
                                scope.delay(100);
                                return 200;
                            }
                        }).await() + 100;
                    }
                });
                return async.await() + 100;
            }
        });

        Assert.assertEquals(400, result);
    }

    @Test
    public void singleInstruction() {
        int integer = runBlocking(new SuspendableWithResult<CoroutineScope, Integer>() {
            @Override
            public Integer run(CoroutineScope scope) {
                return scope.async(new SuspendableWithResult<CoroutineScope, Integer>() {
                    @Override
                    public Integer run(CoroutineScope scope) {
                        return 100;
                    }
                }).await();
            }
        });

        Assert.assertEquals(100, integer);
    }

    @Test
    public void orderTest() {
        runBlocking(new CoroutineSuspendable() {
            @Override
            public void run(CoroutineScope scope) {
                Deferred<String> customer = scope.async(new SuspendableWithResult<CoroutineScope, String>() {
                    @Override
                    public String run(CoroutineScope scope) {
                        scope.delay(1000);
                        return "Alex";
                    }
                });

                Deferred<String> product = scope.async(new SuspendableWithResult<CoroutineScope, String>() {
                    @Override
                    public String run(CoroutineScope scope) {
                        scope.delay(1000);
                        return "Bread";
                    }
                });

                System.out.println(customer.await() + " orders " + product.await());
            }
        });
    }
}
