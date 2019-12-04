package joroutine;

public abstract class SuspendableWithResult<C extends Context, R> {
    abstract public R run(C context);
}
