package joroutine;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SuspendInfo {
    private final List<Mapping> variableMappings;
    private final List<Mapping> stackMappings;

    public SuspendInfo(List<Mapping> mappings, List<Mapping> stackMappings) {
        this.variableMappings = mappings;
        this.stackMappings = stackMappings;
    }

    public List<Mapping> getVariableMappings() {
        return Collections.unmodifiableList(variableMappings);
    }

    public List<Mapping> getStackMappings() {
        return Collections.unmodifiableList(stackMappings);
    }

    static class Mapping {
        final int index;
        final Field field;

        Mapping(int index, Field field) {
            this.index = index;
            this.field = field;
        }
    }

    static class Field {
        final String fieldName;
        final String fieldDescriptor;

        Field(String fieldName, String fieldDescriptor) {
            this.fieldName = fieldName;
            this.fieldDescriptor = fieldDescriptor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Field field = (Field) o;
            return Objects.equals(fieldName, field.fieldName) &&
                    Objects.equals(fieldDescriptor, field.fieldDescriptor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldName, fieldDescriptor);
        }
    }
}
