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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import magnet.internal.Factory;
import magnet.internal.Range;

final class MagnetImplementationManager implements ImplementationManager {

    private static final String DEFAULT_TARGET = "";

    private Factory[] factories;
    private Map<Class, Object> index;

    MagnetImplementationManager() {
        registerImplementations();
    }

    private void registerImplementations() {
        try {
            Class<?> magnetClass = Class.forName("magnet.MagnetIndexer");
            Method registerFactories = magnetClass.getMethod("register", MagnetImplementationManager.class);
            registerFactories.invoke(magnetClass, this);
        } catch (Exception e) {
            System.out.println(
                    "MagnetIndexer.class cannot be found. " +
                            "Add a @MagnetizeImplementations-annotated class to the application module.");
        }
    }

    // called by generated index class
    void register(Factory[] factories, Map<Class, Object> index) {
        this.factories = factories;
        this.index = index;
    }

    @Override
    public <T> List<T> get(Class<T> forType, DependencyScope dependencyScope) {
        return get(forType, DEFAULT_TARGET, dependencyScope);
    }

    @Override
    public <T> List<T> get(Class<T> forType, String forTarget, DependencyScope dependencyScope) {
        Object indexed = index.get(forType);

        if (indexed instanceof Range) {
            Range range = (Range) indexed;
            if (range.getTarget().equals(forTarget)) {
                return createFromRange(range, dependencyScope);
            }
            return Collections.emptyList();
        }

        if (indexed instanceof Map) {
            //noinspection unchecked
            Map<String, Range> ranges = (Map<String, Range>) indexed;
            Range range = ranges.get(forTarget);
            if (range != null) {
                return createFromRange(range, dependencyScope);
            }
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    private <T> List<T> createFromRange(Range range, DependencyScope dependencyScope) {
        List<T> impls = new ArrayList<>();
        for (int i = range.getFrom(), to = range.getFrom() + range.getCount(); i < to; i++) {
            //noinspection unchecked
            impls.add((T) factories[i].create(dependencyScope));
        }
        return impls;
    }

}
