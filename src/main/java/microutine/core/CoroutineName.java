package microutine.core;

public class CoroutineName implements CoroutineContext.ContextElement<CoroutineName> {
    public static final CoroutineContext.ElementKey<CoroutineName> KEY = new CoroutineContext.ElementKey<>("CoroutineName");

    private final String name;

    public CoroutineName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public CoroutineContext.ElementKey<CoroutineName> getKey() {
        return KEY;
    }
}
