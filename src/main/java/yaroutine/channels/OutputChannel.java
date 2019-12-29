package yaroutine.channels;

import yaroutine.core.Suspend;

public interface OutputChannel<T> {
    @Suspend
    void put(T t);
}
