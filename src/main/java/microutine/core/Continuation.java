package microutine.core;

import java.lang.reflect.Field;

public interface Continuation<T> extends CoroutineContext.ContextElement<Continuation<T>> {
    CoroutineContext.ElementKey<? extends Continuation<?>> KEY = new CoroutineContext.ElementKey<>("Continuation");

    Object SUSPEND = new Object() {
        @Override
        public String toString() {
            return "[SUSPEND]";
        }
    };

    static <R> R getSuspend() {
        //noinspection unchecked
        return (R) SUSPEND;
    }

    T run(Object resumeWith);

    default boolean isDone() {
        try {
            Field maxLabel = getClass().getDeclaredField("maxLabel$S$S");
            maxLabel.setAccessible(true);
            Field label = getClass().getDeclaredField("label$S$S");
            label.setAccessible(true);

            Integer l = (Integer) label.get(this);
            Integer m = (Integer) maxLabel.get(this);
            return l >= m;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new FatalCoroutineException(e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static <T> Continuation<T> getCurrent() {
        return CoroutineContext.getCurrent().getElement((CoroutineContext.ElementKey<? extends Continuation>) KEY);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    default CoroutineContext.ElementKey<? extends Continuation<T>> getKey() {
        return (CoroutineContext.ElementKey<? extends Continuation<T>>) KEY;
    }
}
