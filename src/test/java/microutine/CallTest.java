package microutine;

import microutine.core.SuspendableWithResult;
import microutine.coroutine.AsyncScope;
import microutine.coroutine.AsyncSuspendable;
import org.junit.Test;

public class CallTest {
    @Test
    public void name() {
        AsyncScope.runBlocking(new AsyncSuspendable() {
            @Override
            public void run(AsyncScope scope) {
                scope.call(sayHello());
                Integer integer = scope.call(getInteger());
                System.out.println("World");
                System.out.println(integer);
            }
        });
    }

    private SuspendableWithResult<AsyncScope, Integer> getInteger() {
        return new SuspendableWithResult<AsyncScope, Integer>() {
            @Override
            public Integer run(AsyncScope scope) {
                return 10;
            }
        };
    }

    private AsyncSuspendable sayHello() {
        return new AsyncSuspendable() {
            @Override
            public void run(AsyncScope scope) {
                scope.delay(1000);
                System.out.println("Hello");
            }
        };
    }
}
