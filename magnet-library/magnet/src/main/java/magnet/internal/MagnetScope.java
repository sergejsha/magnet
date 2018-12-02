/*
 * Copyright (C) 2018 Sergej Shafarenka, www.halfbit.de
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

import magnet.Classifier;
import magnet.Scope;
import magnet.Scoping;
import magnet.SelectorFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

/* Subject to change. For internal use only. */
final class MagnetScope implements Scope, FactoryFilter, InstanceBucket.OnInstanceListener {

    private static final byte CARDINALITY_OPTIONAL = 0;
    private static final byte CARDINALITY_SINGLE = 1;
    private static final byte CARDINALITY_MANY = 2;

    @Nullable
    private final MagnetScope parent;
    @NotNull
    private final InstanceManager instanceManager;

    @Nullable
    private WeakScopeReference childrenScopes;
    @Nullable
    private List<InstanceBucket.InjectedInstance> disposables;
    private boolean disposed = false;

    /** Visible for testing */
    final int depth;

    /** Visible for testing */
    @NotNull
    final Map<String, InstanceBucket> instanceBuckets;

    @SuppressWarnings("AnonymousHasLambdaAlternative")
    @NotNull
    private final ThreadLocal<InstantiationContext> instantiationContext = new ThreadLocal<InstantiationContext>() {
        @Override
        protected InstantiationContext initialValue() {
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
    @Nullable
    public <T> T getOptional(@NotNull Class<T> type) {
        checkNotDisposed();
        InstanceFactory<T> factory = instanceManager.getFilteredInstanceFactory(type, Classifier.NONE, this);
        return findOrInjectOptional(type, Classifier.NONE, factory, CARDINALITY_OPTIONAL);
    }

    @Override
    @Nullable
    public <T> T getOptional(@NotNull Class<T> type, @NotNull String classifier) {
        checkNotDisposed();
        InstanceFactory<T> factory = instanceManager.getFilteredInstanceFactory(type, classifier, this);
        return findOrInjectOptional(type, classifier, factory, CARDINALITY_OPTIONAL);
    }

    @Override
    @NotNull
    public <T> T getSingle(@NotNull Class<T> type) {
        return getSingle(type, Classifier.NONE);
    }

    @Override
    @NotNull
    public <T> T getSingle(@NotNull Class<T> type, @NotNull String classifier) {
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
    @NotNull
    public <T> List<T> getMany(@NotNull Class<T> type) {
        checkNotDisposed();
        return getManyObjects(type, Classifier.NONE);
    }

    @Override
    @NotNull
    public <T> List<T> getMany(@NotNull Class<T> type, @NotNull String classifier) {
        checkNotDisposed();
        return getManyObjects(type, classifier);
    }

    @Override
    @NotNull
    public <T> Scope bind(@NotNull Class<T> type, @NotNull T object) {
        bind(type, object, Classifier.NONE);
        return this;
    }

    @Override
    @NotNull
    public <T> Scope bind(@NotNull Class<T> type, @NotNull T object, @NotNull String classifier) {
        checkNotDisposed();
        final String key = key(type, classifier);
        // todo
        Object existing = instanceBuckets.put(
            key,
            new InstanceBucket<>(
                /* depth = */ depth,
                /* factory = */ null,
                /* instanceType = */ type,
                /* instance = */ object,
                /* classifier = */ classifier,
                /* listener = */  this
            )
        );
        if (existing != null) {
            throw new IllegalStateException(
                String.format("Instance of type %s already registered. Existing instance %s, new instance %s",
                    key, existing, object));
        }
        return this;
    }

    @Override
    @NotNull
    public Scope createSubscope() {
        checkNotDisposed();

        MagnetScope child = new MagnetScope(this, instanceManager);
        childrenScopes = new WeakScopeReference(child, childrenScopes);

        return child;
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
    public <T> void onInstanceCreated(InstanceBucket.SingleInstance<T> instance) {
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
                String.format("Factory %s requires selector '%s', which implementation is not available in the scope." +
                        " Make sure to add corresponding %s implementation to the classpath.",
                    factory, Arrays.toString(selector), SelectorFilter.class));
        }
        return selectorFilter.filter(selector);
    }

    @NotNull
    private <T> List<T> getManyObjects(Class<T> type, String classifier) {
        List<InstanceFactory<T>> factories = instanceManager.getManyInstanceFactories(type, classifier, this);
        if (factories.size() == 0) return Collections.emptyList();

        List<T> objects = new ArrayList<>(factories.size());
        for (InstanceFactory<T> factory : factories) {
            T object = findOrInjectOptional(type, classifier, factory, CARDINALITY_MANY);
            if (object != null) objects.add(object);
        }
        return objects;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> T findOrInjectOptional(
        Class<T> objectType, String classifier, InstanceFactory<T> factory, byte cardinality
    ) {
        @NotNull InstantiationContext instantiationContext = this.instantiationContext.get();
        @NotNull String key = key(objectType, classifier);

        if (factory == null) {
            InstanceBucket<T> instanceBucket = findDeepInstanceBucket(key);
            if (instanceBucket == null) {
                if (cardinality == CARDINALITY_SINGLE) {
                    throw new IllegalStateException(
                        String.format(
                            "Instance of type '%s' (classifier: '%s') was not found in scopes.",
                            objectType.getName(), classifier));
                }
                return null;
            }
            instantiationContext.onDependencyFound(instanceBucket.getScopeDepth());
            return instanceBucket.getSingleInstance();
        }

        InstanceBucket<T> deepInstances = findDeepInstanceBucket(key);
        boolean keepInScope = factory.getScoping() != Scoping.UNSCOPED;

        if (keepInScope) {
            if (deepInstances != null) {
                boolean isSingleOrOptional = cardinality != CARDINALITY_MANY;

                if (isSingleOrOptional) {
                    instantiationContext.onDependencyFound(deepInstances.getScopeDepth());
                    return deepInstances.getSingleInstance();
                }

                T object = deepInstances.getOptional((Class<InstanceFactory<T>>) factory.getClass());
                if (object != null) {
                    return object;
                }
            }
        }

        instantiationContext.onBeginInstantiation(key);

        T object = factory.create(this);

        int objectDepth = instantiationContext.onEndInstantiation();
        if (factory.getScoping() == Scoping.DIRECT) objectDepth = this.depth;
        instantiationContext.onDependencyFound(objectDepth);

        if (keepInScope) {

            boolean canUseDeepInstances = deepInstances != null
                && deepInstances.getScopeDepth() == objectDepth;

            if (canUseDeepInstances) {
                deepInstances.registerObject(factory, objectType, object, classifier);

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

    private <T> void registerInstanceInScope(
        String key,
        int depth,
        InstanceFactory<T> factory,
        Class<T> objectType,
        T object,
        String classifier
    ) {
        if (this.depth == depth) {
            @SuppressWarnings("unchecked") final InstanceBucket<T> bucket = instanceBuckets.get(key);
            if (bucket == null) {
                instanceBuckets.put(
                    key,
                    new InstanceBucket<>(depth, factory, objectType, object, classifier, this)
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
    private <T> InstanceBucket<T> findDeepInstanceBucket(String key) {
        InstanceBucket<T> bucket = (InstanceBucket<T>) instanceBuckets.get(key);
        if (bucket == null && parent != null) {
            return parent.findDeepInstanceBucket(key);
        }
        return bucket;
    }

    /** Visible for testing */
    @NotNull
    static String key(Class<?> type, String classifier) {
        if (classifier == null || classifier.length() == 0) {
            return type.getName();
        }
        return classifier + "@" + type.getName();
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

        int onEndInstantiation() {
            int resultDepth = currentInstantiation.depth;
            currentInstantiation = instantiations.isEmpty() ? null : instantiations.pollFirst();
            return resultDepth;
        }

        void onDependencyFound(int dependencyDepth) {
            if (currentInstantiation == null) return;
            if (dependencyDepth > currentInstantiation.depth) {
                currentInstantiation.depth = dependencyDepth;
            }
        }

        private IllegalStateException createCircularDependencyException() {

            Instantiation[] objects = instantiations.toArray(new Instantiation[0]);
            StringBuilder builder = new StringBuilder()
                .append("Magnet failed because of unresolved circular dependency: ");
            for (int i = objects.length; i-- > 0; ) {
                builder.append(objects[i].key).append(" -> ");
            }
            builder.append(currentInstantiation.key);

            return new IllegalStateException(builder.toString());
        }
    }

    private final static class Instantiation {
        final String key;
        int depth;

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
        @Nullable
        private WeakScopeReference next;

        WeakScopeReference(MagnetScope referent, @Nullable WeakScopeReference next) {
            super(referent);
            this.next = next;
        }
    }

}
