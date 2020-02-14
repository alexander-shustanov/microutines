package microutine.core;

import microutine.coroutine.Dispatchers;

import java.util.HashMap;
import java.util.Map;

public class CoroutineContext {
    private static final ThreadLocal<CoroutineContext> contexts = new ThreadLocal<>();

    public static final CoroutineContext DEFAULT = new CoroutineContext() {
        @Override
        public void set() {
            contexts.remove();
        }
    }.with(Dispatchers.DEFAULT);

    private Map<ElementKey<?>, ContextElement<?>> elements = new HashMap<>();

    public CoroutineContext() {
    }

    public <E extends ContextElement<E>> E getElement(ElementKey<E> key) {
        //noinspection unchecked
        return (E) elements.get(key);
    }

    public <E extends ContextElement<E>> CoroutineContext with(E element) {
        CoroutineContext created = new CoroutineContext();
        created.elements.putAll(elements);
        created.elements.put(element.getKey(), element);
        return created;
    }

    public static CoroutineContext getCurrent() {
        CoroutineContext context = contexts.get();
        if (context == null)
            return DEFAULT;
        return context;
    }

    public void set() {
        contexts.set(this);
    }

    public static class ElementKey<E extends ContextElement<? extends E>> {
        public final String name;

        public ElementKey(String name) {
            this.name = name;
        }
    }

    public interface ContextElement<E extends ContextElement<E>> {
        CoroutineContext.ElementKey<? extends E> getKey();
    }

}
