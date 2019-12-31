package microutine.core;

import java.lang.reflect.Field;

public class Magic {
    public static final String SCOPE = "scope$S";

    public static <C extends Scope, R> Continuation<R> createContinuation(Suspendable<C> suspendable, C scope) {
        return getContinuation(suspendable, scope);
    }

    public static <C extends Scope, R> Continuation<R> createContinuation(SuspendableWithResult<C, R> suspendable, C scope) {
        return getContinuation(suspendable, scope);
    }

    private static <C extends Scope, R> Continuation<R> getContinuation(Object suspendable, C scope) {
        try {
            Field contextField = suspendable.getClass().getDeclaredField(SCOPE);
            contextField.setAccessible(true);

            if (contextField.get(suspendable) != null)
                throw new IllegalArgumentException("Continuation already created");

            contextField.set(suspendable, scope);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return getContinuation(suspendable);
    }

    public static <R, C extends Scope> Continuation<R> getContinuation(Object suspendable) {
        if (getScope(suspendable) == null)
            throw new RuntimeException("No continuation created for specified suspendable");
        //noinspection unchecked
        return ((Continuation<R>) suspendable);
    }

    private static Scope getScope(Object suspendable) {
        try {
            Field contextField = suspendable.getClass().getDeclaredField(SCOPE);
            contextField.setAccessible(true);
            return (Scope) contextField.get(suspendable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
