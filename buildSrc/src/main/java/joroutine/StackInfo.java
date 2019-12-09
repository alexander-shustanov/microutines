package joroutine;

import java.util.Collections;
import java.util.List;

/**
 * Represents stack info before suspend.
 */
public class StackInfo {
    final int index;
    private final List<Object> variableTypes;
    private List<Object> stackTypes;

    public StackInfo(int index, List<Object> variableTypes, List<Object> stackTypes) {
        this.index = index;
        this.variableTypes = variableTypes;
        this.stackTypes = stackTypes;
    }

    public List<Object> getVariableTypes() {
        return Collections.unmodifiableList(variableTypes);
    }

    public List<Object> getStackTypes() {
        return Collections.unmodifiableList(stackTypes);
    }
}
