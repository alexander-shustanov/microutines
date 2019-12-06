package joroutine;

import java.lang.reflect.Field;

public class Magic {
    public static final String SCOPE = "scope$S";

    static <C extends Scope, R> Continuation<R> createContinuation(Suspendable<C> suspendable, C scope) {
        try {
            Field contextField = suspendable.getClass().getDeclaredField(SCOPE);
            contextField.setAccessible(true);

            if (contextField.get(suspendable) != null)
                throw new IllegalArgumentException("Continuation already created");

            contextField.set(suspendable, scope);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Continuation<R> continuation = getContinuation(suspendable);

        if (scope instanceof ScopeImpl) {
            ((ScopeImpl) scope).continuation = continuation;
        }
        return continuation;
    }


    public static <R, C extends Scope> Continuation<R> getContinuation(Suspendable<C> suspendable) {
        return ((Continuation<R>) suspendable);
    }

    public static Context getScope(Suspendable suspendable) {
        try {
            Field contextField = suspendable.getClass().getDeclaredField(SCOPE);
            return (Context) contextField.get(suspendable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
