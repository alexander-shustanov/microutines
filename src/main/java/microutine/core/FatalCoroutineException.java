package microutine.core;

public class FatalCoroutineException extends RuntimeException {
    public FatalCoroutineException(Throwable cause) {
        super(cause);
    }
}
