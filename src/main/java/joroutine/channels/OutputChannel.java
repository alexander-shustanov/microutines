package joroutine.channels;

import joroutine.core.Suspend;

public interface OutputChannel<T> {
    @Suspend
    void put(T t);
}
