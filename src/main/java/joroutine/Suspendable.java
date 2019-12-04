package joroutine;

public abstract class Suspendable<C extends Context> {
    abstract public void run(C context);
}


