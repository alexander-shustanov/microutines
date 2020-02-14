package microutine;

import microutine.core.CoroutineContext;
import microutine.core.Dispatcher;
import microutine.core.SuspendableWithResult;
import microutine.coroutine.AsyncScope;
import microutine.coroutine.Dispatchers;
import org.junit.Assert;
import org.junit.Test;

public class SeveralDispatchers {
    @Test
    public void test() {
        String result = AsyncScope.runBlocking(new SuspendableWithResult<AsyncScope, String>() {
            @Override
            public String run(AsyncScope scope) {
                return scope.async(
                        CoroutineContext.getCurrent().with(Dispatchers.IO),
                        new SuspendableWithResult<AsyncScope, String>() {
                            @Override
                            public String run(AsyncScope scope) {
                                return CoroutineContext.getCurrent().getElement(Dispatcher.KEY).getName();
                            }
                        }
                ).await();
            }
        });

        Assert.assertEquals(Dispatchers.IO.getName(), result);
    }
}
