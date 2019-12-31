package test;

import microutine.coroutine.BlockingContext;

public class SequenceTest {
    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {
//        BlockingContext.INSTANCE.launch(new MilionCoroutines(2_000_000));


        BlockingContext.INSTANCE.launch(new AwaitTest());

        System.exit(0);
    }
}
