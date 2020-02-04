package test;

import microutine.coroutine.AsyncScope;

public class SequenceTest {
    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {
        AsyncScope.runBlocking(new AwaitTest());
        System.exit(0);
    }
}
