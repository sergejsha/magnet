package magnet;

import java.util.HashMap;
import java.util.Map;

final class MagnetDependencyScope implements DependencyScope {

    private final DependencyScope parent;
    private final Map<Class<?>, Object> dependencies;

    private MagnetDependencyScope(DependencyScope parent) {
        this.parent = parent;
        this.dependencies = new HashMap<>();
    }

    MagnetDependencyScope() {
        this.parent = null;
        this.dependencies = new HashMap<>();
    }

    @Override
    public <T> T get(Class<T> type) {
        Object component = dependencies.get(type);
        if (component == null && parent != null) {
            return parent.get(type);
        }
        //noinspection unchecked
        return (T) component;
    }

    @Override
    public <T> T require(Class<T> type) {
        T component = get(type);
        if (component == null) {
            throw new IllegalStateException(
                    String.format(
                            "Component of type %s must be registered, but it's not.", type));
        }
        return component;
    }

    @Override
    public <T> void register(Class<T> type, T dependency) {
        Object existing = dependencies.put(type, dependency);
        if (existing != null) {
            throw new IllegalStateException(
                    String.format("Dependency of type %s already registered." +
                                    " Existing dependency %s, new dependency %s",
                            type, existing, dependency));
        }
    }

    @Override
    public DependencyScope subscope() {
        return new MagnetDependencyScope(this);
    }

}