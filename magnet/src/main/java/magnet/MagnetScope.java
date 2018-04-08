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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import magnet.internal.Dependencies;

final class MagnetScope implements Scope {

    private final MagnetScope parent;
    private final Map<String, Object> instances;
    private final InstanceManager instanceManager;

    MagnetScope(MagnetScope parent, InstanceManager instanceManager) {
        this.parent = parent;
        this.instances = new HashMap<>();
        this.instanceManager = instanceManager;
    }

    @Override
    public <T> T getOptional(Class<T> type) {
        return getInstance(type, Classifier.NONE, false);
    }

    @Override
    public <T> T getOptional(Class<T> type, String classifier) {
        return getInstance(type, classifier, false);
    }

    @Override
    public <T> T getSingle(Class<T> type) {
        return getInstance(type, Classifier.NONE, true);
    }

    @Override
    public <T> T getSingle(Class<T> type, String classifier) {
        return getInstance(type, classifier, true);
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
    public <T> Scope register(Class<T> type, T instance) {
        register(Dependencies.key(type, Classifier.NONE), instance);
        return this;
    }

    @Override
    public <T> Scope register(Class<T> type, T instance, String classifier) {
        register(Dependencies.key(type, classifier), instance);
        return this;
    }

    @Override
    public Scope subscope() {
        return new MagnetScope(this, instanceManager);
    }

    private void register(String key, Object dependency) {
        Object existing = instances.put(key, dependency);
        if (existing != null) {
            throw new IllegalStateException(
                    String.format("Instance of type %s already registered." +
                                    " Existing instance %s, new instance %s",
                            key, existing, dependency));
        }
    }

    private <T> T getInstance(Class<T> type, String classifier, boolean required) {
        InstanceFactory<T> factory = instanceManager.getOptionalFactory(type, classifier);

        if (factory == null) {
            String key = Dependencies.key(type, classifier);
            T instance = findInstance(key);
            if (required && instance == null) {
                throw new IllegalStateException(
                        String.format(
                                "Instance of type '%s' and classifier '%s' was not found in scopes.",
                                type, classifier));
            }
            return instance;
        }

        if (factory.isScoped()) {
            String key = Dependencies.key(type, classifier);
            T instance = findInstance(key);
            if (instance != null) {
                return instance;
            }
        }

        T instance = factory.create(this);
        // todo register instance in scope

        return instance;
    }

    private <T> T findInstance(String key) {
        Object instance = instances.get(key);
        if (instance == null && parent != null) {
            return parent.findInstance(key);
        }
        //noinspection unchecked
        return (T) instance;
    }

}