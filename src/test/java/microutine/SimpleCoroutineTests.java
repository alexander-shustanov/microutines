package microutine;

import microutine.core.SuspendableWithResult;
import microutine.coroutine.AsyncScope;
import microutine.coroutine.AsyncSuspendable;
import microutine.coroutine.Deferred;
import org.junit.Test;

import static microutine.coroutine.AsyncScope.GLOBAL_SCOPE;
import static microutine.coroutine.AsyncScope.runBlocking;

public class SimpleCoroutineTests {
    @Test
    public void helloWorld() throws Exception {
        GLOBAL_SCOPE.launch(new AsyncSuspendable() {
            @Override
            public void run(AsyncScope scope) {
                scope.delay(1000);
                System.out.println("Hello");
            }
        });

        Thread.sleep(1000);
        System.out.println("World");
    }

    @Test
    public void globalScopeAwait() {
        runBlocking(new AsyncSuspendable() {
            @Override
            public void run(AsyncScope scope) {
                Deferred<String> result = scope.async(new SuspendableWithResult<AsyncScope, String>() {
                    @Override
                    public String run(AsyncScope scope) {
                        return "Alex";
                    }
                });

                System.out.println(result.await());
            }
        });
    }
}
