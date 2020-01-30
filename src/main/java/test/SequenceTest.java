package test;

import microutine.coroutine.CoroutineScope;

public class SequenceTest {
    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {
        CoroutineScope.runBlocking(new AwaitTest());
        System.exit(0);
    }
}
