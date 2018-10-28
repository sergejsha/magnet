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

import magnet.Magnetizer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/* Subject to change. For internal use only. */
@SuppressWarnings("unchecked")
final class MagnetInstanceManager implements InstanceManager {

    private InstanceFactory[] factories;
    private Map<Class, Object> index;

    MagnetInstanceManager() {
        registerInstanceFactories();
    }

    private void registerInstanceFactories() {
        try {
            Class<?> magnetClass = Class.forName("magnet.internal.MagnetIndexer");
            Method registerFactories = magnetClass.getMethod("register", MagnetInstanceManager.class);
            registerFactories.invoke(magnetClass, this);
        } catch (Exception e) {
            System.err.println(
                String.format(
                    "MagnetIndexer cannot be found. Add a @%s-annotated class to the application module.",
                    Magnetizer.class
                )
            );
        }
    }

    // called by generated index class
    void register(InstanceFactory[] factories, Map<Class, Object> index) {
        this.factories = factories;
        this.index = index;
    }

    @Override
    public <T> InstanceFactory<T> getOptionalFactory(
        Class<T> type, String classifier, FactoryFilter factoryFilter
    ) {
        Range range = getOptionalRange(type, classifier);
        if (range == null) {
            return null;
        }

        if (range.getCount() == 1) {
            InstanceFactory factory = factories[range.getFrom()];
            if (factoryFilter.filter(factory)) {
                return factory;
            }
            return null;
        }

        InstanceFactory<T> factory = null;
        for (int index = range.getFrom(), afterLast = range.getFrom() + range.getCount(); index < afterLast; index++) {
            InstanceFactory<T> candidate = factories[index];
            if (factoryFilter.filter(candidate)) {
                if (factory != null) {
                    throw new IllegalStateException(
                        String.format("Multiple implementations of type %s (classifier: %s) can be injected," +
                                " while single implementation is expected. Overloaded factories: %s, %s",
                            type, classifier, factory, candidate));
                }
                factory = candidate;
            }
        }

        return factory;
    }

    @Override
    public <T> List<InstanceFactory<T>> getManyFactories(
        Class<T> type, String classifier, FactoryFilter factoryFilter
    ) {
        Object indexed = index.get(type);

        if (indexed instanceof Range) {
            Range range = (Range) indexed;
            if (range.getClassifier().equals(classifier)) {
                return factoriesFromRange(range, factoryFilter);
            }
            return Collections.emptyList();
        }

        if (indexed instanceof Map) {
            Map<String, Range> ranges = (Map<String, Range>) indexed;
            Range range = ranges.get(classifier);
            if (range != null) {
                return factoriesFromRange(range, factoryFilter);
            }
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    private Range getOptionalRange(Class<?> type, String classifier) {
        Object indexed = index.get(type);

        if (indexed == null) {
            return null;
        }

        if (indexed instanceof Range) {
            Range range = (Range) indexed;
            if (classifier.equals(range.getClassifier())) {
                return (Range) indexed;
            }
            return null;
        }

        if (indexed instanceof Map) {
            Map<String, Range> ranges = (Map<String, Range>) indexed;
            return ranges.get(classifier);
        }

        throw new IllegalStateException(
            String.format(
                "Unsupported index type: %s", indexed.getClass()));
    }

    private <T> List<InstanceFactory<T>> factoriesFromRange(Range range, FactoryFilter factoryFilter) {
        List<InstanceFactory<T>> filteredFactories = null;
        for (int index = range.getFrom(), afterLast = range.getFrom() + range.getCount(); index < afterLast; index++) {
            InstanceFactory<T> factory = factories[index];
            if (factory.getSelector() != null) {
                if (filteredFactories == null) {
                    filteredFactories = new ArrayList<>(range.getCount());
                }
                if (factoryFilter.filter(factory)) {
                    filteredFactories.add(factory);
                }
            }
        }

        if (filteredFactories != null) {
            return filteredFactories;
        }

        InstanceFactory<T>[] factories = new InstanceFactory[range.getCount()];
        System.arraycopy(this.factories, range.getFrom(), factories, 0, range.getCount());
        return new ImmutableArrayList<>(factories);
    }

}
