package test;

import joroutine.Sequence;

public class SequenceTest {
    public static void main(String[] args) {
        Sequence<Integer> sequence = new Sequence<>(new IntegerSequence());

        for (Integer integer : sequence) {
            System.out.println(integer);
        }
    }
}
