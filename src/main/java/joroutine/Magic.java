package joroutine;

import java.lang.reflect.Field;

public class Magic {
    public static final Object SUSPEND = new Object();
    public static final String CONTEXT = "context$S";

    static <C extends Context, R> Continuation<R> create(Suspendable<C> suspendable, C context) {
        try {
            Field contextField = suspendable.getClass().getDeclaredField(CONTEXT);
            contextField.setAccessible(true);
            contextField.set(suspendable, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return getContinuation(suspendable);
    }


    private static <R, C extends Context> Continuation<R> getContinuation(Suspendable<C> suspendable) {
        return ((Continuation<R>) suspendable);
    }

    public static Context getContext(Suspendable suspendable) {
        try {
            Field contextField = suspendable.getClass().getDeclaredField(CONTEXT);
            return (Context) contextField.get(suspendable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
