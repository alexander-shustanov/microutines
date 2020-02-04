package microutine;

import microutine.channels.BlockingChannel;
import microutine.channels.InputChannel;
import microutine.channels.OutputChannel;
import microutine.core.SuspendableWithResult;
import microutine.coroutine.AsyncScope;
import microutine.coroutine.AsyncSuspendable;
import microutine.coroutine.Deferred;
import org.junit.Assert;
import org.junit.Test;

public class ChannelTest {
    @Test
    public void channel() {
        int result = AsyncScope.runBlocking(new SuspendableWithResult<AsyncScope, Integer>() {
            @Override
            public Integer run(AsyncScope scope) {
                BlockingChannel<Integer> channel = new BlockingChannel<>();

                Deferred<Integer> result = scope.async(new SuspendableWithResult<AsyncScope, Integer>() {
                    @Override
                    public Integer run(AsyncScope scope) {
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

                scope.launch(new AsyncSuspendable() {
                    @Override
                    public void run(AsyncScope scope) {
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
