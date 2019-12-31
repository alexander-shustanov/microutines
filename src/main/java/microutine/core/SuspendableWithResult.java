package microutine.core;

public abstract class SuspendableWithResult<C extends Scope, R> {
    abstract public R run(C scope);
}
