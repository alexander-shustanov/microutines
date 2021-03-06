package microutine;

import java.util.Arrays;
import java.util.Objects;

public class MethodId {
    private final int access;
    private final String name;
    private final String descriptor;
    private final String signature;
    private final String[] exceptions;

    public MethodId(int access, String name, String descriptor, String signature, String[] exceptions) {
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.exceptions = exceptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodId methodId = (MethodId) o;
        return access == methodId.access &&
                Objects.equals(name, methodId.name) &&
                Objects.equals(descriptor, methodId.descriptor) &&
                Objects.equals(signature, methodId.signature) &&
                Arrays.equals(exceptions, methodId.exceptions);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(access, name, descriptor, signature);
        result = 31 * result + Arrays.hashCode(exceptions);
        return result;
    }
}
