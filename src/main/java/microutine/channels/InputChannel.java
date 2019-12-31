package microutine.channels;

import microutine.core.Suspend;

public interface InputChannel<T> {
    @Suspend
    T next();
}
