package yaroutine;

import yaroutine.channels.BlockingChannel;
import yaroutine.channels.InputChannel;
import yaroutine.channels.OutputChannel;
import yaroutine.core.SuspendableWithResult;
import yaroutine.coroutine.BlockingContext;
import yaroutine.coroutine.CoroutineScope;
import yaroutine.coroutine.CoroutineSuspendable;
import yaroutine.coroutine.Deferred;
import org.junit.Assert;
import org.junit.Test;

public class ChannelTest {
    @Test
    public void channel() {
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
    }
}
