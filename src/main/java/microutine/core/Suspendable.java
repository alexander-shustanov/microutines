package microutine.core;

import java.io.IOException;

public abstract class Suspendable<C extends CoroutineScope> {
    abstract public void run(C scope) throws IOException;
}


