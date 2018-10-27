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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Subject to change. For internal use only. */
final class MagnetScope implements Scope {

    private static final byte CARDINALITY_OPTIONAL = 0;
    private static final byte CARDINALITY_SINGLE = 1;
    private static final byte CARDINALITY_MANY = 2;

    private final MagnetScope parent;
    private final InstanceManager instanceManager;

    /** Visible for testing */
    final int depth;

    /** Visible for testing */
    final Map<String, RuntimeInstance> instances;

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
        Object existing = instances.put(key, RuntimeInstance.create(object, null, depth));
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
            T object = getSingleObject(type, classifier, factory, CARDINALITY_MANY);
            if (object != null) objects.add(object);
        }
        return objects;
    }

    @SuppressWarnings("unchecked")
    private <T> T getSingleObject(Class<T> type, String classifier, InstanceFactory<T> factory, byte cardinality) {
        InstantiationContext instantiationContext = this.instantiationContext.get();
        String key = key(type, classifier);

        if (factory == null) {
            RuntimeInstance<T> instance = findInstanceDeep(key);
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

        RuntimeInstance<T> deepInstance = findInstanceDeep(key);
        boolean keepInScope = factory.getScoping() != Scoping.UNSCOPED;

        if (keepInScope) {
            if (deepInstance != null) {
                boolean isSingleOrOptional = cardinality != CARDINALITY_MANY;

                if (isSingleOrOptional) {
                    instantiationContext.onDependencyFound(deepInstance.getScopeDepth());
                    return deepInstance.getValue();
                }

                T object = deepInstance.getValue((Class<InstanceFactory>) factory.getClass());
                if (object != null) {
                    return object;
                }
            }
        }

        String[] selector = factory.getSelector();
        if (selector != null) {
            String selectorName = selector[0];
            SelectorFilter selectorFilter = getSingle(SelectorFilter.class, selectorName);
            if (!selectorFilter.filter(selector)) {
                if (cardinality == CARDINALITY_SINGLE) {
                    StringBuilder selectorBuilder = new StringBuilder();
                    for (String selectorPart : selector) {
                        selectorBuilder.append(selectorPart).append(" ");
                    }
                    throw new IllegalStateException(
                        String.format(
                            "Instance of type: '%s' and classifier: '%s' was excluded by selector filter: %s",
                            type.getName(), classifier, selectorBuilder.toString()));
                }
                return null;
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
                deepInstance.addValue(object, (Class<InstanceFactory>) factory.getClass());

            } else {
                registerInstanceInScope(
                    key, RuntimeInstance.create(object, (Class<InstanceFactory>) factory.getClass(), objectDepth)
                );
            }

            Class[] siblingFactoryTypes = factory.getSiblingTypes();
            if (siblingFactoryTypes != null) {
                for (int i = 0, size = siblingFactoryTypes.length; i < size; i += 2) {
                    String siblingKey = key(siblingFactoryTypes[i], classifier);
                    registerInstanceInScope(
                        siblingKey,
                        RuntimeInstance.create(object, siblingFactoryTypes[i + 1], objectDepth)
                    );
                }
            }

        }

        return object;
    }

    private void registerInstanceInScope(String key, RuntimeInstance instance) {
        if (depth == instance.getScopeDepth()) {
            RuntimeInstance existing = instances.put(key, instance);
            if (existing != null) {
                existing.addInstance(instance);
                instances.put(key, existing);
            }
            return;
        }
        if (parent == null) {
            throw new IllegalStateException(
                String.format(
                    "Cannot register instance with depth %s", instance.getScopeDepth()));
        }
        parent.registerInstanceInScope(key, instance);
    }

    @SuppressWarnings("unchecked")
    private <T> RuntimeInstance<T> findInstanceDeep(String key) {
        RuntimeInstance<T> instance = instances.get(key);
        if (instance == null && parent != null) {
            return parent.findInstanceDeep(key);
        }
        return instance;
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
