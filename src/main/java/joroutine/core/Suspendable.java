package joroutine.core;

import joroutine.core.Scope;

public abstract class Suspendable<C extends Scope> {
    abstract public void run(C scope);
}


