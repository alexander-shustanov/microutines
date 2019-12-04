package joroutine;

public class BlockingContext extends Context {
    public BlockingContext() {
        super(Dispatcher.IMMEDIATE);
    }
}
