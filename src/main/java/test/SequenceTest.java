package test;

import joroutine.BlockingContext;

public class SequenceTest {
    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {

        long start = System.currentTimeMillis();

        System.out.println("Start");
        BlockingContext.INSTANCE.launch(new Coroutine(1000000));

        System.out.println("Time elapsed: " + (System.currentTimeMillis() - start));

        System.exit(0);
    }
}
