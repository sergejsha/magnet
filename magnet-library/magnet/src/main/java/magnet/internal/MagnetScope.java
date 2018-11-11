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

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* Subject to change. For internal use only. */
final class MagnetScope implements Scope, FactoryFilter, InstanceBucket.OnInstanceListener {

    private static final byte CARDINALITY_OPTIONAL = 0;
    private static final byte CARDINALITY_SINGLE = 1;
    private static final byte CARDINALITY_MANY = 2;

    private final MagnetScope parent;
    private final InstanceManager instanceManager;

    private boolean disposed = false;
    private List<WeakReference<MagnetScope>> children;
    private List<InstanceBucket.SingleValueInstance> disposables;

    /** Visible for testing */
    final int depth;

    /** Visible for testing */
    final Map<String, InstanceBucket> instanceBuckets;

    @SuppressWarnings("AnonymousHasLambdaAlternative")
    private final ThreadLocal<InstantiationContext> instantiationContext = new ThreadLocal<InstantiationContext>() {
        @Override protected InstantiationContext initialValue() { return new InstantiationContext(); }
    };

    MagnetScope(MagnetScope parent, InstanceManager instanceManager) {
        this.depth = parent == null ? 0 : parent.depth + 1;
        this.parent = parent;
        this.instanceManager = instanceManager;
        this.instanceBuckets = new HashMap<>(32, 0.75f);
    }

    @Override
    public <T> T getOptional(Class<T> type) {
        checkNotDisposed();
        InstanceFactory<T> factory = instanceManager.getFilteredInstanceFactory(type, Classifier.NONE, this);
        return getSingleObject(type, Classifier.NONE, factory, CARDINALITY_OPTIONAL);
    }

    @Override
    public <T> T getOptional(Class<T> type, String classifier) {
        checkNotDisposed();
        InstanceFactory<T> factory = instanceManager.getFilteredInstanceFactory(type, classifier, this);
        return getSingleObject(type, classifier, factory, CARDINALITY_OPTIONAL);
    }

    @Override
    public <T> T getSingle(Class<T> type) {
        checkNotDisposed();
        InstanceFactory<T> factory = instanceManager.getFilteredInstanceFactory(type, Classifier.NONE, this);
        return getSingleObject(type, Classifier.NONE, factory, CARDINALITY_SINGLE);
    }

    @Override
    public <T> T getSingle(Class<T> type, String classifier) {
        checkNotDisposed();
        InstanceFactory<T> factory = instanceManager.getFilteredInstanceFactory(type, classifier, this);
        return getSingleObject(type, classifier, factory, CARDINALITY_SINGLE);
    }

    @Override
    public <T> List<T> getMany(Class<T> type) {
        checkNotDisposed();
        return getManyObjects(type, Classifier.NONE);
    }

    @Override
    public <T> List<T> getMany(Class<T> type, String classifier) {
        checkNotDisposed();
        return getManyObjects(type, classifier);
    }

    @Override
    public <T> Scope bind(Class<T> type, T object) {
        bind(type, object, Classifier.NONE);
        return this;
    }

    @Override
    public <T> Scope bind(Class<T> type, T object, String classifier) {
        checkNotDisposed();
        final String key = key(type, classifier);
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
    public Scope createSubscope() {
        checkNotDisposed();

        MagnetScope child = new MagnetScope(this, instanceManager);
        if (children == null) children = new ArrayList<>(4);
        children.add(new WeakReference<>(child));

        if (children.size() > 30) {
            Iterator<WeakReference<MagnetScope>> iterator = children.iterator();
            //noinspection Java8CollectionRemoveIf
            while (iterator.hasNext()) {
                WeakReference<MagnetScope> scopeRef = iterator.next();
                if (scopeRef.get() == null) {
                    iterator.remove();
                }
            }
        }
        return child;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void dispose() {
        checkNotDisposed();

        if (children != null) {
            for (WeakReference<MagnetScope> child : children) {
                MagnetScope scope = child.get();
                if (scope != null) {
                    scope.dispose();
                }
            }
        }

        if (disposables != null) {
            for (int i = disposables.size(); i-- > 0; ) {
                InstanceBucket.SingleValueInstance single = disposables.get(i);
                single.factory.dispose(single.instance);
            }
        }

        disposed = true;
    }

    @Override
    public <T> void onInstanceRegistered(InstanceBucket.SingleValueInstance<T> instance) {
        if (instance.factory.isDisposable()) {
            if (disposables == null) {
                disposables = new ArrayList<>(8);
            }
            disposables.add(instance);
        }
    }

    private void checkNotDisposed() {
        if (disposed) throw new IllegalStateException("Scope is disposed.");
    }

    @Override
    public boolean filter(InstanceFactory factory) {
        String[] selector = factory.getSelector();
        if (selector == null) {
            return true;
        }
        SelectorFilter selectorFilter = getSingle(SelectorFilter.class, selector[0]);
        if (selectorFilter == null) {
            throw new IllegalStateException(
                String.format("Factory %s requires selector '%s', which implementation is not available in the scope." +
                        " Make sure to add corresponding %s implementation to the classpath.",
                    factory, Arrays.toString(selector), SelectorFilter.class));
        }
        return selectorFilter.filter(selector);
    }

    private <T> List<T> getManyObjects(Class<T> type, String classifier) {
        List<InstanceFactory<T>> factories = instanceManager.getManyInstanceFactories(type, classifier, this);
        if (factories.size() == 0) return Collections.emptyList();

        List<T> objects = new ArrayList<>(factories.size());
        for (InstanceFactory<T> factory : factories) {
            T object = getSingleObject(type, classifier, factory, CARDINALITY_MANY);
            if (object != null) objects.add(object);
        }
        return objects;
    }

    @SuppressWarnings("unchecked")
    private <T> T getSingleObject(
        Class<T> objectType, String classifier, InstanceFactory<T> factory, byte cardinality
    ) {
        InstantiationContext instantiationContext = this.instantiationContext.get();
        String key = key(objectType, classifier);

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

                T object = deepInstances.getOptionalInstance((Class<InstanceFactory<T>>) factory.getClass());
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
                deepInstances.registerInstance(factory, objectType, object, classifier);

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
                    String siblingKey = key(siblingFactoryTypes[i], classifier);
                    InstanceFactory siblingFactory = instanceManager.getInstanceFactory(
                        objectType, classifier, siblingFactoryTypes[i + 1]
                    );
                    registerInstanceInScope(
                        siblingKey,
                        objectDepth,
                        siblingFactory,
                        objectType,
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
        Class<T> instanceType,
        T instance,
        String classifier
    ) {
        if (this.depth == depth) {
            @SuppressWarnings("unchecked") final InstanceBucket<T> bucket = instanceBuckets.get(key);
            if (bucket == null) {
                instanceBuckets.put(
                    key,
                    new InstanceBucket<>(depth, factory, instanceType, instance, classifier, this)
                );
            } else {
                bucket.registerInstance(factory, instanceType, instance, classifier);
            }
            return;
        }
        if (parent == null) {
            throw new IllegalStateException(
                String.format(
                    "Cannot register instance %s, factory: %s, depth: %s",
                    instance, factory, depth
                )
            );
        }
        parent.registerInstanceInScope(key, depth, factory, instanceType, instance, classifier);
    }

    @SuppressWarnings("unchecked")
    private <T> InstanceBucket<T> findDeepInstanceBucket(String key) {
        InstanceBucket<T> bucket = (InstanceBucket<T>) instanceBuckets.get(key);
        if (bucket == null && parent != null) {
            return parent.findDeepInstanceBucket(key);
        }
        return bucket;
    }

    /** Visible for testing */
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

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Instantiation that = (Instantiation) o;
            return key.equals(that.key);
        }

        @Override public int hashCode() {
            return key.hashCode();
        }
    }

}
