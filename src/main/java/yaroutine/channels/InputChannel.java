package yaroutine.channels;

import yaroutine.core.Suspend;

public interface InputChannel<T> {
    @Suspend
    T next();
}
