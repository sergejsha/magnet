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
import java.util.Map;

final class MagnetDependencyScope implements DependencyScope {

    private final MagnetDependencyScope parent;
    private final Map<String, Object> dependencies;

    private MagnetDependencyScope(MagnetDependencyScope parent) {
        this.parent = parent;
        this.dependencies = new HashMap<>();
    }

    MagnetDependencyScope() {
        this.parent = null;
        this.dependencies = new HashMap<>();
    }

    @Override
    public <T> T get(Class<T> type) {
        return get(type.getName());
    }

    @Override
    public <T> T get(Class<T> type, String qualifier) {
        return get(key(qualifier, type));
    }

    @Override
    public <T> T require(Class<T> type) {
        return require(type.getName());
    }

    @Override
    public <T> T require(Class<T> type, String qualifier) {
        return require(key(qualifier, type));
    }

    @Override
    public <T> DependencyScope register(Class<T> type, T dependency) {
        register(type.getName(), dependency);
        return this;
    }

    @Override
    public <T> DependencyScope register(Class<T> type, T dependency, String qualifier) {
        register(key(qualifier, type), dependency);
        return this;
    }

    @Override
    public DependencyScope subscope() {
        return new MagnetDependencyScope(this);
    }

    private static String key(String qualifier, Class<?> type) {
        if (qualifier == null || qualifier.length() == 0) {
            return type.getName();
        }
        return qualifier + "#" + type.getName();
    }

    private <T> T get(String key) {
        Object component = dependencies.get(key);
        if (component == null && parent != null) {
            return parent.get(key);
        }
        //noinspection unchecked
        return (T) component;
    }

    private <T> T require(String key) {
        T component = get(key);
        if (component == null) {
            throw new IllegalStateException(
                    String.format(
                            "Component of type %s must be registered, but it's not.", key));
        }
        return component;
    }

    private void register(String key, Object dependency) {
        Object existing = dependencies.put(key, dependency);
        if (existing != null) {
            throw new IllegalStateException(
                    String.format("Dependency of type %s already registered." +
                                    " Existing dependency %s, new dependency %s",
                            key, existing, dependency));
        }
    }


}