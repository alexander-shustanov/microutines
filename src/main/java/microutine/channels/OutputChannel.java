package microutine.channels;

import microutine.core.Suspend;

public interface OutputChannel<T> {
    @Suspend
    void put(T t);
}
