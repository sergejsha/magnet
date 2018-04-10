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

final class MagnetScope implements Scope {

    private static byte CARDINALITY_OPTIONAL = 0;
    private static byte CARDINALITY_SINGLE = 1;
    private static byte CARDINALITY_MANY = 2;

    private final int depth;
    private final MagnetScope parent;
    private final InstanceManager instanceManager;
    private final Map<String, Instance> instances;

    @SuppressWarnings("AnonymousHasLambdaAlternative")
    private ThreadLocal<AutoScope> autoScope = new ThreadLocal<AutoScope>() {
        @Override protected AutoScope initialValue() { return new AutoScope(); }
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
        return getObject(type, Classifier.NONE, factory, CARDINALITY_OPTIONAL);
    }

    @Override
    public <T> T getOptional(Class<T> type, String classifier) {
        InstanceFactory<T> factory = instanceManager.getOptionalFactory(type, classifier);
        return getObject(type, classifier, factory, CARDINALITY_OPTIONAL);
    }

    @Override
    public <T> T getSingle(Class<T> type) {
        InstanceFactory<T> factory = instanceManager.getOptionalFactory(type, Classifier.NONE);
        return getObject(type, Classifier.NONE, factory, CARDINALITY_SINGLE);
    }

    @Override
    public <T> T getSingle(Class<T> type, String classifier) {
        InstanceFactory<T> factory = instanceManager.getOptionalFactory(type, classifier);
        return getObject(type, classifier, factory, CARDINALITY_SINGLE);
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
    public <T> Scope register(Class<T> type, T object) {
        register(key(type, Classifier.NONE), object);
        return this;
    }

    @Override
    public <T> Scope register(Class<T> type, T object, String classifier) {
        register(key(type, classifier), object);
        return this;
    }

    @Override
    public Scope subscope() {
        return new MagnetScope(this, instanceManager);
    }

    private void register(String key, Object object) {
        Object existing = instances.put(key, new Instance(object, depth));
        if (existing != null) {
            throw new IllegalStateException(
                    String.format("Instance of type %s already registered." +
                                    " Existing instance %s, new instance %s",
                            key, existing, object));
        }
    }

    private <T> List<T> getManyObjects(Class<T> type, String classifier) {
        List<InstanceFactory<T>> factories = instanceManager.getManyFactories(type, classifier);
        if (factories.size() == 0) return Collections.emptyList();

        List<T> objects = new ArrayList<>(factories.size());
        for (InstanceFactory<T> factory : factories) {
            objects.add(getObject(type, Classifier.NONE, factory, CARDINALITY_MANY));
        }
        return objects;
    }

    private <T> T getObject(Class<T> type, String classifier, InstanceFactory<T> factory, byte cardinality) {
        AutoScope autoScope = this.autoScope.get();
        String key = key(type, classifier);

        if (factory == null) {
            Instance instance = findInstanceDeep(key);
            if (instance == null) {
                if (cardinality == CARDINALITY_SINGLE) {
                    throw new IllegalStateException(
                            String.format(
                                    "Instance of type '%s' and classifier '%s' was not found in scopes.",
                                    type.getName(), classifier));
                }
                return null;
            }
            autoScope.onMaybeDependencyInScope(instance.scopeDepth);
            //noinspection unchecked
            return (T) instance.value;
        }

        if (factory.isScoped()) {
            Instance instance = findInstanceDeep(key);
            if (instance != null) {
                autoScope.onMaybeDependencyInScope(instance.scopeDepth);
                //noinspection unchecked
                return (T) instance.value;
            }
        }

        autoScope.onBeforeInstanceCreated(key);
        T object = factory.create(this);
        int depth = autoScope.onAfterInstanceCreated();
        autoScope.onMaybeDependencyInScope(depth);

        if (factory.isScoped()) {
            Instance instance = new Instance(object, depth);
            registerInstance(key, instance);
        }

        return object;
    }

    private void registerInstance(String key, Instance instance) {
        if (depth == instance.scopeDepth) {
            instances.put(key, instance);
            return;
        }
        if (parent == null) {
            throw new IllegalStateException(
                    String.format(
                            "Cannot register instance with depth %s", instance.scopeDepth));
        }
        parent.registerInstance(key, instance);
    }

    private Instance findInstanceDeep(String key) {
        Instance instance = instances.get(key);
        if (instance == null && parent != null) {
            return parent.findInstanceDeep(key);
        }
        return instance;
    }

    /** Used for testing the objects registered in this scope. */
    <T> T getScopeObject(Class<T> type, String classifier) {
        Instance instance = instances.get(key(type, classifier));
        //noinspection unchecked
        return instance == null ? null : (T) instance.value;
    }

    private static String key(Class<?> type, String classifier) {
        if (classifier == null || classifier.length() == 0) {
            return type.getName();
        }
        return classifier + "@" + type.getName();
    }

    private final static class Instance {
        final Object value;
        final int scopeDepth;

        Instance(Object value, int scopeDepth) {
            this.value = value;
            this.scopeDepth = scopeDepth;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Instance instance = (Instance) o;
            return value.equals(instance.value);
        }

        @Override public int hashCode() {
            return value.hashCode();
        }
    }

    private final static class AutoScope {
        private final ArrayDeque<InstanceCreation> creationStack = new ArrayDeque<>();
        private InstanceCreation instanceCreation;

        void onBeforeInstanceCreated(String key) {
            if (instanceCreation != null) {
                creationStack.addFirst(instanceCreation);
            }
            instanceCreation = new InstanceCreation(key);
            if (creationStack.contains(instanceCreation)) {
                throw new IllegalStateException("Circular dependency detected");
            }
        }

        int onAfterInstanceCreated() {
            int resultDepth = instanceCreation.depth;
            instanceCreation = creationStack.isEmpty() ? null : creationStack.pollFirst();
            return resultDepth;
        }

        void onMaybeDependencyInScope(int instanceDepth) {
            if (instanceCreation == null) return;
            if (instanceDepth > instanceCreation.depth) {
                instanceCreation.depth = instanceDepth;
            }
        }
    }

    private final static class InstanceCreation {
        final String key;
        int depth;

        InstanceCreation(String key) {
            this.key = key;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InstanceCreation that = (InstanceCreation) o;
            return key.equals(that.key);
        }

        @Override public int hashCode() {
            return key.hashCode();
        }
    }

}
