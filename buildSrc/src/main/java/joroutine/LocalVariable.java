package joroutine;

public class LocalVariable {
    final String name;
    final int index;
    final String descriptor;

    public LocalVariable(String name, int index, String descriptor) {
        this.name = name;
        this.index = index;
        this.descriptor = descriptor;
    }
}
