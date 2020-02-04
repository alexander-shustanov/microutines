package test;

import microutine.core.SuspendableWithResult;
import microutine.coroutine.AsyncScope;
import microutine.coroutine.AsyncSuspendable;
import microutine.coroutine.Deferred;

import java.util.concurrent.Future;

public class AwaitTest extends AsyncSuspendable {
    @Override
    public void run(AsyncScope scope) {

        Deferred<Integer> deferred = scope.async(new SuspendableWithResult<AsyncScope, Integer>() {
            @Override
            public Integer run(AsyncScope scope) {
                scope.delay(1000);
                return 100;
            }
        });

        System.out.println(deferred.await());
    }
}
