package joroutine;

import java.lang.reflect.Field;

public class Magic {
    public static final String SCOPE = "scope$S";

    static <C extends Scope, R> Continuation<R> create(Suspendable<C> suspendable, C context) {
        try {
            Field contextField = suspendable.getClass().getDeclaredField(SCOPE);
            contextField.setAccessible(true);
            contextField.set(suspendable, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return getContinuation(suspendable);
    }


    public static <R, C extends Scope> Continuation<R> getContinuation(Suspendable<C> suspendable) {
        return ((Continuation<R>) suspendable);
    }

    public static Context getContext(Suspendable suspendable) {
        try {
            Field contextField = suspendable.getClass().getDeclaredField(SCOPE);
            return (Context) contextField.get(suspendable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
