/*
 * Copyright (C) 2018-2019 Sergej Shafarenka, www.halfbit.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package magnet.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import magnet.Classifier;
import magnet.Scope;
import magnet.Scoping;
import magnet.SelectorFilter;
import magnet.Visitor;

/* Subject to change. For internal use only. */
final class MagnetScope implements Scope, Visitor.Scope, FactoryFilter, InstanceBucket.OnInstanceListener {

    private static final byte CARDINALITY_OPTIONAL = 0;
    private static final byte CARDINALITY_SINGLE = 1;
    private static final byte CARDINALITY_MANY = 2;

    private final @Nullable MagnetScope parent;
    private final @NotNull InstanceManager instanceManager;
    private final int depth;

    private @Nullable WeakScopeReference childrenScopes;
    private @Nullable List<InstanceBucket.InjectedInstance> disposables;
    private @Nullable String[] limits;
    private boolean disposed = false;

    final @NotNull Map<String, InstanceBucket> instanceBuckets;

    @SuppressWarnings("AnonymousHasLambdaAlternative")
    private final @NotNull ThreadLocal<InstantiationContext> instantiationContext =
        new ThreadLocal<InstantiationContext>() {
            @Override protected InstantiationContext initialValue() {
                return new InstantiationContext();
            }
        };

    MagnetScope(@Nullable MagnetScope parent, @NotNull InstanceManager instanceManager) {
        this.depth = parent == null ? 0 : parent.depth + 1;
        this.parent = parent;
        this.instanceManager = instanceManager;
        this.instanceBuckets = new HashMap<>(32, 0.75f);
    }

    @Override
    public <T> @Nullable T getOptional(@NotNull Class<T> type) {
        checkNotDisposed();
        InstanceFactory<T> factory = instanceManager.getFilteredInstanceFactory(type, Classifier.NONE, this);
        return findOrInjectOptional(type, Classifier.NONE, factory, CARDINALITY_OPTIONAL);
    }

    @Override
    public <T> @Nullable T getOptional(@NotNull Class<T> type, @NotNull String classifier) {
        checkNotDisposed();
        InstanceFactory<T> factory = instanceManager.getFilteredInstanceFactory(type, classifier, this);
        return findOrInjectOptional(type, classifier, factory, CARDINALITY_OPTIONAL);
    }

    @Override
    public <T> @NotNull T getSingle(@NotNull Class<T> type) {
        return getSingle(type, Classifier.NONE);
    }

    @Override
    public <T> @NotNull T getSingle(@NotNull Class<T> type, @NotNull String classifier) {
        checkNotDisposed();
        InstanceFactory<T> factory = instanceManager.getFilteredInstanceFactory(type, classifier, this);
        T object = findOrInjectOptional(type, classifier, factory, CARDINALITY_SINGLE);
        if (object == null) {
            throw new IllegalStateException(
                String.format(
                    "Instance of type '%s' (classifier: '%s') was not found in scopes.",
                    type.getName(), classifier
                )
            );
        }
        return object;
    }

    @Override
    public <T> @NotNull List<T> getMany(@NotNull Class<T> type) {
        checkNotDisposed();
        return getManyObjects(type, Classifier.NONE);
    }

    @Override
    public <T> @NotNull List<T> getMany(@NotNull Class<T> type, @NotNull String classifier) {
        checkNotDisposed();
        return getManyObjects(type, classifier);
    }

    @Override
    public <T> @NotNull Scope bind(@NotNull Class<T> type, @NotNull T object) {
        bind(type, object, Classifier.NONE);
        return this;
    }

    @Override
    public <T> @NotNull Scope bind(@NotNull Class<T> type, @NotNull T object, @NotNull String classifier) {
        checkNotDisposed();
        final String key = key(type, classifier);
        Object existing = instanceBuckets.put(
            key,
            new InstanceBucket<>(
                /* scope = */ this,
                /* factory = */ null,
                /* instanceType = */ type,
                /* instance = */ object,
                /* classifier = */ classifier,
                /* listener = */  this
            )
        );
        if (existing != null) {
            throw new IllegalStateException(
                String.format(
                    "Instance of type %s already registered. Existing instance %s, new instance %s",
                    key, existing, object
                )
            );
        }
        return this;
    }

    @Override
    public @NotNull Scope createSubscope() {
        checkNotDisposed();

        MagnetScope child = new MagnetScope(this, instanceManager);
        childrenScopes = new WeakScopeReference(child, childrenScopes);

        return child;
    }

    @Override
    public @NotNull Scope limit(String... limits) {
        if (this.limits != null) {
            throw new IllegalStateException(
                String.format(
                    "Cannot set limits to '%s' because they must only be applied once." +
                        " Current limits '%s'", Arrays.toString(limits), Arrays.toString(this.limits))
            );
        }
        for (String limit : limits) {
            if (limit.length() == 0 || limit.equals("*")) {
                throw new IllegalStateException("Limit must not be empty or be a '*'");
            }
        }
        Arrays.sort(limits);
        this.limits = limits;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void dispose() {
        if (disposed) return;

        WeakScopeReference weakScope = childrenScopes;
        if (weakScope != null) {
            do {
                MagnetScope scope = weakScope.get();
                if (scope != null) {
                    scope.dispose();
                }
                weakScope = weakScope.next;
            } while (weakScope != null);
        }

        if (disposables != null) {
            for (int i = disposables.size(); i-- > 0; ) {
                InstanceBucket.InjectedInstance single = disposables.get(i);
                single.factory.dispose(single.object);
            }
        }

        disposed = true;

        if (parent != null) {
            parent.onChildScopeDisposed(this);
        }
    }

    private void onChildScopeDisposed(MagnetScope childScope) {
        if (childrenScopes == null) return;
        WeakScopeReference prevWeakScope = null;
        WeakScopeReference weakScope = childrenScopes;
        do {
            MagnetScope scope = weakScope.get();
            if (scope == childScope) {
                if (prevWeakScope == null) {
                    childrenScopes = weakScope.next;
                } else {
                    prevWeakScope.next = weakScope.next;
                }
                break;
            }
            prevWeakScope = weakScope;
            weakScope = weakScope.next;
        } while (weakScope != null);
    }

    @Override
    public <T> void onInstanceCreated(InstanceBucket.SingleObjectInstance<T> instance) {
        if (instance instanceof InstanceBucket.InjectedInstance) {
            InstanceBucket.InjectedInstance injected = (InstanceBucket.InjectedInstance) instance;
            if (injected.factory.isDisposable()) {
                if (disposables == null) {
                    disposables = new ArrayList<>(8);
                }
                disposables.add(injected);
            }
        }
    }

    private void checkNotDisposed() {
        if (disposed) throw new IllegalStateException("Scope is already disposed.");
    }

    @Override
    public boolean filter(@NotNull InstanceFactory factory) {
        String[] selector = factory.getSelector();
        if (selector == null) {
            return true;
        }
        SelectorFilter selectorFilter = getOptional(SelectorFilter.class, selector[0]);
        if (selectorFilter == null) {
            throw new IllegalStateException(
                String.format(
                    "Factory %s requires selector '%s', which implementation is not available in the scope." +
                        " Make sure to add corresponding %s implementation to the classpath.",
                    factory, Arrays.toString(selector), SelectorFilter.class)
            );
        }
        return selectorFilter.filter(selector);
    }

    private <T> @NotNull List<T> getManyObjects(Class<T> type, String classifier) {
        List<InstanceFactory<T>> factories = instanceManager.getManyInstanceFactories(type, classifier, this);
        if (factories.size() == 0) return Collections.emptyList();

        List<T> objects = new ArrayList<>(factories.size());
        for (InstanceFactory<T> factory : factories) {
            T object = findOrInjectOptional(type, classifier, factory, CARDINALITY_MANY);
            if (object != null) objects.add(object);
        }
        return objects;
    }

    @SuppressWarnings("unchecked")
    private <T> @Nullable T findOrInjectOptional(
        @NotNull Class<T> objectType,
        @NotNull String classifier,
        @Nullable InstanceFactory<T> factory,
        byte cardinality
    ) {
        @NotNull InstantiationContext instantiationContext = this.instantiationContext.get();
        @NotNull String key = key(objectType, classifier);

        InstanceBucket<T> deepInstanceBucket = findDeepInstanceBucket(key, factory);
        if (factory == null) {
            if (deepInstanceBucket == null) {
                if (cardinality == CARDINALITY_SINGLE) {
                    throw new IllegalStateException(
                        String.format(
                            "Instance of type '%s' (classifier: '%s') was not found in scopes.",
                            objectType.getName(), classifier));
                }
                return null;
            }
            instantiationContext.onDependencyFound(deepInstanceBucket.getScope().depth, key);
            return deepInstanceBucket.getSingleInstance();
        }

        boolean keepInScope = factory.getScoping() != Scoping.UNSCOPED;
        if (keepInScope) {
            if (deepInstanceBucket != null) {
                boolean isSingleOrOptional = cardinality != CARDINALITY_MANY;

                if (isSingleOrOptional) {
                    instantiationContext.onDependencyFound(deepInstanceBucket.getScope().depth, key);
                    return deepInstanceBucket.getSingleInstance();
                }

                T object = deepInstanceBucket.getOptional((Class<InstanceFactory<T>>) factory.getClass());
                if (object != null) {
                    instantiationContext.onDependencyFound(deepInstanceBucket.getScope().depth, key);
                    return object;
                }
            }
        }

        instantiationContext.onBeginInstantiation(key);

        T object = factory.create(this);

        Instantiation instantiation = instantiationContext.onEndInstantiation();
        int objectDepth = instantiation.dependencyDepth;
        Scoping objectScoping = factory.getScoping();

        @NotNull String objectLimit = factory.getLimit();
        if (objectLimit.length() > 0) {
            if (objectScoping == Scoping.TOPMOST) {
                objectDepth = findTopMostLimitedObjectDepth(objectLimit, objectDepth);

            } else if (objectScoping == Scoping.DIRECT) {
                objectDepth = findDirectLimitedObjectDepth(
                    objectLimit, objectDepth, object, objectType, classifier, instantiation
                );
            }

            if (objectDepth < 0) throwLimitNotFound(object, objectType, classifier, objectLimit);

        } else {
            if (objectScoping == Scoping.DIRECT) objectDepth = this.depth;
        }

        instantiationContext.onDependencyFound(objectDepth, key);

        if (keepInScope) {

            boolean canRegisterAtDeepInstanceBucket = deepInstanceBucket != null
                && deepInstanceBucket.getScope().depth == objectDepth;

            if (canRegisterAtDeepInstanceBucket) {
                deepInstanceBucket.registerObject(factory, objectType, object, classifier);

            } else {
                registerInstanceInScope(
                    key,
                    objectDepth,
                    factory,
                    objectType,
                    object,
                    classifier
                );
            }

            Class[] siblingFactoryTypes = factory.getSiblingTypes();
            if (siblingFactoryTypes != null) {
                for (int i = 0, size = siblingFactoryTypes.length; i < size; i += 2) {
                    Class siblingObjectType = siblingFactoryTypes[i];
                    String siblingKey = key(siblingObjectType, classifier);
                    InstanceFactory siblingFactory = instanceManager.getInstanceFactory(
                        siblingObjectType, classifier, siblingFactoryTypes[i + 1]
                    );
                    registerInstanceInScope(
                        siblingKey,
                        objectDepth,
                        siblingFactory,
                        siblingObjectType,
                        object,
                        classifier
                    );
                }
            }
        }

        return object;
    }

    private int findTopMostLimitedObjectDepth(String objectLimit, int objectDepth) {
        @Nullable MagnetScope scope = this;
        while (scope != null) {
            if (objectDepth > scope.depth) {
                return objectDepth;
            } else if (scope.hasLimit(objectLimit)) {
                return scope.depth;
            }
            scope = scope.parent;
        }
        return -1;
    }

    private <T> int findDirectLimitedObjectDepth(
        String objectLimit, int objectDepth, T object, Class<T> objectType, String classifier,
        Instantiation instantiation
    ) {
        @Nullable MagnetScope scope = this;
        int limitingScopeDepth = -1;
        while (scope != null) {
            if (scope.hasLimit(objectLimit)) {
                limitingScopeDepth = scope.depth;
                break;
            }
            scope = scope.parent;
        }

        if (limitingScopeDepth > -1 && limitingScopeDepth < objectDepth) {
            StringBuilder logDetails = new StringBuilder();
            buildInstanceDetails(logDetails, object, objectType, classifier, objectLimit);

            throw new IllegalStateException(
                String.format(
                    "Cannot register instance in limiting scope [depth: %s] because its" +
                        " dependency '%s' is located in non-reachable child scope [depth: %s].\n%s",
                    limitingScopeDepth,
                    instantiation.dependencyKey,
                    instantiation.dependencyDepth,
                    logDetails
                )
            );
        }

        return limitingScopeDepth;
    }

    private boolean hasLimit(@NotNull String limit) {
        if (limit.length() == 0 || limits == null) {
            return false;
        }
        return Arrays.binarySearch(limits, limit) > -1;
    }

    private <T> void registerInstanceInScope(
        @NotNull String key,
        int depth,
        @Nullable InstanceFactory<T> factory,
        @NotNull Class<T> objectType,
        @NotNull T object,
        @NotNull String classifier
    ) {
        if (this.depth == depth) {
            @SuppressWarnings("unchecked") final InstanceBucket<T> bucket = instanceBuckets.get(key);
            if (bucket == null) {
                instanceBuckets.put(
                    key,
                    new InstanceBucket<>(this, factory, objectType, object, classifier, this)
                );
            } else {
                bucket.registerObject(factory, objectType, object, classifier);
            }
            return;
        }
        if (parent == null) {
            throw new IllegalStateException(
                String.format(
                    "Cannot register instance %s, factory: %s, depth: %s",
                    object, factory, depth
                )
            );
        }
        parent.registerInstanceInScope(key, depth, factory, objectType, object, classifier);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> InstanceBucket<T> findDeepInstanceBucket(String key, InstanceFactory<T> factory) {
        InstanceBucket<T> bucket = (InstanceBucket<T>) instanceBuckets.get(key);
        if (bucket != null && bucket.hasInstanceWithFactory(factory)) return bucket;
        if (parent == null) return null;
        return parent.findDeepInstanceBucket(key, factory);
    }

    @NotNull
    static String key(Class<?> type, String classifier) {
        if (classifier == null || classifier.length() == 0) {
            return type.getName();
        }
        return classifier + "@" + type.getName();
    }

    @Override
    public void accept(Visitor visitor, int depth) {
        acceptAtLevel(0, visitor, depth);
    }

    private void acceptAtLevel(int level, Visitor visitor, int depth) {
        if (disposed) return;

        boolean visitScopes = true;
        if (visitor.onEnterScope(this, parent)) {
            Collection<InstanceBucket> buckets = this.instanceBuckets.values();
            for (InstanceBucket bucket : buckets) {
                if (!bucket.accept(visitor)) {
                    visitScopes = false;
                    break;
                }
            }
        }

        if (visitScopes && level < depth) {
            WeakScopeReference scopeRef = this.childrenScopes;
            while (scopeRef != null) {
                MagnetScope scope = scopeRef.get();
                if (scope != null) scope.acceptAtLevel(level + 1, visitor, depth);
                scopeRef = scopeRef.next;
            }
        }

        visitor.onExitScope(this);
    }

    @Override public @Nullable String[] getLimits() {
        return limits;
    }

    private static <T> void buildInstanceDetails(
        StringBuilder builder, T object, Class<T> objectType, String classifier, String objectLimit
    ) {
        builder
            .append("Instance:")
            .append("\n\tobject : ").append(object)
            .append("\n\ttype : ").append(objectType.getName());

        if (classifier.length() > 0) {
            builder
                .append("\n\tclassifier: '").append(classifier).append("'");
        }

        builder
            .append("\n\tlimit : '").append(objectLimit).append("'");
    }

    private <T> void throwLimitNotFound(T object, Class<T> objectType, String classifier, String objectLimit) {
        StringBuilder logDetail = new StringBuilder();
        buildInstanceDetails(logDetail, object, objectType, classifier, objectLimit);
        logDetail.append(" <- required limit");

        logDetail.append("\n\nSearched scopes:\n\t-> ");
        @Nullable MagnetScope scope = this;
        while (scope != null) {
            String scopeName = scope.toString();
            if (scope.limits != null) {
                logDetail.append('(');
                for (String limit : scope.limits) {
                    logDetail.append("'").append(limit).append("', ");
                }
                logDetail.setLength(logDetail.length() - 2);
                logDetail.append(") ").append(scope.toString());
            } else logDetail.append(scopeName);
            logDetail.append("\n\t-> ");
            scope = scope.parent;
        }
        logDetail.setLength(logDetail.length() - 5);
        logDetail.append(" <root scope>");

        throw new IllegalStateException(
            String.format(
                "Cannot register instance because no scope with limit '%s' has been found.\n%s",
                objectLimit, logDetail
            )
        );
    }

    private final static class InstantiationContext {
        private final ArrayDeque<Instantiation> instantiations = new ArrayDeque<>();
        private Instantiation currentInstantiation;

        void onBeginInstantiation(String key) {
            if (currentInstantiation != null) {
                instantiations.addFirst(currentInstantiation);
            }
            currentInstantiation = new Instantiation(key);
            if (instantiations.contains(currentInstantiation)) {
                throw createCircularDependencyException();
            }
        }

        @NotNull Instantiation onEndInstantiation() {
            Instantiation instantiation = currentInstantiation;
            currentInstantiation = instantiations.isEmpty() ? null : instantiations.pollFirst();
            return instantiation;
        }

        void onDependencyFound(int dependencyDepth, @NotNull String dependencyKey) {
            if (currentInstantiation == null) return;
            if (dependencyDepth > currentInstantiation.dependencyDepth) {
                currentInstantiation.dependencyDepth = dependencyDepth;
                currentInstantiation.dependencyKey = dependencyKey;
            }
        }

        private IllegalStateException createCircularDependencyException() {

            Instantiation[] objects = instantiations.toArray(new Instantiation[0]);
            StringBuilder builder = new StringBuilder()
                .append("Dependency injection failed because of unresolved circular dependency: ");
            for (int i = objects.length; i-- > 0; ) {
                builder.append(objects[i].key).append(" -> ");
            }
            builder.append(currentInstantiation.key);

            return new IllegalStateException(builder.toString());
        }
    }

    private final static class Instantiation {
        final String key;
        int dependencyDepth;
        @Nullable String dependencyKey;

        Instantiation(String key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Instantiation that = (Instantiation) o;
            return key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    private final static class WeakScopeReference extends WeakReference<MagnetScope> {
        private @Nullable WeakScopeReference next;
        WeakScopeReference(MagnetScope referent, @Nullable WeakScopeReference next) {
            super(referent);
            this.next = next;
        }
    }
}
