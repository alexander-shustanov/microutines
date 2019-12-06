package joroutine;

public abstract class SuspendableWithResult<C extends Scope, R> {
    abstract public R run(C context);
}
