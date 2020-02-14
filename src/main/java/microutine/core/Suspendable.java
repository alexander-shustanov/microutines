package microutine.core;

public abstract class Suspendable<C extends CoroutineScope> {
    abstract public void run(C scope);
}


