package joroutine.core;

import joroutine.core.Scope;

public abstract class SuspendableWithResult<C extends Scope, R> {
    abstract public R run(C scope);
}
