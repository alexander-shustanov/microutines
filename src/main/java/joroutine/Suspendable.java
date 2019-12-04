package joroutine;

public abstract class Suspendable<C extends Scope> {
    abstract public void run(C scope);
}


