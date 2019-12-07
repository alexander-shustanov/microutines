package joroutine;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SuspendInfo {
    private final List<VarToFieldMapping> mappings;

    public SuspendInfo(List<VarToFieldMapping> mappings) {
        this.mappings = mappings;
    }

    public List<VarToFieldMapping> getMappings() {
        return Collections.unmodifiableList(mappings);
    }

    static class VarToFieldMapping {
        final int varIndex;
        final Field field;

        VarToFieldMapping(int varIndex, Field field) {
            this.varIndex = varIndex;
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
