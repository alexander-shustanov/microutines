package microutine;

import microutine.core.*;
import org.junit.Test;

@SuppressWarnings("rawtypes")
public class ContinuationTest {

    @Test
    public void testNoException() {
        Suspendable suspendable = new Suspendable() {
            @Override
            public void run(CoroutineScope scope) {
            }
        };

        Continuation continuation = Magic.createContinuation(suspendable, new CoroutineScope() {
        });

        continuation.run(null);
    }

    @Test(expected = ContinuationEndException.class)
    public void testEndException() {
        Suspendable suspendable = new Suspendable() {
            @Override
            public void run(CoroutineScope scope) {
            }
        };

        Continuation continuation = Magic.createContinuation(suspendable, new CoroutineScope() {
        });

        continuation.run(null);
        continuation.run(null);
    }
}
