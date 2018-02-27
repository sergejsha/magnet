package magnet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import magnet.internal.Factory;
import magnet.internal.Range;

final class MagnetImplementationManager implements ImplementationManager {

    private static final String DEFAULT_TARGET = "";

    private Factory[] factories;
    private Map<Class, Object> index;

    MagnetImplementationManager() {
        registerImplementations();
    }

    private void registerImplementations() {
        try {
            Class<?> magnetClass = Class.forName("magnet.MagnetIndexer");
            Method registerFactories = magnetClass.getMethod("register", MagnetImplementationManager.class);
            registerFactories.invoke(magnetClass, this);
        } catch (Exception e) {
            System.out.println(
                    "MagnetIndexer.class cannot be found. " +
                            "Add a @MagnetizeImplementations-annotated class to the application module.");
        }
    }

    // called by generated index class
    void register(Factory[] factories, Map<Class, Object> index) {
        this.factories = factories;
        this.index = index;
    }

    @Override
    public <T> List<T> get(Class<T> forType, DependencyScope dependencyScope) {
        return get(forType, DEFAULT_TARGET, dependencyScope);
    }

    @Override
    public <T> List<T> get(Class<T> forType, String forTarget, DependencyScope dependencyScope) {
        Object indexed = index.get(forType);

        if (indexed instanceof Range) {
            Range range = (Range) indexed;
            if (range.getTarget().equals(forTarget)) {
                return createFromRange(range, dependencyScope);
            }
            return Collections.emptyList();
        }

        if (indexed instanceof Map) {
            //noinspection unchecked
            Map<String, Range> ranges = (Map<String, Range>) indexed;
            Range range = ranges.get(forTarget);
            if (range != null) {
                return createFromRange(range, dependencyScope);
            }
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    private <T> List<T> createFromRange(Range range, DependencyScope dependencyScope) {
        List<T> impls = new ArrayList<>();
        for (int i = range.getFrom(), to = range.getFrom() + range.getCount(); i < to; i++) {
            //noinspection unchecked
            impls.add((T) factories[i].create(dependencyScope));
        }
        return impls;
    }

}
