package magnet.internal;

import magnet.Scope;

import java.util.Collections;
import java.util.List;

/** Used for testing MagnetScopeContainer. */
public class InstrumentedInstanceScope implements Scope, FactoryFilter {

    private final MagnetScope scope;

    public InstrumentedInstanceScope(MagnetScope scope) {
        this.scope = scope;
    }

    @Override public <T> T getOptional(Class<T> type) {
        return scope.getOptional(type);
    }

    @Override public <T> T getOptional(Class<T> type, String classifier) {
        return scope.getOptional(type, classifier);
    }

    @Override public <T> T getSingle(Class<T> type) {
        return scope.getSingle(type);
    }

    @Override public <T> T getSingle(Class<T> type, String classifier) {
        return scope.getSingle(type, classifier);
    }

    @Override public <T> List<T> getMany(Class<T> type) {
        return scope.getMany(type);
    }

    @Override public <T> List<T> getMany(Class<T> type, String classifier) {
        return scope.getMany(type, classifier);
    }

    @Override public <T> Scope bind(Class<T> type, T instance) {
        scope.bind(type, instance);
        return this;
    }

    @Override public <T> Scope bind(Class<T> type, T instance, String classifier) {
        scope.bind(type, instance, classifier);
        return this;
    }

    @Override public Scope createSubscope() {
        return new InstrumentedInstanceScope((MagnetScope) scope.createSubscope());
    }
    @Override public void dispose() {
        scope.dispose();
    }

    /*
    @Override public <T> RuntimeInstance<T> findDeepInstance(String key) {
        return scope.findDeepInstance(key);
    }

    @Override public void registerInstanceInScope(String key, RuntimeInstance instance) {
        scope.registerInstanceInScope(key, instance);
    }
    */

    @Override public boolean filter(InstanceFactory factory) { return scope.filter(factory); }

    /** Returns and object registered right in this scope or null if no object was registered. */
    @SuppressWarnings("unchecked") <T> T getOptionalInScope(Class<T> type, String classifier) {
        RuntimeInstance<T> instance = scope.instances.get(MagnetScope.key(type, classifier));
        return instance == null ? null : instance.getValue();
    }

    /** Returns list of objects registered right in this scope. */
    @SuppressWarnings("unchecked") <T> List<T> getManyInScope(Class<T> type, String classifier) {
        RuntimeInstance<T> instance = scope.instances.get(MagnetScope.key(type, classifier));
        return instance == null ? Collections.emptyList() : instance.getValues();
    }

    /** Injects given object right into the scope, as I would be injected using given factory. */
    @SuppressWarnings("unchecked") void instrumentObjectIntoScope(
        String classifier, Class type, Object object, InstanceFactory factory
    ) {
        String key = MagnetScope.key(type, classifier);
        RuntimeInstance instance = RuntimeInstance.create(
            object, (Class<InstanceFactory>) factory.getClass(), scope.depth
        );
        RuntimeInstance existing = scope.instances.get(key);
        if (existing == null) {
            scope.instances.put(key, instance);
        } else {
            existing.addInstance(instance);
        }
    }

}
