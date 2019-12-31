# Microutines

<p align="center">
<a href="http://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat" alt="license" title=""></a>
<a href="https://travis-ci.org/alexander-shustanov/yaroutines"><img src="https://travis-ci.org/alexander-shustanov/yaroutines.svg?branch=master" alt="Build Status" title=""></a>
</p>

Simple project that introduces Continuations to Java, and as a result - Coroutines, lazy generators and channels. The purpose of the project is to show, how all these scary things works under the hood.

### Sequences (Lazy Generators)

```java
Sequence<Integer> sequence = new Sequence<>(new SequenceSuspendable<Integer>() {
    @Override
    public void run(SequenceScope<Integer> scope) {
        yield(1);
        yield(1);
        int cur = 1;
        int prev = 1;
        while (true) {
            int tmp = prev;
            prev = cur;
            cur += tmp;
            yield(cur);
        }
    }
});

//noinspection OptionalGetWithoutIsPresent
Integer tenthFibonacci = Stream.generate(sequence.iterator()::next)
        .skip(9).findFirst().get();

assertEquals(55, ((int) tenthFibonacci));
```

### Coroutines

```java
int million = 1_000_000;

AtomicInteger counter = new AtomicInteger(0);

BlockingContext.INSTANCE.launch(new CoroutineSuspendable() {
    @Override
    public void run(CoroutineScope scope) {
        long startTime = System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(million);

        for (int i = 0; i < million; i++) {
            CoroutineContext.EMPTY.launch(new CoroutineSuspendable() {
                @Override
                public void run(CoroutineScope scope) {
                    scope.delay(500);

                    counter.incrementAndGet();
                    latch.countDown();
                }
            });
        }

        scope.await(latch);

        System.out.println(System.currentTimeMillis() - startTime);
    }
});

Assert.assertEquals(million, counter.get());
```

### Channels

```java
int result = BlockingContext.INSTANCE.launch(new SuspendableWithResult<CoroutineScope, Integer>() {
    @Override
    public Integer run(CoroutineScope scope) {
        BlockingChannel<Integer> channel = new BlockingChannel<>();

        Deferred<Integer> result = scope.async(new SuspendableWithResult<CoroutineScope, Integer>() {
            @Override
            public Integer run(CoroutineScope scope) {
                InputChannel<Integer> inputChannel = channel.getInputChannel();

                Integer state = 0;
                for (int i = 0; i < 10; i++) {
                    Integer received = inputChannel.next();
                    System.out.println("Received: " + received + ". Thread: " + Thread.currentThread().getName());
                    state += received;
                }
                return state;
            }
        });

        scope.launch(new CoroutineSuspendable() {
            @Override
            public void run(CoroutineScope scope) {
                OutputChannel<Integer> outputChannel = channel.getOutputChannel();

                for (int i = 0; i < 10; i++) {
                    System.out.println("Sent: " + i + ". Thread: " + Thread.currentThread().getName());
                    outputChannel.put(i);
                }
            }
        });

        return result.await();
    }
});

Assert.assertEquals(45, result);
```
