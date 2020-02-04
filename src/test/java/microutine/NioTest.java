package microutine;

import microutine.coroutine.AsyncScope;
import microutine.coroutine.AsyncSuspendable;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;

public class NioTest {
    @Test
    public void testNio() {
        AsyncScope.runBlocking(new AsyncSuspendable() {
            @Override
            public void run(AsyncScope scope) throws IOException {
                AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get(""));
//                channel.read()
            }
        });
    }
}
