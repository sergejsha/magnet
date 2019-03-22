package magnet.internal;

import magnet.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/** Used for testing MagnetScopeContainer. */
public class InstrumentedInstanceScope implements Scope, FactoryFilter {

    private final MagnetScope scope;

    InstrumentedInstanceScope(MagnetScope scope) {
        this.scope = scope;
    }

    @Nullable
    @Override
    public <T> T getOptional(@NotNull Class<T> type) {
        return scope.getOptional(type);
    }

    @Nullable
    @Override
    public <T> T getOptional(@NotNull Class<T> type, @NotNull String classifier) {
        return scope.getOptional(type, classifier);
    }

    @Override
    @NotNull
    public <T> T getSingle(@NotNull Class<T> type) {
        return scope.getSingle(type);
    }

    @NotNull
    @Override
    public <T> T getSingle(@NotNull Class<T> type, @NotNull String classifier) {
        return scope.getSingle(type, classifier);
    }

    @NotNull
    @Override
    public <T> List<T> getMany(@NotNull Class<T> type) {
        return scope.getMany(type);
    }

    @NotNull
    @Override
    public <T> List<T> getMany(@NotNull Class<T> type, @NotNull String classifier) {
        return scope.getMany(type, classifier);
    }

    @NotNull
    @Override
    public <T> Scope bind(@NotNull Class<T> type, @NotNull T instance) {
        scope.bind(type, instance);
        return this;
    }

    @NotNull
    @Override
    public <T> Scope bind(@NotNull Class<T> type, @NotNull T instance, @NotNull String classifier) {
        scope.bind(type, instance, classifier);
        return this;
    }

    @NotNull
    @Override
    public Scope createSubscope() {
        return new InstrumentedInstanceScope((MagnetScope) scope.createSubscope());
    }

    @Override public void dispose() {
        scope.dispose();
    }

    @Override public boolean filter(InstanceFactory factory) { return scope.filter(factory); }

    /** Returns and object registered right in this scope or null if no object was registered. */
    @SuppressWarnings("unchecked") <T> T getOptionalInScope(Class<T> type, String classifier) {
        InstanceBucket<T> instance = scope.instanceBuckets.get(MagnetScope.key(type, classifier));
        return instance == null ? null : instance.getSingleInstance();
    }

    /** Returns list of objects registered right in this scope. */
    @SuppressWarnings("unchecked") <T> List<T> getManyInScope(Class<T> type, String classifier) {
        InstanceBucket<T> instance = scope.instanceBuckets.get(MagnetScope.key(type, classifier));
        return instance == null ? Collections.emptyList() : instance.getMany();
    }

    /** Injects given object right into the scope, as I would be injected using given factory. */
    @SuppressWarnings("unchecked") <T> InstrumentedInstanceScope instrumentObjectIntoScope(
        InstanceFactory<T> factory, Class<T> objectType, T object, String classifier
    ) {
        String key = MagnetScope.key(objectType, classifier);
        InstanceBucket existing = scope.instanceBuckets.get(key);
        if (existing == null) {
            scope.instanceBuckets.put(
                key,
                new InstanceBucket(scope.depth, factory, objectType, object, classifier, scope)
            );
        } else {
            existing.registerObject(factory, objectType, object, classifier);
        }
        return this;
    }

    InstrumentedInstanceScope createInstrumentedSubscope() {
        return new InstrumentedInstanceScope((MagnetScope) scope.createSubscope());
    }

}
