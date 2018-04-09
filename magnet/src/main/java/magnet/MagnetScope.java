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
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class MagnetScope implements Scope {

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
        return getObject(type, Classifier.NONE, false);
    }

    @Override
    public <T> T getOptional(Class<T> type, String classifier) {
        return getObject(type, classifier, false);
    }

    @Override
    public <T> T getSingle(Class<T> type) {
        return getObject(type, Classifier.NONE, true);
    }

    @Override
    public <T> T getSingle(Class<T> type, String classifier) {
        return getObject(type, classifier, true);
    }

    @Override
    public <T> List<T> getMany(Class<T> type) {
        // todo, reimplement with instanceManager.getManyFactories()
        return instanceManager.getMany(type, this);
    }

    @Override
    public <T> List<T> getMany(Class<T> type, String classifier) {
        // todo, reimplement with instanceManager.getManyFactories()
        return instanceManager.getMany(type, classifier, this);
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
        Object existing = instances.put(key, new Instance<>(object, depth));
        if (existing != null) {
            throw new IllegalStateException(
                    String.format("Instance of type %s already registered." +
                                    " Existing instance %s, new instance %s",
                            key, existing, object));
        }
    }

    private <T> T getObject(Class<T> type, String classifier, boolean required) {
        InstanceFactory<T> factory = instanceManager.getOptionalFactory(type, classifier);
        AutoScope autoScope = this.autoScope.get();
        String key = key(type, classifier);

        if (factory == null) {
            Instance<T> instance = findInstanceDeep(key);
            if (instance == null) {
                if (required) {
                    throw new IllegalStateException(
                            String.format(
                                    "Instance of type '%s' and classifier '%s' was not found in scopes.",
                                    type.getName(), classifier));
                }
                return null;
            }
            autoScope.onMaybeDependencyInScope(instance.scopeDepth);
            return instance.value;
        }

        if (factory.isScoped()) {
            Instance<T> instance = findInstanceDeep(key);
            if (instance != null) {
                autoScope.onMaybeDependencyInScope(instance.scopeDepth);
                return instance.value;
            }
        }

        autoScope.onBeforeInstanceCreated();
        T object = factory.create(this);
        int depth = autoScope.onAfterInstanceCreated();
        autoScope.onMaybeDependencyInScope(depth);

        if (factory.isScoped()) {
            Instance<T> instance = new Instance<>(object, depth);
            registerInstance(key, instance);
        }

        return object;
    }

    private <T> void registerInstance(String key, Instance<T> instance) {
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

    private <T> Instance<T> findInstanceDeep(String key) {
        Instance instance = instances.get(key);
        if (instance == null && parent != null) {
            return parent.findInstanceDeep(key);
        }
        //noinspection unchecked
        return (Instance<T>) instance;
    }

    /** Used for testing the objects registered in this scope. */
    <T> T getScopeObject(Class<T> type, String classifier) {
        @SuppressWarnings("unchecked") Instance<T> instance = instances.get(key(type, classifier));
        return instance == null ? null : instance.value;
    }

    private static String key(Class<?> type, String classifier) {
        if (classifier == null || classifier.length() == 0) {
            return type.getName();
        }
        return classifier + "@" + type.getName();
    }

    private static class Instance<T> {
        final T value;
        final int scopeDepth;

        Instance(T value, int scopeDepth) {
            this.value = value;
            this.scopeDepth = scopeDepth;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Instance<?> instance = (Instance<?>) o;
            return Objects.equals(value, instance.value);
        }

        @Override public int hashCode() {
            return value.hashCode();
        }
    }

    private static class AutoScope {
        private static final int NONE = -1;
        private final Deque<Integer> depthStack = new ArrayDeque<>();
        private int currentDepth = NONE;

        void onBeforeInstanceCreated() {
            if (currentDepth > NONE) {
                depthStack.addFirst(currentDepth);
            }
            currentDepth = 0;
        }

        int onAfterInstanceCreated() {
            int resultDepth = currentDepth;
            currentDepth = depthStack.isEmpty() ? NONE : depthStack.pollFirst();
            return resultDepth;
        }

        void onMaybeDependencyInScope(int instanceDepth) {
            if (currentDepth == NONE) return;
            if (instanceDepth > currentDepth) {
                currentDepth = instanceDepth;
            }
        }
    }

}

