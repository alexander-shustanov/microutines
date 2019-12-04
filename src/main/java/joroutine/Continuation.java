package joroutine;

public interface Continuation<T> {
    Object SUSPEND = new Object();
    Object END = new Object();

    T run();
}
