package joroutine;

import java.lang.reflect.Field;

public interface Continuation<T> {
    Object SUSPEND = new Object();
    Object END = new Object();

    T run();

    default boolean isDone() {
        try {
            Field maxLabel = getClass().getDeclaredField("maxLabel$S$S");
            maxLabel.setAccessible(true);
            Field label = getClass().getDeclaredField("label$S$S");
            label.setAccessible(true);

            Integer l = (Integer) label.get(this);
            Integer m = (Integer) maxLabel.get(this);
            if (l >= m) {
                return true;
            }
            return false;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
