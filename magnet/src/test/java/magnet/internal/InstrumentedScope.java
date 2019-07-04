package magnet.internal;

import magnet.Scope;
import magnet.Visitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/** Used for testing MagnetScopeContainer. */
public class InstrumentedScope implements Scope, FactoryFilter {

    public final MagnetScope scope;

    public InstrumentedScope(MagnetScope scope) {
        this.scope = scope;
    }

    public InstrumentedScope(Scope scope) {
        this.scope = (MagnetScope) scope;
    }

    @Override public <T> @Nullable T getOptional(@NotNull Class<T> type) {
        return scope.getOptional(type);
    }

    @Override public <T> @Nullable T getOptional(@NotNull Class<T> type, @NotNull String classifier) {
        return scope.getOptional(type, classifier);
    }

    @Override public <T> @NotNull T getSingle(@NotNull Class<T> type) {
        return scope.getSingle(type);
    }

    @Override public <T> @NotNull T getSingle(@NotNull Class<T> type, @NotNull String classifier) {
        return scope.getSingle(type, classifier);
    }

    @Override public <T> @NotNull List<T> getMany(@NotNull Class<T> type) {
        return scope.getMany(type);
    }

    @Override public <T> @NotNull List<T> getMany(@NotNull Class<T> type, @NotNull String classifier) {
        return scope.getMany(type, classifier);
    }

    @Override public <T> @NotNull Scope bind(@NotNull Class<T> type, @NotNull T instance) {
        scope.bind(type, instance);
        return this;
    }

    @Override public <T> @NotNull Scope bind(@NotNull Class<T> type, @NotNull T instance, @NotNull String classifier) {
        scope.bind(type, instance, classifier);
        return this;
    }

    @Override public @NotNull Scope createSubscope() {
        return new InstrumentedScope((MagnetScope) scope.createSubscope());
    }

    @Override public @NotNull Scope limit(String... limits) {
        return scope.limit(limits);
    }

    @Override public void dispose() {
        scope.dispose();
    }

    @Override public void accept(Visitor visitor, int depth) {
        scope.accept(visitor, depth);
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
    @SuppressWarnings("unchecked") public <T> InstrumentedScope instrumentObjectIntoScope(
        InstanceFactory<T> factory, Class<T> objectType, T object, String classifier
    ) {
        String key = MagnetScope.key(objectType, classifier);
        InstanceBucket existing = scope.instanceBuckets.get(key);
        if (existing == null) {
            scope.instanceBuckets.put(
                key,
                new InstanceBucket(scope, factory, objectType, object, classifier, scope)
            );
        } else {
            existing.registerObject(factory, objectType, object, classifier);
        }
        return this;
    }

    InstrumentedScope createInstrumentedSubscope() {
        return new InstrumentedScope((MagnetScope) scope.createSubscope());
    }

}
