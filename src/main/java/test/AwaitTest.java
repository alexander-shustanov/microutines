package test;

import joroutine.core.SuspendableWithResult;
import joroutine.coroutine.CoroutineScope;
import joroutine.coroutine.CoroutineSuspendable;
import joroutine.coroutine.Deferred;

public class AwaitTest extends CoroutineSuspendable {
    @Override
    public void run(CoroutineScope scope) {

        Deferred<Integer> deferred = scope.async(new SuspendableWithResult<CoroutineScope, Integer>() {
            @Override
            public Integer run(CoroutineScope scope) {
                scope.delay(1000);
                return 100;
            }
        });

        System.out.println(deferred.await());
    }
}
