package microutine.core;

public abstract class SuspendableWithResult<C extends CoroutineScope, R> {
    abstract public R run(C scope);
}
