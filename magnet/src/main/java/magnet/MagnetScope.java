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

package magnet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import magnet.internal.Instance;

final class MagnetScope implements Scope {

    private static final byte CARDINALITY_OPTIONAL = 0;
    private static final byte CARDINALITY_SINGLE = 1;
    private static final byte CARDINALITY_MANY = 2;

    private final int depth;
    private final MagnetScope parent;
    private final InstanceManager instanceManager;
    private final Map<String, Instance> instances;

    @SuppressWarnings("AnonymousHasLambdaAlternative")
    private final ThreadLocal<InstantiationContext> instantiationContext = new ThreadLocal<InstantiationContext>() {
        @Override protected InstantiationContext initialValue() { return new InstantiationContext(); }
    };

    MagnetScope(MagnetScope parent, InstanceManager instanceManager) {
        this.depth = parent == null ? 0 : parent.depth + 1;
        this.parent = parent;
        this.instanceManager = instanceManager;
        this.instances = new HashMap<>();
    }

    @Override
    public <T> T getOptional(Class<T> type) {
        InstanceFactory<T> factory = instanceManager.getOptionalFactory(type, Classifier.NONE);
        return getSingleObject(type, Classifier.NONE, factory, CARDINALITY_OPTIONAL);
    }

    @Override
    public <T> T getOptional(Class<T> type, String classifier) {
        InstanceFactory<T> factory = instanceManager.getOptionalFactory(type, classifier);
        return getSingleObject(type, classifier, factory, CARDINALITY_OPTIONAL);
    }

    @Override
    public <T> T getSingle(Class<T> type) {
        InstanceFactory<T> factory = instanceManager.getOptionalFactory(type, Classifier.NONE);
        return getSingleObject(type, Classifier.NONE, factory, CARDINALITY_SINGLE);
    }

    @Override
    public <T> T getSingle(Class<T> type, String classifier) {
        InstanceFactory<T> factory = instanceManager.getOptionalFactory(type, classifier);
        return getSingleObject(type, classifier, factory, CARDINALITY_SINGLE);
    }

    @Override
    public <T> List<T> getMany(Class<T> type) {
        return getManyObjects(type, Classifier.NONE);
    }

    @Override
    public <T> List<T> getMany(Class<T> type, String classifier) {
        return getManyObjects(type, classifier);
    }

    @Override
    public <T> Scope bind(Class<T> type, T object) {
        bind(key(type, Classifier.NONE), object);
        return this;
    }

    @Override
    public <T> Scope bind(Class<T> type, T object, String classifier) {
        bind(key(type, classifier), object);
        return this;
    }

    @Override
    public Scope createSubscope() {
        return new MagnetScope(this, instanceManager);
    }

    private void bind(String key, Object object) {
        Object existing = instances.put(key, Instance.create(object, null, depth));
        if (existing != null) {
            throw new IllegalStateException(
                    String.format("Instance of type %s already registered. Existing instance %s, new instance %s",
                            key, existing, object));
        }
    }

    private <T> List<T> getManyObjects(Class<T> type, String classifier) {
        List<InstanceFactory<T>> factories = instanceManager.getManyFactories(type, classifier);
        if (factories.size() == 0) return Collections.emptyList();

        List<T> objects = new ArrayList<>(factories.size());
        for (InstanceFactory<T> factory : factories) {
            objects.add(getSingleObject(type, classifier, factory, CARDINALITY_MANY));
        }
        return objects;
    }

    @SuppressWarnings("unchecked")
    private <T> T getSingleObject(Class<T> type, String classifier, InstanceFactory<T> factory, byte cardinality) {
        InstantiationContext instantiationContext = this.instantiationContext.get();
        String key = key(type, classifier);

        if (factory == null) {
            Instance<T> instance = findInstanceDeep(key);
            if (instance == null) {
                if (cardinality == CARDINALITY_SINGLE) {
                    throw new IllegalStateException(
                            String.format(
                                    "Instance of type: '%s' and classifier: '%s' was not found in scopes.",
                                    type.getName(), classifier));
                }
                return null;
            }
            instantiationContext.onDependencyFound(instance.getScopeDepth());
            return instance.getValue();
        }

        Instance<T> deepInstance = findInstanceDeep(key);
        boolean keepInScope = factory.getScoping() != Scoping.UNSCOPED;

        if (keepInScope) {
            if (deepInstance != null) {
                boolean isSingleOrOptional = cardinality != CARDINALITY_MANY;

                if (isSingleOrOptional) {
                    instantiationContext.onDependencyFound(deepInstance.getScopeDepth());
                    return deepInstance.getValue();
                }

                T object = deepInstance.getValue(factory);
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

            boolean canUseDeepInstance = deepInstance != null
                    && deepInstance.getScopeDepth() == objectDepth;

            if (canUseDeepInstance) {
                deepInstance.addValue(object, factory);

            } else {
                registerInstanceInScope(key, Instance.create(object, factory, objectDepth), factory.getScoping());
            }
        }

        return object;
    }

    private void registerInstanceInScope(String key, Instance instance, Scoping scoping) {
        if (depth == instance.getScopeDepth()) {
            Instance existing = instances.put(key, instance);
            if (existing != null) {
                throw new IllegalStateException(
                        String.format(
                                "Instance %s with key %s already registered.", instance, key));
            }
            return;
        }
        if (parent == null) {
            throw new IllegalStateException(
                    String.format(
                            "Cannot register instance with depth %s", instance.getScopeDepth()));
        }
        parent.registerInstanceInScope(key, instance, scoping);
    }

    @SuppressWarnings("unchecked")
    private <T> Instance<T> findInstanceDeep(String key) {
        Instance<T> instance = instances.get(key);
        if (instance == null && parent != null) {
            return parent.findInstanceDeep(key);
        }
        return instance;
    }

    /** Used for testing the objects registered in this scope. */
    @SuppressWarnings("unchecked") <T> T getRegisteredSingle(Class<T> type, String classifier) {
        Instance<T> instance = instances.get(key(type, classifier));
        return instance == null ? null : instance.getValue();
    }

    /** Used for testing the objects registered in this scope. */
    @SuppressWarnings("unchecked") <T> List<T> getRegisteredMany(Class<T> type, String classifier) {
        Instance<T> instance = instances.get(key(type, classifier));
        return instance == null ? Collections.emptyList() : instance.getValues();
    }

    private static String key(Class<?> type, String classifier) {
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
                    .append("Magnet failed because of unresolved circular dependency between implementations: ");
            for (int i = objects.length; i-- > 0; ) {
                builder.append(objects[i].key).append(" --> ");
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
