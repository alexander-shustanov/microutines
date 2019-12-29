package joroutine.channels;

import joroutine.core.Suspend;

public interface InputChannel<T> {
    @Suspend
    T next();
}
