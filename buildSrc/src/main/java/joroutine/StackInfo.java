package joroutine;

import java.util.Collections;
import java.util.List;

/**
 * Represents stack info before suspend.
 */
public class StackInfo {
    final int index;
    private final List<Object> variableTypes;

    public StackInfo(int index, List<Object> variableTypes) {
        this.index = index;
        this.variableTypes = variableTypes;
    }

    public List<Object> getVariableTypes() {
        return Collections.unmodifiableList(variableTypes);
    }
}
