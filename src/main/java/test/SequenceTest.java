package test;

import joroutine.BlockingContext;

public class SequenceTest {
    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {
        BlockingContext.INSTANCE.launch(new Coroutine(2_000_000));
        System.exit(0);
    }
}
